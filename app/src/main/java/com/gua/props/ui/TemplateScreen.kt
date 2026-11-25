package com.gua.props.ui

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gua.props.AppColor
import com.gua.props.db.TplItemDao
import com.gua.props.model.TplItem

/**
 * 模板管理：列出所有模板，点开看行，可以删模板、删行、直接套用。
 * 行内容是点开时才去数据库读的（loadItems）。
 */
@Composable
fun TemplateScreen(
    briefs: List<TplItemDao.TplBrief>,
    loadItems: (String) -> List<TplItem>,
    onBack: () -> Unit,
    onApply: (String) -> Unit,
    onDeleteTemplate: (String) -> Unit,
    onDeleteRow: (TplItem) -> Unit
) {
    var expanded by remember { mutableStateOf<Set<String>>(emptySet()) }
    val cache = remember { mutableStateMapOf<String, List<TplItem>>() }
    var pendingDelete by remember { mutableStateOf<String?>(null) }

    fun toggle(name: String) {
        expanded = if (name in expanded) {
            expanded - name
        } else {
            if (cache[name] == null) cache[name] = loadItems(name)
            expanded + name
        }
    }

    fun refresh(name: String) {
        cache[name] = loadItems(name)
    }

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
                "模板",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColor.Ink
            )
            Spacer(Modifier.weight(1f))
            Text("${briefs.size} 个", fontSize = 12.sp, color = AppColor.Sub)
        }

        Spacer(Modifier.height(12.dp))

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (briefs.isEmpty()) {
                Text(
                    "还没有模板。\n在今日页点「套用模板 → 把当前清单存成模板」，就有第一个了。",
                    fontSize = 13.sp,
                    color = AppColor.Sub,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }

            briefs.forEach { b ->
                val isOpen = b.name in expanded
                val rows = cache[b.name].orEmpty()

                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, AppColor.Line, RoundedCornerShape(12.dp))
                ) {
                    // ---- 模板头 ----
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { toggle(b.name) }
                            .padding(start = 14.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                b.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColor.Ink
                            )
                            Text(
                                "${b.count} 项" + if (isOpen) "" else "  ·  点开看明细",
                                fontSize = 11.sp,
                                color = AppColor.Sub
                            )
                        }
                        Text(
                            "用",
                            fontSize = 13.sp,
                            color = AppColor.Ink,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable { onApply(b.name) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                        Text(
                            "删",
                            fontSize = 13.sp,
                            color = AppColor.Red,
                            modifier = Modifier
                                .clickable { pendingDelete = b.name }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // ---- 展开的行 ----
                    if (isOpen) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .background(AppColor.Card)
                                .padding(start = 14.dp, end = 10.dp, top = 4.dp, bottom = 4.dp)
                        ) {
                            if (rows.isEmpty()) {
                                Text(
                                    "这个模板是空的",
                                    fontSize = 12.sp,
                                    color = AppColor.Sub,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                            rows.forEach { r ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        r.title,
                                        fontSize = 12.sp,
                                        color = AppColor.Ink,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        "${r.category} ×${r.qty}" + if (r.expand) " 按件" else "",
                                        fontSize = 11.sp,
                                        color = AppColor.Sub
                                    )
                                    Text(
                                        "删",
                                        fontSize = 11.sp,
                                        color = AppColor.Sub,
                                        modifier = Modifier
                                            .clickable {
                                                onDeleteRow(r)
                                                refresh(b.name)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ---- 删模板确认 ----
    pendingDelete?.let { name ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除模板？") },
            text = { Text("「$name」会被整个删掉，已经生成的清单不受影响。") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteTemplate(name)
                    pendingDelete = null
                }) { Text("删除", color = AppColor.Red) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("取消") }
            }
        )
    }
}
