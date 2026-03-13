package at.bitfire.nophonespam

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object BlockScheduler {

    fun scheduleExpiry(context: Context, pattern: String, blockUntil: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = buildPendingIntent(context, pattern) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, blockUntil, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, blockUntil, pi)
            }
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, blockUntil, pi)
        }
    }

    fun cancelExpiry(context: Context, pattern: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            context,
            pattern.hashCode(),
            Intent(context, BlockExpiryReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        am.cancel(pi)
        pi.cancel()
    }

    private fun buildPendingIntent(context: Context, pattern: String): PendingIntent? {
        val intent = Intent(context, BlockExpiryReceiver::class.java).apply {
            putExtra(BlockExpiryReceiver.EXTRA_PATTERN, pattern)
        }
        return PendingIntent.getBroadcast(
            context,
            pattern.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
