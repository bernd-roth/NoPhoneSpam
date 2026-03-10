package at.bitfire.nophonespam

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.DatabaseUtils
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import at.bitfire.nophonespam.model.BlockedCall
import at.bitfire.nophonespam.model.DbHelper
import at.bitfire.nophonespam.model.Number

object BlockedCallHandler {
    private const val TAG = "NoPhoneSpam"
    private const val NOTIFY_REJECTED = 0
    const val NOTIFICATION_CHANNEL_ID = "blocked_calls"

    fun queryAndUpdateDb(context: Context, incomingNumber: String): Number? {
        val dbHelper = DbHelper(context)
        try {
            val db = dbHelper.writableDatabase
            val c = db.query(
                Number._TABLE, null,
                "? LIKE ${Number.NUMBER}",
                arrayOf(incomingNumber),
                null, null, null
            )
            try {
                if (c.moveToNext()) {
                    val values = ContentValues()
                    DatabaseUtils.cursorRowToContentValues(c, values)
                    val number = Number.fromValues(values)

                    val now = System.currentTimeMillis()

                    val updateValues = ContentValues().apply {
                        put(Number.LAST_CALL, now)
                        put(Number.TIMES_CALLED, number.timesCalled + 1)
                    }
                    db.update(Number._TABLE, updateValues, "${Number.NUMBER}=?", arrayOf(number.number))

                    val callValues = ContentValues().apply {
                        put(BlockedCall.MATCHED_PATTERN, number.number)
                        put(BlockedCall.INCOMING_NUMBER, incomingNumber)
                        put(BlockedCall.BLOCKED_AT, now)
                    }
                    db.insert(BlockedCall._TABLE, null, callValues)

                    BlacklistObserver.notifyUpdated()
                    return number
                }
            } finally {
                c.close()
            }
        } finally {
            dbHelper.close()
        }
        return null
    }

    fun shouldBlock(context: Context, incomingNumber: String?): Boolean {
        if (incomingNumber.isNullOrEmpty()) {
            return Settings(context).blockHiddenNumbers
        }
        val dbHelper = DbHelper(context)
        try {
            val db = dbHelper.readableDatabase
            val c = db.query(
                Number._TABLE, null,
                "? LIKE ${Number.NUMBER}",
                arrayOf(incomingNumber),
                null, null, null
            )
            try {
                return c.moveToNext()
            } finally {
                c.close()
            }
        } finally {
            dbHelper.close()
        }
    }

    fun showNotification(context: Context, number: Number?) {
        val settings = Settings(context)
        if (!settings.showNotifications) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }

        var pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntentFlags = pendingIntentFlags or PendingIntent.FLAG_IMMUTABLE
        }

        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            pendingIntentFlags
        )

        val contentText = when {
            number?.name != null -> number.name
            number != null -> number.number
            else -> context.getString(R.string.receiver_notify_private_number)
        }

        val notify = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.receiver_notify_call_rejected))
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setShowWhen(true)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .apply {
                if (number != null) addPerson("tel:${number.number}")
            }
            .setGroup("rejected")
            .build()

        val tag = number?.number ?: "private"
        try {
            NotificationManagerCompat.from(context).notify(tag, NOTIFY_REJECTED, notify)
        } catch (e: SecurityException) {
            Log.w(TAG, "Cannot show notification (permission not granted)", e)
        }
    }
}
