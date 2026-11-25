package com.gua.props.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.gua.props.util.Prefs
import java.util.Calendar

/**
 * 三个闹钟的排期。全部是"一次性 + 响完自动排明天"，
 * 不用 setRepeating（那个在 Doze 下不准）。
 */
object AlarmScheduler {

    const val REQ_MORNING = 1001
    const val REQ_DEPART = 1002
    const val REQ_WRAP = 1003

    const val EXTRA_CODE = "extra_code"

    /** 重新排全部（设置改时间、开机、启动时都调这个） */
    fun scheduleAll(ctx: Context) {
        val p = Prefs(ctx)
        cancel(ctx, REQ_MORNING)
        cancel(ctx, REQ_DEPART)
        cancel(ctx, REQ_WRAP)

        if (p.morningOn) schedule(ctx, REQ_MORNING, p.morningTime)
        if (p.departOn) schedule(ctx, REQ_DEPART, p.departTime)
        if (p.wrapOn) schedule(ctx, REQ_WRAP, p.wrapTime)
    }

    /** 响完以后排下一次 */
    fun rescheduleFromPrefs(ctx: Context, code: Int) {
        val p = Prefs(ctx)
        when (code) {
            REQ_MORNING -> if (p.morningOn) schedule(ctx, code, p.morningTime)
            REQ_DEPART -> if (p.departOn) schedule(ctx, code, p.departTime)
            REQ_WRAP -> if (p.wrapOn) schedule(ctx, code, p.wrapTime)
        }
    }

    fun schedule(ctx: Context, code: Int, time: String) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(ctx, code)
        val triggerAt = nextTrigger(time)

        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                am.canScheduleExactAlarms()

        if (canExact) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            // 没有精确闹钟权限就退一步，允许晚几分钟
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun cancel(ctx: Context, code: Int) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(ctx, code))
    }

    /** "07:30" → 今天 07:30；已经过了就明天 07:30 */
    private fun nextTrigger(time: String): Long {
        val parts = time.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 7
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 30

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    private fun pendingIntent(ctx: Context, code: Int): PendingIntent {
        val intent = Intent(ctx, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_CODE, code)
        }
        return PendingIntent.getBroadcast(
            ctx,
            code,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
