package com.gua.props

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.gua.props.alarm.AlarmScheduler
import com.gua.props.db.DbHelper
import com.gua.props.db.MemoDao
import com.gua.props.db.PlanItemDao
import com.gua.props.db.TplItemDao
import com.gua.props.model.Memo
import com.gua.props.model.PlanItem
import com.gua.props.model.TplItem
import com.gua.props.ui.AddItemDialog
import com.gua.props.ui.HomeScreen
import com.gua.props.ui.MemoScreen
import com.gua.props.ui.NameInputDialog
import com.gua.props.ui.SettingsScreen
import com.gua.props.ui.TemplatePickerDialog
import com.gua.props.ui.TemplateScreen
import com.gua.props.util.DateUtil
import com.gua.props.util.Prefs

/** 全局色板 */
object AppColor {
    val Ink = Color(0xFF1E1F22)
    val Sub = Color(0xFF8A8F98)
    val Line = Color(0xFFE1E4E8)
    val Bg = Color(0xFFFFFFFF)
    val Card = Color(0xFFF4F5F7)
    val Red = Color(0xFFD93025)
    val Yellow = Color(0xFFC77F1A)
    val Green = Color(0xFF2E9E5B)
    val Purple = Color(0xFF6C4FE0)
}

private enum class Screen { HOME, MEMO, TEMPLATE, SETTINGS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 每次启动都重排三个闹钟（系统可能把它们清掉了）
        AlarmScheduler.scheduleAll(this)

        val helper = DbHelper.get(this)
        val planDao = PlanItemDao(helper)
        val tplDao = TplItemDao(helper)
        val memoDao = MemoDao(helper)

        setContent {
            PropsTheme {
                AppRoot(planDao, tplDao, memoDao)
            }
        }
    }
}

@Composable
fun PropsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = AppColor.Ink,
            onPrimary = Color.White,
            background = AppColor.Bg,
            surface = AppColor.Bg,
            onSurface = AppColor.Ink
        ),
        content = content
    )
}

@Composable
fun AppRoot(planDao: PlanItemDao, tplDao: TplItemDao, memoDao: MemoDao) {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }

    var screen by remember { mutableStateOf(Screen.HOME) }
    var date by remember { mutableStateOf(DateUtil.today()) }

    var items by remember { mutableStateOf<List<PlanItem>>(emptyList()) }
    var memos by remember { mutableStateOf<List<Memo>>(emptyList()) }
    var briefs by remember { mutableStateOf<List<TplItemDao.TplBrief>>(emptyList()) }
    var memoCount by remember { mutableStateOf(0) }

    var showAdd by remember { mutableStateOf(false) }
    var showTpl by remember { mutableStateOf(false) }
    var showSaveTpl by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<PlanItem?>(null) }

    fun reloadHome() {
        items = planDao.listByDate(date)
        memoCount = memoDao.countUndone()
    }

    // 页面切换 / 日期切换时刷新对应数据
    LaunchedEffect(screen, date) {
        reloadHome()
        when (screen) {
            Screen.MEMO -> memos = memoDao.listAll()
            Screen.TEMPLATE -> briefs = tplDao.listBrief()
            else -> Unit
        }
    }

    // Android 13+ 申请通知权限（不给也能用，只是不弹提醒）
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // 系统返回键：非首页就回首页
    BackHandler(enabled = screen != Screen.HOME) { screen = Screen.HOME }

    when (screen) {
        Screen.HOME -> HomeScreen(
            date = date,
            list = items,
            memoCount = memoCount,
            onPrevDay = { date = DateUtil.shift(date, -1) },
            onNextDay = { date = DateUtil.shift(date, 1) },
            onToggle = { item ->
                planDao.setDone(item.id, !item.done)
                reloadHome()
            },
            onDelete = { pendingDelete = it },
            onAddClick = { showAdd = true },
            onApplyTemplate = { showTpl = true },
            onMemoClick = { screen = Screen.MEMO },
            onSettingsClick = { screen = Screen.SETTINGS }
        )

        Screen.MEMO -> MemoScreen(
            memos = memos,
            onBack = { screen = Screen.HOME },
            onAdd = { text ->
                memoDao.insert(text)
                memos = memoDao.listAll()
                memoCount = memoDao.countUndone()
            },
            onToggle = { m ->
                memoDao.setDone(m.id, !m.done)
                memos = memoDao.listAll()
                memoCount = memoDao.countUndone()
            },
            onDelete = { m ->
                memoDao.delete(m.id)
                memos = memoDao.listAll()
                memoCount = memoDao.countUndone()
            },
            onEdit = { m, text ->
                memoDao.updateContent(m.id, text)
                memos = memoDao.listAll()
            }
        )

        Screen.TEMPLATE -> TemplateScreen(
            briefs = briefs,
            loadItems = { name -> tplDao.itemsOf(name) },
            onBack = { screen = Screen.SETTINGS },
            onApply = { name ->
                val rows = tplDao.itemsOf(name)
                val result = planDao.addFromTemplate(date, expandRows(rows))
                tplDao.touch(name)
                toast(ctx, "已加 ${result.added} 条，跳过 ${result.skipped} 条重复")
                briefs = tplDao.listBrief()
            },
            onDeleteTemplate = { name ->
                tplDao.deleteTemplate(name)
                briefs = tplDao.listBrief()
                toast(ctx, "已删除模板：$name")
            },
            onDeleteRow = { row -> tplDao.deleteRow(row.id) }
        )

        Screen.SETTINGS -> SettingsScreen(
            prefs = prefs,
            onBack = { screen = Screen.HOME },
            onAlarmChanged = {
                AlarmScheduler.scheduleAll(ctx)
                toast(ctx, "闹钟已重排")
            },
            onOpenTemplates = { screen = Screen.TEMPLATE }
        )
    }

    // ---------- 加一条 ----------
    if (showAdd) {
        AddItemDialog(
            onDismiss = { showAdd = false },
            onConfirm = { title, category, qty ->
                planDao.insert(
                    PlanItem(date = date, title = title, category = category, qty = qty)
                )
                showAdd = false
                reloadHome()
            }
        )
    }

    // ---------- 删除确认 ----------
    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除这一条？") },
            text = { Text(item.title) },
            confirmButton = {
                TextButton(onClick = {
                    planDao.delete(item.id)
                    pendingDelete = null
                    reloadHome()
                }) { Text("删除", color = AppColor.Red) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("取消") }
            }
        )
    }

    // ---------- 套用模板（首页弹层） ----------
    if (showTpl) {
        TemplatePickerDialog(
            briefs = tplDao.listBrief(),
            onDismiss = { showTpl = false },
            onSaveCurrent = {
                if (items.isEmpty()) {
                    toast(ctx, "这一天还没有清单，没东西可存")
                } else {
                    showTpl = false
                    showSaveTpl = true
                }
            },
            onPick = { name ->
                val rows = tplDao.itemsOf(name)
                val result = planDao.addFromTemplate(date, expandRows(rows))
                tplDao.touch(name)
                showTpl = false
                toast(ctx, "已加 ${result.added} 条，跳过 ${result.skipped} 条重复")
                reloadHome()
            }
        )
    }

    // ---------- 存为模板 ----------
    if (showSaveTpl) {
        NameInputDialog(
            title = "存为模板",
            hint = "例如：街拍外景",
            onDismiss = { showSaveTpl = false },
            onConfirm = { name ->
                val rows = items.map {
                    TplItem(
                        tplName = name,
                        title = it.title,
                        category = it.category,
                        qty = it.qty
                    )
                }
                tplDao.saveAsTemplate(name, rows)
                showSaveTpl = false
                toast(ctx, "已存为模板：$name")
            }
        )
    }
}

/** 模板行 → 清单行；expand 且 qty>1 时拆成 #1..#N */
private fun expandRows(rows: List<TplItem>): List<PlanItem> {
    val out = ArrayList<PlanItem>()
    rows.forEach { r ->
        if (r.expand && r.qty > 1) {
            repeat(r.qty) { i ->
                out.add(
                    PlanItem(
                        title = "${r.title} #${i + 1}",
                        category = r.category,
                        qty = 1
                    )
                )
            }
        } else {
            out.add(PlanItem(title = r.title, category = r.category, qty = r.qty))
        }
    }
    return out
}

private fun toast(ctx: android.content.Context, msg: String) {
    Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
}
