package at.bitfire.nophonespam

import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.provider.ContactsContract
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import at.bitfire.nophonespam.model.BlockedCall
import at.bitfire.nophonespam.model.DbHelper
import at.bitfire.nophonespam.model.Number

object BlockedCallHandler {
    private const val TAG = "NoPhoneSpam"
    private const val NOTIFY_REJECTED = 0
    const val NOTIFICATION_CHANNEL_ID = "blocked_calls"

    private fun activeTimeRangeFilter(now: Long): String =
        "((${Number.BLOCK_FROM} IS NULL AND ${Number.BLOCK_UNTIL} IS NULL) OR " +
        "((${Number.BLOCK_FROM} IS NULL OR $now >= ${Number.BLOCK_FROM}) AND " +
        "(${Number.BLOCK_UNTIL} IS NULL OR $now <= ${Number.BLOCK_UNTIL})))"

    fun queryAndUpdateDb(context: Context, incomingNumber: String): Number? {
        val now = System.currentTimeMillis()
        val dbHelper = DbHelper(context)
        try {
            val db = dbHelper.writableDatabase
            val c = db.query(
                Number._TABLE, null,
                "? LIKE ${Number.NUMBER} AND ${activeTimeRangeFilter(now)}",
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

    fun isNumberInContacts(context: Context, number: String): Boolean {
        val uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon()
            .appendPath(number).build()
        return context.contentResolver.query(
            uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null
        )?.use { it.moveToFirst() } == true
    }

    fun logNonContactBlock(context: Context, incomingNumber: String): Number? {
        val countryIndex = CountryCode.findByDialCode(incomingNumber)
        val pattern: String? = if (countryIndex > 0)
            "+${CountryCode.COUNTRIES[countryIndex].dialCode}%" else null

        val dbHelper = DbHelper(context)
        try {
            val db = dbHelper.writableDatabase
            val now = System.currentTimeMillis()

            if (pattern != null) {
                // Insert pattern row if it doesn't exist yet
                val insertValues = ContentValues().apply {
                    put(Number.NUMBER, pattern)
                    put(Number.TIMES_CALLED, 0)
                }
                db.insertWithOnConflict(Number._TABLE, null, insertValues, SQLiteDatabase.CONFLICT_IGNORE)

                // Increment call stats in-place (no read-modify-write race)
                db.execSQL(
                    "UPDATE ${Number._TABLE} SET ${Number.TIMES_CALLED}=${Number.TIMES_CALLED}+1, ${Number.LAST_CALL}=? WHERE ${Number.NUMBER}=?",
                    arrayOf(now, pattern)
                )

                val callValues = ContentValues().apply {
                    put(BlockedCall.MATCHED_PATTERN, pattern)
                    put(BlockedCall.INCOMING_NUMBER, incomingNumber)
                    put(BlockedCall.BLOCKED_AT, now)
                }
                db.insert(BlockedCall._TABLE, null, callValues)

                BlacklistObserver.notifyUpdated()

                val c = db.query(Number._TABLE, null, "${Number.NUMBER}=?", arrayOf(pattern), null, null, null)
                return c.use {
                    if (it.moveToFirst()) {
                        val values = ContentValues()
                        DatabaseUtils.cursorRowToContentValues(it, values)
                        Number.fromValues(values)
                    } else Number(number = pattern, timesCalled = 1, lastCall = now)
                }
            } else {
                val callValues = ContentValues().apply {
                    put(BlockedCall.MATCHED_PATTERN, "[not in contacts]")
                    put(BlockedCall.INCOMING_NUMBER, incomingNumber)
                    put(BlockedCall.BLOCKED_AT, now)
                }
                db.insert(BlockedCall._TABLE, null, callValues)
                return null
            }
        } finally {
            dbHelper.close()
        }
    }

    fun shouldBlock(context: Context, incomingNumber: String?): Boolean {
        val settings = Settings(context)
        if (incomingNumber.isNullOrEmpty()) {
            return settings.blockHiddenNumbers
        }
        val now = System.currentTimeMillis()
        val dbHelper = DbHelper(context)
        try {
            val db = dbHelper.readableDatabase
            val c = db.query(
                Number._TABLE, null,
                "? LIKE ${Number.NUMBER} AND ${activeTimeRangeFilter(now)}",
                arrayOf(incomingNumber),
                null, null, null
            )
            try {
                if (c.moveToNext()) return true
            } finally {
                c.close()
            }
        } finally {
            dbHelper.close()
        }
        if (settings.blockNonContacts && !isNumberInContacts(context, incomingNumber)) {
            return true
        }
        return false
    }

    fun showNotification(context: Context, number: Number?) {
        val settings = Settings(context)
        if (!settings.showNotifications) return

        NotificationManagerCompat.from(context).createNotificationChannel(
            NotificationChannelCompat.Builder(NOTIFICATION_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName(context.getString(R.string.notification_channel_name))
                .build()
        )

        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        val contentIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            pendingIntentFlags
        )

        val contentText = when {
            number?.name != null -> number.name
            number != null -> Number.wildcardsDbToView(number.number)
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
