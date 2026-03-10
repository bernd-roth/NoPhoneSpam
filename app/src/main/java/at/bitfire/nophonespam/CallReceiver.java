/*
 * Copyright © Ricki Hirner (bitfire web engineering).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.bitfire.nophonespam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

import at.bitfire.nophonespam.model.Number;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "NoPhoneSpam";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction()) &&
                intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            Log.i(TAG, "Received call: " + incomingNumber);

            if (TextUtils.isEmpty(incomingNumber)) {
                Settings settings = new Settings(context);
                if (settings.blockHiddenNumbers()) {
                    endCall(context);
                    BlockedCallHandler.showNotification(context, null);
                }
            } else {
                Number number = BlockedCallHandler.queryAndUpdateDb(context, incomingNumber);
                if (number != null) {
                    endCall(context);
                    BlockedCallHandler.showNotification(context, number);
                }
            }
        }
    }

    private void endCall(@NonNull Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Class c = Class.forName(tm.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);

            ITelephony telephony = (ITelephony) m.invoke(tm);
            telephony.endCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
