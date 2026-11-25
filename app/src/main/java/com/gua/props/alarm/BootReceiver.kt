package com.gua.props.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 手机重启后，系统会把所有闹钟清掉，这里重新排一遍。
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            AlarmScheduler.scheduleAll(ctx)
        }
    }
}
