/*
 * Copyright 2016 Urban Airship and Contributors
 */

package com.urbanairship.cordova.gimbal;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.gimbal.android.PlaceEventListener;
import com.gimbal.android.PlaceManager;
import com.gimbal.android.Visit;
import com.urbanairship.UAirship;
import com.urbanairship.location.RegionEvent;
import com.urbanairship.util.DateUtils;

/**
 * GimbalAdapter interfaces Gimbal SDK functionality with Urban Airship services.
 */
public class GimbalAdapter {

    /**
     * GimbalAdapter logging tag.
     */
    private static final String TAG = "GimbalAdapter";

    private static final String PREFERENCE_NAME = "com.urbanairhsip.gimbal.preferences";
    private static final String STARTED_PREFERENCE = "com.urbanairhsip.gimbal.started";

    /**
     * GimbalAdapter shared instance.
     */
    private static GimbalAdapter instance;

    /**
     * Analytics event source.
     */
    private static final String SOURCE = "Gimbal";
    private final SharedPreferences preferences;
    private final Context context;

    /**
     * Boolean representing the started state of the GimbalAdapter.
     */
    private boolean isStarted = false;

    /**
     * Listener for Gimbal place events. Creates an analytics event
     * corresponding to boundary event type.
     */
    private PlaceEventListener placeEventListener = new PlaceEventListener() {
        @Override
        public void onVisitStart(final Visit visit) {
            Log.i(TAG, "Entered place: " + visit.getPlace().getName() + "Entrance date: " +
                    DateUtils.createIso8601TimeStamp(visit.getArrivalTimeInMillis()));

            UAirship.shared(new UAirship.OnReadyCallback() {
                @Override
                public void onAirshipReady(UAirship airship) {
                    RegionEvent enter = new RegionEvent(visit.getPlace().getIdentifier(), SOURCE, RegionEvent.BOUNDARY_EVENT_ENTER);
                    airship.getAnalytics().addEvent(enter);
                }
            });
        }

        @Override
        public void onVisitEnd(final Visit visit) {
            Log.i(TAG, "Exited place: " + visit.getPlace().getName() + "Entrance date: " +
                    DateUtils.createIso8601TimeStamp(visit.getArrivalTimeInMillis()) + "Exit date:" +
                    DateUtils.createIso8601TimeStamp(visit.getDepartureTimeInMillis()));

            UAirship.shared(new UAirship.OnReadyCallback() {
                @Override
                public void onAirshipReady(UAirship airship) {
                    RegionEvent exit = new RegionEvent(visit.getPlace().getIdentifier(), SOURCE, RegionEvent.BOUNDARY_EVENT_EXIT);
                    airship.getAnalytics().addEvent(exit);
                }
            });
        }
    };

    /**
     * Hidden to support the singleton pattern.
     *
     * @param context The application context.
     */
    GimbalAdapter(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * GimbalAdapter shared instance.
     */
    public synchronized static GimbalAdapter shared(Context context) {
        if (instance == null) {
            instance = new GimbalAdapter(context.getApplicationContext());
        }

        return instance;
    }

    /**
     * Restores the last run state. If previously started it will start listening, otherwise
     * it will stop listening. Should be called when the application starts up.
     */
    public void restore() {
        if (this.preferences.getBoolean(STARTED_PREFERENCE, false)) {
            start();
        } else {
            stop();
        }
    }

    /**
     * Starts tracking places.
     */
    public void start() {
        if (isStarted) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                Log.e(TAG, "Unable to start adapter, permission denied.");
                return;
            }
        }

        isStarted = true;

        PlaceManager.getInstance().addListener(placeEventListener);
        PlaceManager.getInstance().startMonitoring();
        Log.i(TAG, "Adapter Started");

        preferences.edit().putBoolean(STARTED_PREFERENCE, true).apply();
    }

    /**
     * Stops tracking places.
     */
    public void stop() {
        if (!isStarted) {
            return;
        }

        isStarted = false;

        PlaceManager.getInstance().stopMonitoring();
        PlaceManager.getInstance().removeListener(placeEventListener);

        Log.i(TAG, "Adapter Stopped");

        preferences.edit().putBoolean(STARTED_PREFERENCE, false).apply();
    }

}