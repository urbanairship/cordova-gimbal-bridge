# Urban Airship Gimbal PhoneGap/Cordova Plugin

Requirements:
 - cordova 9.0.0+, and cordova-ios 5.0.1+, urbanairship-cordova plugin 8.0.0+

## Installation

1. Install this plugin using PhoneGap/Cordova CLI:

```xml		
	cordova plugin add urbanairship-gimbal-bridge-cordova
```

2. Add your Gimbal API key to the config.xml file:

```xml		
	<preference name="com.urbanairship.gimbal_api_key" value="Your Gimbal API Key" />
```

You can also use `com.urbanairship.android_gimbal_api_key` and `com.urbanairship.ios_gimbal_api_key` if you have one gimbal API key for each platform.


## Permissions

This plugin requires location access for both Android and iOS. Once Gimbal is started, the plugin will prompt the user for permission. By default the plugin starts as soon as the app is opened. You have the option to disable auto-start in favor of calling start manually. This gives you control over when the permission dialog is displayed. To disable auto-start add this preference to the config.xml file:

```xml
	<preference name="com.urbanairship.gimbal_auto_start" value="false" />
```

The JavaScript API exposes a start and stop method.
To start the plugin via JavaScript do the following:

```javascript
	$ionicPlatform.ready(function(){
		window.UAGimbal.start(
			function(result){
				//Called on success
			},
			function(error){
				//Called on error such as permissions denied
			}
		);
	});
```

## Contributing Code

We accept pull requests! If you would like to submit a pull request, please fill out and submit a
[Code Contribution Agreement](http://docs.urbanairship.com/contribution-agreement.html).

## Issues

Please contact support@urbanairship.com for any issues integrating or using this plugin.
