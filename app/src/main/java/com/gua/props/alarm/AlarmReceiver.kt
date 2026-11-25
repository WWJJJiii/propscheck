package com.gua.props.alarm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.gua.props.MainActivity
import com.gua.props.db.DbHelper
import com.gua.props.db.PlanItemDao
import com.gua.props.util.DateUtil

/**
 * 到点被系统叫醒：弹一条通知，然后把明天的同一个闹钟排上。
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(ctx: Context, intent: Intent) {
        val code = intent.getIntExtra(AlarmScheduler.EXTRA_CODE, 0)
        if (code == 0) return

        val (title, body) = buildMessage(ctx, code)
        showNotification(ctx, code, title, body)

        // 一次性闹钟，响完排明天
        AlarmScheduler.rescheduleFromPrefs(ctx, code)
    }

    private fun buildMessage(ctx: Context, code: Int): Pair<String, String> {
        // 读一下今天还剩几项没勾（失败就退回通用文案）
        val remain = try {
            val dao = PlanItemDao(DbHelper.get(ctx))
            val (done, total) = dao.counts(DateUtil.today())
            total - done
        } catch (e: Exception) {
            -1
        }
        val tail = if (remain >= 0) "还有 $remain 项没勾" else ""

        return when (code) {
            AlarmScheduler.REQ_MORNING ->
                "今天的清单" to if (tail.isEmpty()) "打开看看今天要带什么" else tail

            AlarmScheduler.REQ_DEPART ->
                "出发前核对" to if (tail.isEmpty()) "确认都带齐了？" else "$tail，确认带齐了？"

            AlarmScheduler.REQ_WRAP ->
                "收工" to "东西都收回来了吗？"

            else -> "提醒" to ""
        }
    }

    private fun showNotification(ctx: Context, code: Int, title: String, body: String) {
        // Android 13+ 没有通知权限就直接算了，不崩
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val ok = ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!ok) return
        }

        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "道具核对提醒",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }

        val contentIntent = PendingIntent.getActivity(
            ctx,
            code,
            Intent(ctx, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val n = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        nm.notify(code, n)
    }

    private companion object {
        const val CHANNEL_ID = "props_alarm"
    }
}
