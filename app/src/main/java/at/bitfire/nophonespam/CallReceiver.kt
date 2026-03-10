package at.bitfire.nophonespam

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

class CallReceiver : BroadcastReceiver() {
    private val TAG = "NoPhoneSpam"

    override fun onReceive(context: Context, intent: Intent) {
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED == intent.action &&
            intent.getStringExtra(TelephonyManager.EXTRA_STATE) == TelephonyManager.EXTRA_STATE_RINGING
        ) {
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            Log.i(TAG, "Received call: $incomingNumber")

            if (incomingNumber.isNullOrEmpty()) {
                val settings = Settings(context)
                if (settings.blockHiddenNumbers) {
                    endCall(context)
                    BlockedCallHandler.showNotification(context, null)
                }
            } else {
                val number = BlockedCallHandler.queryAndUpdateDb(context, incomingNumber)
                if (number != null) {
                    endCall(context)
                    BlockedCallHandler.showNotification(context, number)
                }
            }
        }
    }

    private fun endCall(context: Context) {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        try {
            val getITelephony = tm.javaClass.getDeclaredMethod("getITelephony")
            getITelephony.isAccessible = true
            val telephony = getITelephony.invoke(tm)
            val endCall = telephony!!.javaClass.getDeclaredMethod("endCall")
            endCall.invoke(telephony)
        } catch (e: Exception) {
            Log.w(TAG, "endCall via reflection failed", e)
        }
    }
}
