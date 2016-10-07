/*
 * Copyright 2016 Urban Airship and Contributors
 */

package com.urbanairship.cordova.gimbal;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.MainThread;
import android.util.Log;

import com.gimbal.android.Gimbal;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class GimbalPlugin extends CordovaPlugin {

    static final String TAG = "UAGimbalPlugin";

    private static final String START_COMMAND = "start";
    private static final String STOP_COMMAND = "stop";

    private static final int PERMISSION_REQUEST_CODE_LOCATION = 0;
    private static final String PERMISSION_DENIED_ERROR = "permission denied";

    private static boolean isGimbalInitialized = false;

    private List<CallbackContext> pendingStartCallbacks = new ArrayList<CallbackContext>();
    private boolean isStarted = false;
    private boolean isPermissionRequested = false;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.i(TAG, "Initializing Urban Airship Gimbal cordova plugin.");

        initGimbal(webView.getContext());

        if (GimbalPluginConfig.getInstance(cordova.getActivity()).getAutoStart()) {
            Log.i(TAG, "Auto starting Gimbal Adapter.");
            start(null);
        }
    }

    /**
     * Initializes the Gimbal adapter.
     *
     * @param context The application context.
     */
    @MainThread
    public static void initGimbal(Context context) {
        if (isGimbalInitialized) {
            return;
        }

        isGimbalInitialized = true;

        String key = GimbalPluginConfig.getInstance(context).getGimbalKey();
        if (key == null) {
            Log.e(GimbalPlugin.TAG, "Missing Gimbal api key.");
            return;
        }

        // Set the API key
        Gimbal.setApiKey((Application) context.getApplicationContext(), key);

        // Restore the adapter
        GimbalAdapter.shared(context).restore();
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

    private void stop(CallbackContext callbackContext) {
        isStarted = false;
        GimbalAdapter.shared(cordova.getActivity()).stop();
        callbackContext.success();
    }

    private void start(CallbackContext callbackContext) {
        isStarted = true;

        // Permission granted
        if (cordova.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            GimbalAdapter.shared(cordova.getActivity()).start();
            if (callbackContext != null) {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
            }

            return;
        }

        if (callbackContext != null) {
            pendingStartCallbacks.add(callbackContext);
        }

        if (!isPermissionRequested) {
            // Request permission
            String[] permissions = { Manifest.permission.ACCESS_FINE_LOCATION };
            cordova.requestPermissions(this, PERMISSION_REQUEST_CODE_LOCATION, permissions);
        }
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, final int[] grantResults) throws JSONException {
        if (requestCode != PERMISSION_REQUEST_CODE_LOCATION) {
            return;
        }

        isPermissionRequested = false;

        boolean isPermissionGranted = true;

        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                isPermissionGranted = false;
                break;
            }
        }

        for (CallbackContext callbackContext : pendingStartCallbacks) {
            if (isPermissionGranted) {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
            } else {
                callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
            }
        }

        pendingStartCallbacks.clear();

        if (isPermissionGranted && isStarted) {
            GimbalAdapter.shared(cordova.getActivity()).start();
        }
    }
}

