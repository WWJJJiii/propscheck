package com.gua.props.model

/** 今日清单里的一项。done 用 Boolean，读写库时在 DAO 里转 0/1。 */
data class PlanItem(
    val id: String = "",
    val date: String = "",          // "2026-11-24"
    val title: String = "",
    val category: String = "道具",   // 道具/服装/鞋/群演/其他
    val qty: Int = 1,
    val done: Boolean = false,
    val note: String = "",
    val sort: Int = 0,
    val createdAt: Long = 0L,
    val doneAt: Long = 0L
)
