package at.bitfire.nophonespam

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import at.bitfire.nophonespam.model.DbHelper
import at.bitfire.nophonespam.model.Number

class BlockExpiryReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_PATTERN = "pattern"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pattern = intent.getStringExtra(EXTRA_PATTERN) ?: return
        val dbHelper = DbHelper(context)
        try {
            val db = dbHelper.writableDatabase
            db.delete(Number._TABLE, "${Number.NUMBER}=? AND ${Number.BLOCK_UNTIL} IS NOT NULL", arrayOf(pattern))
            BlacklistObserver.notifyUpdated()
        } finally {
            dbHelper.close()
        }
    }
}
