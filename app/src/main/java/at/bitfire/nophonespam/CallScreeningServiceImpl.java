/*
 * Copyright © Ricki Hirner (bitfire web engineering).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.nophonespam;

import android.annotation.TargetApi;
import android.net.Uri;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.text.TextUtils;
import android.util.Log;

import at.bitfire.nophonespam.model.Number;

@TargetApi(24)
public class CallScreeningServiceImpl extends CallScreeningService {
    private static final String TAG = "NoPhoneSpam";

    @Override
    public void onScreenCall(Call.Details callDetails) {
        String incomingNumber = null;
        Uri handle = callDetails.getHandle();
        if (handle != null && "tel".equals(handle.getScheme())) {
            incomingNumber = handle.getSchemeSpecificPart();
        }

        Log.i(TAG, "Screening call: " + incomingNumber);

        boolean block = false;
        Number matchedNumber = null;

        if (TextUtils.isEmpty(incomingNumber)) {
            Settings settings = new Settings(this);
            if (settings.blockHiddenNumbers()) {
                block = true;
            }
        } else {
            matchedNumber = BlockedCallHandler.queryAndUpdateDb(this, incomingNumber);
            if (matchedNumber != null) {
                block = true;
            }
        }

        if (block) {
            BlockedCallHandler.showNotification(this, matchedNumber);

            CallResponse response = new CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipNotification(true)
                    .build();
            respondToCall(callDetails, response);
        } else {
            CallResponse response = new CallResponse.Builder().build();
            respondToCall(callDetails, response);
        }
    }
}
