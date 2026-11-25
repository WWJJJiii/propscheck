package com.gua.props.model

/** 模板里的一项。同一个 tplName 的若干行 = 一个模板。 */
data class TplItem(
    val id: String = "",
    val tplName: String = "",       // "街拍外景"
    val title: String = "",
    val category: String = "道具",
    val qty: Int = 1,
    val expand: Boolean = false,    // true 且 qty>1 时，生成会拆成 N 行（群演用）
    val sort: Int = 0,
    val createdAt: Long = 0L,
    val lastUsedAt: Long = 0L
)
