package com.gua.props.model

/** 快速备忘。 */
data class Memo(
    val id: String = "",
    val content: String = "",
    val done: Boolean = false,
    val createdAt: Long = 0L
)
