/*
 * Copyright 2016 Urban Airship and Contributors
 */

package com.urbanairship.cordova.gimbal;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.urbanairship.Logger;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * Helper class to parse the Urban Airship plugin config from the Cordova config.xml file.
 */
public class GimbalPluginConfig {

    private static final String GIMBAL_KEY = "com.urbanairship.gimbal_api_key";
    private static final String ANDROID_GIMBAL_KEY = "com.urbanairship.android_gimbal_api_key";
    private static final String GIMBAL_AUTO_START = "com.urbanairship.gimbal_auto_start";
    private static final String UA_PREFIX = "com.urbanairship";

    private Map<String, String> configValues = new HashMap<String, String>();
    private static GimbalPluginConfig instance;

    /**
     * Constructor for the GimbalPluginConfig.
     *
     * @param context The application context.
     */
    GimbalPluginConfig(Context context) {
        parseConfig(context);
    }

    /**
     * Gets the config for the Urban Airship plugin.
     *
     * @param context The application context.
     * @return The plugin config.
     */
    public static GimbalPluginConfig getInstance(Context context) {
        if (instance == null) {
            instance = new GimbalPluginConfig(context);
        }

        return instance;
    }

    /**
     * Returns the Gimbal key.
     *
     * @return The Gimbal key.
     */
    public String getGimbalKey() {
        return getString(ANDROID_GIMBAL_KEY, getString(GIMBAL_KEY, null));
    }

    /**
     * Returns the auto start config value.
     *
     * @return {@code true} if auto start is enabled, otherwise {@code false}.
     */
    public Boolean getAutoStart() {
        return getBoolean(GIMBAL_AUTO_START, true);
    }

    /**
     * Gets a String value from the config.
     *
     * @param key The config key.
     * @param defaultValue Default value if the key does not exist.
     * @return The value of the config, or default value.
     */
    private String getString(String key, String defaultValue) {
        return configValues.containsKey(key) ? configValues.get(key) : defaultValue;
    }

    /**
     * Gets a Boolean value from the config.
     *
     * @param key The config key.
     * @param defaultValue Default value if the key does not exist.
     * @return The value of the config, or default value.
     */
    private boolean getBoolean(String key, boolean defaultValue) {
        return configValues.containsKey(key) ?
                Boolean.parseBoolean(configValues.get(key)) : defaultValue;
    }

    /**
     * Parses the config.xml file.
     *
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
