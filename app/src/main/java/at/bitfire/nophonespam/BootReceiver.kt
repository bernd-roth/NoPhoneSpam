package at.bitfire.nophonespam

import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContentValues
import android.content.Intent
import android.database.DatabaseUtils
import at.bitfire.nophonespam.model.DbHelper
import at.bitfire.nophonespam.model.Number

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val now = System.currentTimeMillis()
        val dbHelper = DbHelper(context)
        try {
            val db = dbHelper.writableDatabase

            // Remove entries whose block period has already expired
            db.delete(
                Number._TABLE,
                "${Number.BLOCK_UNTIL} IS NOT NULL AND ${Number.BLOCK_UNTIL} < ?",
                arrayOf(now.toString())
            )

            // Re-schedule alarms for entries still within their block period
            val c = db.query(
                Number._TABLE, null,
                "${Number.BLOCK_UNTIL} IS NOT NULL AND ${Number.BLOCK_UNTIL} >= ?",
                arrayOf(now.toString()),
                null, null, null
            )
            c.use {
                while (it.moveToNext()) {
                    val values = ContentValues()
                    DatabaseUtils.cursorRowToContentValues(it, values)
                    val number = Number.fromValues(values)
                    number.blockUntil?.let { until ->
                        BlockScheduler.scheduleExpiry(context, number.number, until)
                    }
                }
            }

            BlacklistObserver.notifyUpdated()
        } finally {
            dbHelper.close()
        }
    }
}
