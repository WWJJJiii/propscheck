package com.gua.props.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 日期只在两个形态之间转换：
 *   存库/查库： "2026-11-24"
 *   显示：      "11月24日 周一"
 *
 * minSdk = 26，所以可以放心用 java.time，不用 desugaring。
 */
object DateUtil {

    private val ISO: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun today(): String = LocalDate.now().format(ISO)

    fun isToday(date: String): Boolean = date == today()

    /** 前后挪几天，返回新的 "yyyy-MM-dd" */
    fun shift(date: String, days: Long): String =
        LocalDate.parse(date, ISO).plusDays(days).format(ISO)

    /** "2026-11-24" → "11月24日 周一" */
    fun title(date: String): String {
        val d = LocalDate.parse(date, ISO)
        return "${d.monthValue}月${d.dayOfMonth}日 ${weekdayCn(d.dayOfWeek)}"
    }

    /** 顶部第二行的小字：今天 / 明天 / 昨天 / N 天后 */
    fun relative(date: String): String {
        val diff = LocalDate.parse(date, ISO).toEpochDay() - LocalDate.now().toEpochDay()
        return when (diff) {
            0L -> "今天"
            1L -> "明天"
            -1L -> "昨天"
            else -> if (diff > 0) "$diff 天后" else "${-diff} 天前"
        }
    }

    private fun weekdayCn(w: DayOfWeek): String = when (w) {
        DayOfWeek.MONDAY -> "周一"
        DayOfWeek.TUESDAY -> "周二"
        DayOfWeek.WEDNESDAY -> "周三"
        DayOfWeek.THURSDAY -> "周四"
        DayOfWeek.FRIDAY -> "周五"
        DayOfWeek.SATURDAY -> "周六"
        DayOfWeek.SUNDAY -> "周日"
    }
}
