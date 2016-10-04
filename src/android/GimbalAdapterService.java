/*
 * Copyright 2015 Urban Airship and Contributors
 */
package com.urbanairship.cordova.gimbal;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gimbal.android.Gimbal;
import com.gimbal.android.PlaceEventListener;
import com.gimbal.android.PlaceManager;
import com.gimbal.android.Visit;
import com.urbanairship.UAirship;
import com.urbanairship.location.RegionEvent;
import com.urbanairship.util.DateUtils;

public class GimbalAdapterService extends Service {
	public static final String GIMBAL_KEY = "gimbalKey";
	public static final String INTENT_START = "com.urbanairship.cordova.gimbal.StartService";
	
    private static final String TAG = "GimbalAdapter ";
    private static final String SOURCE = "Gimbal";
	
	private PlaceEventListener placeEventListener;
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		placeEventListener = new PlaceEventListener() {
			@Override
			public void onVisitStart(Visit visit){
				Log.i(TAG, "Entered place: " + visit.getPlace().getName() + "Entrance date: " +
					DateUtils.createIso8601TimeStamp(visit.getArrivalTimeInMillis()));
				RegionEvent enter = new RegionEvent(visit.getPlace().getIdentifier(), SOURCE, RegionEvent.BOUNDARY_EVENT_ENTER);
				UAirship.shared().getAnalytics().addEvent(enter);
			}
			
			@Override
			public void onVisitEnd(Visit visit){
				Log.i(TAG, "Exited place: " + visit.getPlace().getName() + "Entrance date: " +
					DateUtils.createIso8601TimeStamp(visit.getArrivalTimeInMillis()) + "Exit date:" +
					DateUtils.createIso8601TimeStamp(visit.getDepartureTimeInMillis()));
				RegionEvent exit = new RegionEvent(visit.getPlace().getIdentifier(), SOURCE, RegionEvent.BOUNDARY_EVENT_EXIT);
				UAirship.shared().getAnalytics().addEvent(exit);
			}
		};
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		
		String action = null;
		String gimbalKey = PreferenceManager.getDefaultSharedPreferences(this).getString(GIMBAL_KEY, null);
		if (intent != null){
			action = intent.getAction();

			//setApiKey
			Bundle extras = intent.getExtras();
			if (extras != null){
				gimbalKey = (String) extras.get(GIMBAL_KEY);
				PreferenceManager.getDefaultSharedPreferences(this).edit().putString(GIMBAL_KEY, gimbalKey).commit();
			}
		}
		if (gimbalKey == null){
			throw new IllegalArgumentException("Missing Gimbal API key");
		}
		Gimbal.setApiKey(this.getApplication(), gimbalKey);
		
		//startMonitoring
		if (!Intent.ACTION_RUN.equals(action)){
			start();
		}

		return START_STICKY;
	}
	
	/**
	 * Starts tracking places.
	 */
	public void start(){
		PlaceManager placeManager = PlaceManager.getInstance();
		if (!placeManager.isMonitoring()){
			placeManager.addListener(placeEventListener);
			placeManager.startMonitoring();
			Log.i(TAG, "Adapter Started");
		}
	}

	/**
	 * Stops tracking places.
	 */
	public void stop(){
		PlaceManager placeManager = PlaceManager.getInstance();
		if (placeManager.isMonitoring()){
			placeManager.stopMonitoring();
			placeManager.removeListener(placeEventListener);
			placeEventListener = null;
			Log.i(TAG, "Adapter Stopped");
		}
	}

	@Override
	public void onDestroy(){
		stop();
		
		super.onDestroy();
		
		//Restart service
		sendBroadcast(new Intent(INTENT_START));
	}

	@Override
	public IBinder onBind(Intent intent){
		return null;
	}
}