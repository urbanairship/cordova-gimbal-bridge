package com.urbanairship.cordova.gimbal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GimbalAdapterReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent){
		Boolean startService = false;
		String action = intent.getAction();
		if (GimbalAdapterService.INTENT_START.equals(action)){
			startService = true;
		}
		if (Intent.ACTION_BOOT_COMPLETED.equals(action)){
			startService = true;
		}
		if (startService){
			context.startService(new Intent(context, GimbalAdapterService.class));
		}
	}
}