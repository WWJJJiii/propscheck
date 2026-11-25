package com.gua.props.util

import android.content.Context

/**
 * 三个闹钟的时间 + 开关。存 SharedPreferences，不建表。
 * 时间统一用 "HH:mm" 字符串，读写都简单。
 */
class Prefs(ctx: Context) {

    private val sp = ctx.getSharedPreferences("props_prefs", Context.MODE_PRIVATE)

    var morningTime: String
        get() = sp.getString(K_MORNING, "07:30") ?: "07:30"
        set(v) = sp.edit().putString(K_MORNING, v).apply()

    var morningOn: Boolean
        get() = sp.getBoolean(K_MORNING_ON, true)
        set(v) = sp.edit().putBoolean(K_MORNING_ON, v).apply()

    var departTime: String
        get() = sp.getString(K_DEPART, "08:30") ?: "08:30"
        set(v) = sp.edit().putString(K_DEPART, v).apply()

    var departOn: Boolean
        get() = sp.getBoolean(K_DEPART_ON, true)
        set(v) = sp.edit().putBoolean(K_DEPART_ON, v).apply()

    var wrapTime: String
        get() = sp.getString(K_WRAP, "18:30") ?: "18:30"
        set(v) = sp.edit().putString(K_WRAP, v).apply()

    var wrapOn: Boolean
        get() = sp.getBoolean(K_WRAP_ON, true)
        set(v) = sp.edit().putBoolean(K_WRAP_ON, v).apply()

    /** 把一个 "HH:mm" 拆成 (小时, 分钟)，解析失败返回默认值 */
    fun splitTime(text: String, defHour: Int, defMinute: Int): Pair<Int, Int> {
        val parts = text.split(":")
        if (parts.size != 2) return defHour to defMinute
        val h = parts[0].toIntOrNull() ?: return defHour to defMinute
        val m = parts[1].toIntOrNull() ?: return defHour to defMinute
        if (h !in 0..23 || m !in 0..59) return defHour to defMinute
        return h to m
    }

    private companion object {
        const val K_MORNING = "alarm_morning"
        const val K_MORNING_ON = "alarm_morning_on"
        const val K_DEPART = "alarm_depart"
        const val K_DEPART_ON = "alarm_depart_on"
        const val K_WRAP = "alarm_wrap"
        const val K_WRAP_ON = "alarm_wrap_on"
    }
}
