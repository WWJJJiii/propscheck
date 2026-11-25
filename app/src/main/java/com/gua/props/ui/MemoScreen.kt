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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.gua.props.model.Memo

/**
 * 备忘页：一屏做完"记、勾、改、删"。
 */
@Composable
fun MemoScreen(
    memos: List<Memo>,
    onBack: () -> Unit,
    onAdd: (String) -> Unit,
    onToggle: (Memo) -> Unit,
    onDelete: (Memo) -> Unit,
    onEdit: (Memo, String) -> Unit
) {
    var input by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<Memo?>(null) }

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
                "备忘",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColor.Ink
            )
        }

        Spacer(Modifier.height(16.dp))

        // ---------- 输入 ----------
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                singleLine = true,
                placeholder = { Text("记一条…", fontSize = 13.sp) },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.padding(horizontal = 4.dp))
            Button(
                onClick = {
                    val t = input.trim()
                    if (t.isNotEmpty()) {
                        onAdd(t)
                        input = ""
                    }
                },
                enabled = input.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColor.Ink)
            ) { Text("存", fontSize = 14.sp) }
        }

        Spacer(Modifier.height(12.dp))

        // ---------- 列表 ----------
        LazyColumn(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(memos, key = { it.id }) { m ->
                MemoRow(
                    memo = m,
                    onToggle = { onToggle(m) },
                    onEdit = { editing = m },
                    onDelete = { onDelete(m) }
                )
            }
            item {
                if (memos.isEmpty()) {
                    Text(
                        "还没有备忘。上面写一条，点「存」。",
                        fontSize = 13.sp,
                        color = AppColor.Sub,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    // ---------- 编辑弹窗 ----------
    editing?.let { m ->
        var text by remember(m.id) { mutableStateOf(m.content) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("改备忘", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEdit(m, text.trim())
                        editing = null
                    },
                    enabled = text.isNotBlank()
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { editing = null }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun MemoRow(
    memo: Memo,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (memo.done) AppColor.Card else Color.White)
            .border(1.dp, AppColor.Line, shape)
            .clickable(onClick = onToggle)
            .padding(start = 4.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = memo.done,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = AppColor.Green)
        )
        Text(
            memo.content,
            fontSize = 14.sp,
            color = if (memo.done) AppColor.Sub else AppColor.Ink,
            textDecoration = if (memo.done) TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f)
        )
        Text(
            "✎",
            fontSize = 13.sp,
            color = AppColor.Sub,
            modifier = Modifier
                .clickable(onClick = onEdit)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        )
        Text(
            "✕",
            fontSize = 13.sp,
            color = AppColor.Sub,
            modifier = Modifier
                .clickable(onClick = onDelete)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MemoPreview() {
    PropsTheme {
        MemoScreen(
            memos = listOf(
                Memo(id = "1", content = "跟客户确认领带颜色"),
                Memo(id = "2", content = "戒指是张姐的，明天还", done = true)
            ),
            onBack = {}, onAdd = {}, onToggle = {}, onDelete = {}, onEdit = { _, _ -> }
        )
    }
}
