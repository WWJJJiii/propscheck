package com.gua.props.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gua.props.AppColor

private val CATEGORIES = listOf("道具", "服装", "鞋", "群演", "其他")

/**
 * 加一条 / 改一条。返回 (标题, 分类, 数量)。
 * 标题为空时"保存"不可点。
 */
@Composable
fun AddItemDialog(
    initialTitle: String = "",
    initialCategory: String = "道具",
    initialQty: Int = 1,
    isEdit: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, qty: Int) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var category by remember { mutableStateOf(initialCategory) }
    var qty by remember { mutableIntStateOf(initialQty) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "改一条" else "加一条", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    label = { Text("名称") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))
                Text("分类", fontSize = 12.sp, color = AppColor.Sub)

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CATEGORIES.forEach { c ->
                        FilterChip(
                            selected = category == c,
                            onClick = { category = c },
                            label = { Text(c, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("数量", fontSize = 12.sp, color = AppColor.Sub)
                    Spacer(Modifier.weight(1f))
                    QtyButton("−") { if (qty > 1) qty-- }
                    Text(
                        "$qty",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )
                    QtyButton("＋") { if (qty < 99) qty++ }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title.trim(), category, qty) },
                enabled = title.isNotBlank()
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun QtyButton(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.width(44.dp)) {
        Text(label, fontSize = 18.sp, color = AppColor.Ink)
    }
}
