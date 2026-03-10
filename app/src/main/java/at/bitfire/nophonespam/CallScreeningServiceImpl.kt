package at.bitfire.nophonespam

import android.annotation.TargetApi
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log

@TargetApi(24)
class CallScreeningServiceImpl : CallScreeningService() {
    private val TAG = "NoPhoneSpam"

    override fun onScreenCall(callDetails: Call.Details) {
        var incomingNumber: String? = null
        val handle = callDetails.handle
        if (handle != null && "tel" == handle.scheme) {
            incomingNumber = handle.schemeSpecificPart
        }

        Log.i(TAG, "Screening call: $incomingNumber")

        var block = false
        var matchedNumber: at.bitfire.nophonespam.model.Number? = null

        if (incomingNumber.isNullOrEmpty()) {
            val settings = Settings(this)
            if (settings.blockHiddenNumbers) {
                block = true
            }
        } else {
            matchedNumber = BlockedCallHandler.queryAndUpdateDb(this, incomingNumber)
            if (matchedNumber != null) {
                block = true
            }
        }

        if (block) {
            BlockedCallHandler.showNotification(this, matchedNumber)
            val response = CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipNotification(true)
                .build()
            respondToCall(callDetails, response)
        } else {
            val response = CallResponse.Builder().build()
            respondToCall(callDetails, response)
        }
    }
}
