/*
 Copyright 2009-2015 Urban Airship Inc. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 
 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.
 
 THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.urbanairship.cordova.gimbal;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Application;
import android.app.Activity;
import android.Manifest;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.content.pm.PackageManager;
import android.content.Intent;

import com.urbanairship.Logger;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GimbalPlugin extends CordovaPlugin {
	
	public static final String SERVICE_PARAM_GIMBAL_KEY = "gimbalKey";
	
	private static final String GIMBAL_KEY = "com.urbanairship.gimbal_api_key";
	private static final String GIMBAL_AUTO_START = "com.urbanairship.gimbal_auto_start";
	private static final String UA_PREFIX = "com.urbanairship";
	
	private static final int PERMISSION_REQUEST_CODE_LOCATION = 0;
	private static final String PERMISSION_DENIED_ERROR = "permission denied";
	
	/**
     * List of Cordova "actions". To extend the plugin, add the action below and then define the method
     * with the signature `void <CORDOVA_ACTION>(JSONArray data, final CallbackContext callbackContext)`
     * and it will automatically be called. All methods will be executed in the ExecutorService. Any
     * exceptions thrown by the actions are automatically caught and the callbackContext will return
     * an error result.
     */
    private static final List<String> KNOWN_ACTIONS = Arrays.asList("start", "stop");
    
	private ExecutorService executorService = Executors.newFixedThreadPool(1);
	private PluginConfig pluginConfig;
	private CallbackContext callbackContext = null;
	
	private String gimbalKey;
	
	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		
		Application application = cordova.getActivity().getApplication();
		pluginConfig = getPluginConfig(application);
		
		gimbalKey = pluginConfig.getString(GIMBAL_KEY, "");
		if (gimbalKey.equals("")){
			Logger.error("No Gimbal API key found, Gimbal cordova plugin initialization failed.");
			return;
		}
		Logger.info("Initializing Urban Airship Gimbal cordova plugin.");
		
		//Start service and setApiKey without monitoring
		//THE SERVICE MUST BE STARTED WITH THE API KEY SET BEFORE USER ACCEPTS PERMISSIONS!
		doStart(Intent.ACTION_RUN);
		
		//Auto-start
		if (pluginConfig.getBoolean(GIMBAL_AUTO_START, true)){
			start();
		}
	}
	
	@Override
    public boolean execute(final String action, final JSONArray data, final CallbackContext callbackContext){
        if (!KNOWN_ACTIONS.contains(action)) {
            Logger.debug("Invalid action: " + action);
            return false;
        }
        
        executorService.execute(new Runnable(){
            @Override
            public void run() {
                try {
                    Logger.debug("Plugin Execute: " + action);
                    Method method = GimbalPlugin.class.getDeclaredMethod(action, JSONArray.class, CallbackContext.class);
                    method.invoke(GimbalPlugin.this, data, callbackContext);
                } catch (Exception e) {
                    Logger.error("Action failed to execute: " + action, e);
                    callbackContext.error("Action " + action + " failed with exception: " + e.getMessage());
                }
            }
        });

        return true;
    }
    
    public void start(JSONArray data, CallbackContext callbackContext){
		this.callbackContext = callbackContext;
		start();
    }
    public void start(){
		//Android M permissions
		if (cordova.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)){
			doStart();
		} else {
			String[] permissions = {
				Manifest.permission.ACCESS_COARSE_LOCATION,
				Manifest.permission.ACCESS_FINE_LOCATION
			};
			cordova.requestPermissions(this, PERMISSION_REQUEST_CODE_LOCATION, permissions);
		}
    }
    
    private void doStart(){
		doStart(null);
	}
    private void doStart(String action){
		Activity activity = cordova.getActivity();
		Intent serviceIntent = new Intent(activity, GimbalAdapterService.class);
		if (action != null){
			serviceIntent.setAction(action);
		}
		serviceIntent.putExtra(SERVICE_PARAM_GIMBAL_KEY, gimbalKey);
		activity.startService(serviceIntent);
		
		if (callbackContext != null){
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
			callbackContext = null;
		}
    }
    
    public void stop(JSONArray data, CallbackContext callbackContext){
		stop();
		callbackContext.success();
    }
    public void stop(){
		Activity activity = cordova.getActivity();
		activity.stopService(new Intent(activity, GimbalAdapterService.class));
    }
    
    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
		for(int r:grantResults){
			if(r == PackageManager.PERMISSION_DENIED){
				if (callbackContext != null){
					callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
					callbackContext = null;					
				}
				return;
			}
		}
		switch(requestCode){
			case PERMISSION_REQUEST_CODE_LOCATION:
				try {
					doStart();
				} catch (Exception e) {
                    if (callbackContext != null){
						callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
						callbackContext = null;
					}
                }
				break;
		}
	}
	
	/**
     * Gets the config for the Urban Airship plugin.
     *
     * @param context The application context.
     * @return The plugin config.
     */
    private PluginConfig getPluginConfig(Context context) {
        if (pluginConfig == null) {
            pluginConfig = new PluginConfig(context);
        }

        return pluginConfig;
    }
	
	/**
     * Helper class to parse the Urban Airship plugin config from the Cordova config.xml file.
     */
    class PluginConfig {
        private Map<String, String> configValues = new HashMap<String, String>();

        /**
         * Constructor for the PluginConfig.
         * @param context The application context.
         */
        PluginConfig(Context context) {
            parseConfig(context);
        }

        /**
         * Gets a String value from the config.
         *
         * @param key The config key.
         * @param defaultValue Default value if the key does not exist.
         * @return The value of the config, or default value.
         */
        String getString(String key, String defaultValue) {
            return configValues.containsKey(key) ? configValues.get(key) : defaultValue;
        }

        /**
         * Gets a Boolean value from the config.
         *
         * @param key The config key.
         * @param defaultValue Default value if the key does not exist.
         * @return The value of the config, or default value.
         */
        boolean getBoolean(String key, boolean defaultValue) {
            return configValues.containsKey(key) ?
                   Boolean.parseBoolean(configValues.get(key)) : defaultValue;
        }

        /**
         * Parses the config.xml file.
         * @param context The application context.
         */
        private void parseConfig(Context context) {
            int id = context.getResources().getIdentifier("config", "xml", context.getPackageName());
            if (id == 0) {
                return;
            }

            XmlResourceParser xml = context.getResources().getXml(id);

            int eventType = -1;
            while (eventType != XmlResourceParser.END_DOCUMENT) {

                if (eventType == XmlResourceParser.START_TAG) {
                    if (xml.getName().equals("preference")) {
                        String name = xml.getAttributeValue(null, "name").toLowerCase(Locale.US);
                        String value = xml.getAttributeValue(null, "value");

                        if (name.startsWith(UA_PREFIX) && value != null) {
                            configValues.put(name, value);
                            Logger.verbose("Found " + name + " in config.xml with value: " + value);
                        }
                    }
                }

                try {
                    eventType = xml.next();
                } catch (Exception e) {
                    Logger.error("Error parsing config file", e);
                }
            }
        }
    }
}