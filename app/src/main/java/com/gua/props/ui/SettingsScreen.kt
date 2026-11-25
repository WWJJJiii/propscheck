package com.gua.props.ui

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gua.props.AppColor
import com.gua.props.util.Prefs

/**
 * 设置页：模板入口 + 三个闹钟 + 权限入口 + 数据。
 * 时间直接调系统的 TimePicker，不自己画选择器。
 */
@Composable
fun SettingsScreen(
    prefs: Prefs,
    onBack: () -> Unit,
    onAlarmChanged: () -> Unit,
    onOpenTemplates: () -> Unit
) {
    val ctx = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "←",
                fontSize = 20.sp,
                color = AppColor.Sub,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
            Text(
                "设置",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColor.Ink
            )
        }

        Spacer(Modifier.height(12.dp))

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // ---------------- 模板 ----------------
            SectionTitle("模板")
            LinkRow("模板管理", "看明细 / 删模板") { onOpenTemplates() }
            Spacer(Modifier.height(12.dp))

            // ---------------- 提醒 ----------------
            SectionTitle("提醒")

            AlarmRow(
                label = "早上提醒",
                sub = "今天有多少项没勾",
                time = prefs.morningTime,
                checked = prefs.morningOn,
                onTime = {
                    pickTime(ctx, prefs.morningTime) { v ->
                        prefs.morningTime = v
                        onAlarmChanged()
                    }
                },
                onToggle = {
                    prefs.morningOn = it
                    onAlarmChanged()
                }
            )

            AlarmRow(
                label = "出发前提醒",
                sub = "确认都带齐了",
                time = prefs.departTime,
                checked = prefs.departOn,
                onTime = {
                    pickTime(ctx, prefs.departTime) { v ->
                        prefs.departTime = v
                        onAlarmChanged()
                    }
                },
                onToggle = {
                    prefs.departOn = it
                    onAlarmChanged()
                }
            )

            AlarmRow(
                label = "收工提醒",
                sub = "东西都收回来了吗",
                time = prefs.wrapTime,
                checked = prefs.wrapOn,
                onTime = {
                    pickTime(ctx, prefs.wrapTime) { v ->
                        prefs.wrapTime = v
                        onAlarmChanged()
                    }
                },
                onToggle = {
                    prefs.wrapOn = it
                    onAlarmChanged()
                }
            )

            Spacer(Modifier.height(20.dp))

            // ---------------- 权限 ----------------
            SectionTitle("权限（不设好，闹钟可能不响）")

            LinkRow("通知权限", "允许弹通知") {
                openNotificationSettings(ctx)
            }
            LinkRow("电池优化", "把本 App 加入白名单") {
                openBatterySettings(ctx)
            }

            Spacer(Modifier.height(20.dp))

            // ---------------- 数据 ----------------
            SectionTitle("数据")

            LinkRow("导出备份", "下一步做") {
                toast(ctx, "导出功能下一步做")
            }
            LinkRow("从备份恢复", "下一步做") {
                toast(ctx, "恢复功能下一步做")
            }

            Spacer(Modifier.height(20.dp))

            SectionTitle("关于")
            InfoRow("版本", "0.3（MVP）")
            InfoRow("数据位置", "手机本地 shoot.db")

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        fontSize = 12.sp,
        color = AppColor.Sub,
        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
    )
}

@Composable
private fun AlarmRow(
    label: String,
    sub: String,
    time: String,
    checked: Boolean,
    onTime: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, AppColor.Line, shape)
            .padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, color = AppColor.Ink, fontWeight = FontWeight.Medium)
            Text(sub, fontSize = 11.sp, color = AppColor.Sub)
        }
        Text(
            time,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AppColor.Ink,
            modifier = Modifier
                .clickable(onClick = onTime)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedTrackColor = AppColor.Green)
        )
    }
}

@Composable
private fun LinkRow(label: String, sub: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, AppColor.Line, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = AppColor.Ink, modifier = Modifier.weight(1f))
        Text(sub, fontSize = 12.sp, color = AppColor.Sub)
        Text("  ›", fontSize = 14.sp, color = AppColor.Sub)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = AppColor.Sub)
        Text(value, fontSize = 13.sp, color = AppColor.Ink)
    }
}

// ---------------- 系统弹窗 / 跳转 ----------------

private fun pickTime(ctx: Context, current: String, onPicked: (String) -> Unit) {
    val parts = current.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: 7
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 30
    TimePickerDialog(
        ctx,
        { _, hh, mm -> onPicked(String.format("%02d:%02d", hh, mm)) },
        h,
        m,
        true
    ).show()
}

private fun openNotificationSettings(ctx: Context) {
    try {
        val i = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
        ctx.startActivity(i)
    } catch (e: Exception) {
        toast(ctx, "打不开通知设置，去系统设置里手动找")
    }
}

private fun openBatterySettings(ctx: Context) {
    try {
        ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
    } catch (e: Exception) {
        toast(ctx, "打不开电池优化页，去系统设置里手动找")
    }
}

private fun toast(ctx: Context, msg: String) {
    Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
}
