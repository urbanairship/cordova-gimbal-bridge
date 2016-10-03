/*
 * Copyright 2015 Urban Airship and Contributors
 */
package com.urbanairship.cordova.gimbal;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.gimbal.android.Gimbal;

public class GimbalAdapterService extends Service {
	public static final String SERVICE_PARAM_GIMBAL_KEY = "gimbalKey";
	
	@Override
	public void onCreate(){
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		
		String action = null;
		String gimbalKey = PreferenceManager.getDefaultSharedPreferences(this).getString(SERVICE_PARAM_GIMBAL_KEY, null);
		if (intent != null){
			action = intent.getAction();
			
			//setApiKey
			Bundle extras = intent.getExtras();
			if (extras != null){
				gimbalKey = (String) extras.get(SERVICE_PARAM_GIMBAL_KEY);
				PreferenceManager.getDefaultSharedPreferences(this).edit().putString(SERVICE_PARAM_GIMBAL_KEY, gimbalKey).commit(); 
			}
		}
		if (gimbalKey == null){
			throw new IllegalArgumentException("Missing Gimbal API key");
		}
		Gimbal.setApiKey(this.getApplication(), gimbalKey);
		
		//startMonitoring
		if (!Intent.ACTION_RUN.equals(action)){
			GimbalAdapter.shared().start();
		}
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy(){
		GimbalAdapter.shared().stop();
		
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent intent){
		return null;
	}
}