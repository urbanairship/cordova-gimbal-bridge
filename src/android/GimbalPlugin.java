/* Copyright Airship and Contributors */

package com.urbanairship.cordova.gimbal;

import android.util.Log;

import com.urbanairship.cordova.PluginLogger;
import com.urbanairship.gimbal.GimbalAdapter;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;


public class GimbalPlugin extends CordovaPlugin {

    static final String TAG = "UAGimbalPlugin";

    private static final String START_COMMAND = "start";
    private static final String STOP_COMMAND = "stop";

    private static final String PERMISSION_DENIED_ERROR = "permission denied";

    private boolean isStarted = false;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.i(TAG, "Initializing Urban Airship Gimbal cordova plugin.");

        if (GimbalPluginConfig.getInstance(cordova.getActivity()).getAutoStart()) {
            Log.i(TAG, "Auto starting Gimbal Adapter.");
            start(null);
        }
    }
    @Override
    public boolean execute(final String action, final JSONArray data, final CallbackContext callbackContext) {
        // Start
        if (START_COMMAND.equalsIgnoreCase(action)) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    start(callbackContext);
                }
            });
            return true;
        }

        // Stop
        if (STOP_COMMAND.equalsIgnoreCase(action)) {
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    stop(callbackContext);
                }
            });
            return true;
        }

        return false;
    }

    private String getGimbalKey() {
        return GimbalPluginConfig.getInstance(cordova.getActivity()).getGimbalKey();
    }

    private void stop(CallbackContext callbackContext) {
        isStarted = false;
        GimbalAdapter.shared(cordova.getActivity()).stop();
        callbackContext.success();
    }

    private void start(CallbackContext callbackContext) {
        isStarted = true;

        GimbalAdapter.shared(cordova.getActivity()).startWithPermissionPrompt(getGimbalKey(), new GimbalAdapter.PermissionResultCallback() {
            @Override
            public void onResult(boolean enabled) {
                PluginLogger.debug("Gimbal Plugin attempted to start with result: %s", enabled);
                isStarted = enabled;
            }
        });

        if (callbackContext == null) {
            return;
        }

        if (isStarted) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
        } else {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
        }
    }
}
