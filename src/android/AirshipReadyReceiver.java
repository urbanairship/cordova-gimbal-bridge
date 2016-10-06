/*
 * Copyright 2016 Urban Airship and Contributors
 */

package com.urbanairship.cordova.gimbal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Broadcast receiver for Airship Ready events.
 */
public class AirshipReadyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        GimbalPlugin.initGimbal(context);
    }
}