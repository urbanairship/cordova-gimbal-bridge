# Urban Airship Gimbal PhoneGap/Cordova Plugin

Requirements:
 - Cordova 5.4.0+, and Cordova iOS 3.9.0+, urbanairship-cordova plugin

## Installation

1. Install this plugin using PhoneGap/Cordova CLI:

```xml		
        cordova plugin add urbanairship-gimbal-bridge-cordova
```

2. Add your Gimbal API key to the config.xml file:

```xml		
        <preference name="com.urbanairship.gimbal_api_key" value="Your Gimbal API Key" />
```

#Permissions

		This plugin requires location access and supports the Android M permission model. The plugin will prompt for permission when it starts. By default the plugin starts as soon as the app is opened. You have the option to disable auto-start in favor of calling start manually. This gives you control over when the permission dialog is displayed.
		To disable auto-start add this preference to the config.xml file:

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
