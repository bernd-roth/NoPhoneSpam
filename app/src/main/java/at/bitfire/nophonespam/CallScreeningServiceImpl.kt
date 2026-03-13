package at.bitfire.nophonespam

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log

class CallScreeningServiceImpl : CallScreeningService() {
    private val TAG = "NoPhoneSpam"

    override fun onScreenCall(callDetails: Call.Details) {
        // API 29+: onScreenCall is also invoked for outgoing calls — pass them through immediately
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            callDetails.callDirection != Call.Details.DIRECTION_INCOMING) {
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

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

        if (!block && !incomingNumber.isNullOrEmpty()) {
            val settings = Settings(this)
            if (settings.blockNonContacts && !BlockedCallHandler.isNumberInContacts(this, incomingNumber)) {
                block = true
                matchedNumber = BlockedCallHandler.logNonContactBlock(this, incomingNumber)
                    ?: at.bitfire.nophonespam.model.Number(number = incomingNumber)
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
