/*
 * Copyright 2015 Urban Airship and Contributors
 */
package com.urbanairship.cordova.gimbal;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import com.gimbal.android.Gimbal;

public class GimbalAdapterService extends Service {
	public static final String SERVICE_PARAM_GIMBAL_KEY = "gimbalKey";
	
	private static final int MSG_INTENT_RECEIVED = 1;
    private static final int MSG_INTENT_JOB_FINISHED = 2;
	
	private Looper serviceLooper;
	private ServiceHandler serviceHandler;
	
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper){
			super(looper);
		}
		@Override
		public void handleMessage(Message msg){
			int startId = msg.arg1;
			switch (msg.what){
				case MSG_INTENT_RECEIVED:
					GimbalAdapter.shared().start();
					break;
				case MSG_INTENT_JOB_FINISHED:
					GimbalAdapter.shared().stop();
					break;
			}
		}
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		
		serviceLooper = thread.getLooper();
		serviceHandler = new ServiceHandler(serviceLooper);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		
		String action = null;
		if (intent != null){
			action = intent.getAction();
			
			//setApiKey
			String gimbalKey = null;
			Bundle extras = intent.getExtras();
			if (extras != null){
				gimbalKey = (String) extras.get(SERVICE_PARAM_GIMBAL_KEY);
			}
			if (gimbalKey == null){
				throw new IllegalArgumentException("Missing Gimbal API key");
			}
			Gimbal.setApiKey(this.getApplication(), gimbalKey);
		}
		
		//startMonitoring
		if (!Intent.ACTION_RUN.equals(action)){
			Message msg = serviceHandler.obtainMessage(MSG_INTENT_RECEIVED);
			msg.arg1 = startId;
			msg.obj = intent;
			serviceHandler.sendMessage(msg);
		}
		
		return START_REDELIVER_INTENT;
	}
	
	@Override
	public void onDestroy(){
		Message msg = serviceHandler.obtainMessage(MSG_INTENT_JOB_FINISHED);
		serviceHandler.sendMessage(msg);
		
		serviceLooper.quit();
		
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent intent){
		return null;
	}
}