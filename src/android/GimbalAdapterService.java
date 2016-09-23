/*
 * Copyright 2015 Urban Airship and Contributors
 */
package com.urbanairship.cordova.gimbal;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.gimbal.android.Gimbal;

public class GimbalAdapterService extends Service {
	public static final String SERVICE_PARAM_GIMBAL_KEY = "gimbalKey";
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		
		String gimbalKey = null;
		if (intent != null){
			Bundle extras = intent.getExtras();
			if (extras != null){
				gimbalKey = (String) extras.get(SERVICE_PARAM_GIMBAL_KEY);
			}
		}
		if (gimbalKey == null){
			throw new IllegalArgumentException("Missing Gimbal API key");
		}
		
		Gimbal.setApiKey(this.getApplication(), gimbalKey);
		GimbalAdapter.shared().start();
		
		return START_REDELIVER_INTENT;
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