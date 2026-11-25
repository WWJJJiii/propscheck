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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gua.props.AppColor
import com.gua.props.PropsTheme
import com.gua.props.model.PlanItem
import com.gua.props.util.DateUtil

/**
 * 今日页：纯展示 + 回调，不碰数据库。
 */
@Composable
fun HomeScreen(
    date: String,
    list: List<PlanItem>,
    memoCount: Int,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onToggle: (PlanItem) -> Unit,
    onDelete: (PlanItem) -> Unit,
    onAddClick: () -> Unit,
    onApplyTemplate: () -> Unit,
    onMemoClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val doneCount = list.count { it.done }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // ---------- 顶部：日期切换 ----------
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "←",
                fontSize = 20.sp,
                color = AppColor.Sub,
                modifier = Modifier
                    .clickable(onClick = onPrevDay)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
            Column(Modifier.weight(1f)) {
                Text(
                    DateUtil.title(date),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColor.Ink
                )
                Text(
                    "${DateUtil.relative(date)} · $doneCount / ${list.size} 已勾完",
                    fontSize = 12.sp,
                    color = AppColor.Sub
                )
            }
            Text(
                "→",
                fontSize = 20.sp,
                color = AppColor.Sub,
                modifier = Modifier
                    .clickable(onClick = onNextDay)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
            Text(
                "⚙",
                fontSize = 18.sp,
                color = AppColor.Sub,
                modifier = Modifier
                    .clickable(onClick = onSettingsClick)
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        // ---------- 清单 ----------
        LazyColumn(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(list, key = { it.id }) { item ->
                PlanRow(
                    item = item,
                    onToggle = { onToggle(item) },
                    onDelete = { onDelete(item) }
                )
            }
            item {
                AddRow(onClick = onAddClick)
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---------- 备忘入口 ----------
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AppColor.Card)
                .clickable(onClick = onMemoClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("备忘", fontSize = 14.sp, color = AppColor.Ink, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Text("$memoCount 条", fontSize = 12.sp, color = AppColor.Sub)
            Text("  ›", fontSize = 14.sp, color = AppColor.Sub)
        }

        Spacer(Modifier.height(12.dp))

        // ---------- 唯一的按钮 ----------
        Button(
            onClick = onApplyTemplate,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColor.Ink)
        ) {
            Text("套用模板", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun PlanRow(item: PlanItem, onToggle: () -> Unit, onDelete: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (item.done) AppColor.Card else Color.White)
            .border(1.dp, AppColor.Line, shape)
            .clickable(onClick = onToggle)
            .padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.done,
            onCheckedChange = null,   // 整行都能点，避免重复触发
            colors = CheckboxDefaults.colors(checkedColor = AppColor.Green)
        )
        Column(Modifier.weight(1f)) {
            Text(
                item.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (item.done) AppColor.Sub else AppColor.Ink,
                textDecoration = if (item.done) TextDecoration.LineThrough else null
            )
            Text(
                "${item.category} ×${item.qty}",
                fontSize = 11.sp,
                color = AppColor.Sub
            )
        }
        Text(
            "删",
            fontSize = 12.sp,
            color = AppColor.Sub,
            modifier = Modifier
                .clickable(onClick = onDelete)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun AddRow(onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White)
            .border(1.dp, AppColor.Line, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text("＋ 加一条", fontSize = 14.sp, color = AppColor.Sub)
    }
}

@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    PropsTheme {
        HomeScreen(
            date = DateUtil.today(),
            list = listOf(
                PlanItem(id = "1", title = "主纱 · 鱼尾", category = "服装", qty = 1),
                PlanItem(id = "2", title = "裙撑-大", category = "服装", qty = 1, done = true),
                PlanItem(id = "3", title = "烟饼", category = "道具", qty = 4)
            ),
            memoCount = 3,
            onPrevDay = {}, onNextDay = {}, onToggle = {}, onDelete = {},
            onAddClick = {}, onApplyTemplate = {}, onMemoClick = {}, onSettingsClick = {}
        )
    }
}
