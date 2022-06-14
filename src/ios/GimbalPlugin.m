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

#import <Foundation/Foundation.h>
#import "GimbalPlugin.h"
#import "GimbalAdapter.h"
#import <Cordova/CDVPlugin.h>
#import <CoreLocation/CoreLocation.h>

typedef void (^UACordovaCompletionHandler)(CDVCommandStatus, id);
typedef void (^UACordovaExecutionBlock)(NSArray *args, UACordovaCompletionHandler completionHandler);

@implementation GimbalPlugin

// Config keys
NSString *const GimbalAPIKey = @"com.urbanairship.gimbal_api_key";
NSString *const IOSGimbalAPIKey = @"com.urbanairship.ios_gimbal_api_key";
NSString *const GimbalAutoStartKey = @"com.urbanairship.gimbal_auto_start";

- (void)pluginInitialize {
    NSDictionary *settings = self.commandDelegate.settings;

    NSString *gimbalKey = settings[IOSGimbalAPIKey];

    if (!gimbalKey) {
        if (!settings[GimbalAPIKey]) {
            NSLog(@"No Gimbal API key found, Gimbal cordova plugin initialization failed.");
            return;
        }
        gimbalKey = settings[GimbalAPIKey];
    }

    // Grab the gimbal api key, start the adapter
    NSLog(@"GIMBAL: setting API key");
    [Gimbal setAPIKey:gimbalKey options:nil];

    if (settings[GimbalAutoStartKey] == nil || [settings[GimbalAutoStartKey] boolValue]) {
        NSLog(@"GIMBAL: auto start gimbal adapter");
        [[GimbalAdapter shared] startAdapter];
    }
}

- (void)performCallbackWithCommand:(CDVInvokedUrlCommand *)command withBlock:(UACordovaExecutionBlock)block {
    [self.commandDelegate runInBackground:^{
        UACordovaCompletionHandler completionHandler = ^(CDVCommandStatus status, id value) {
            CDVPluginResult *result = [self pluginResultForValue:value status:status];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        };

        if (!block) {
            completionHandler(CDVCommandStatus_OK, nil);
        } else {
            block(command.arguments, completionHandler);
        }
    }];
}

- (CDVPluginResult *)pluginResultForValue:(id)value status:(CDVCommandStatus)status{

    // String
    if ([value isKindOfClass:[NSString class]]) {
        return [CDVPluginResult resultWithStatus:status
                                 messageAsString:[value stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    }

    // Number
    if ([value isKindOfClass:[NSNumber class]]) {
        CFNumberType numberType = CFNumberGetType((CFNumberRef)value);
        //note: underlyingly, BOOL values are typedefed as char
        if (numberType == kCFNumberIntType || numberType == kCFNumberCharType) {
            return [CDVPluginResult resultWithStatus:status messageAsInt:[value intValue]];
        } else  {
            return [CDVPluginResult resultWithStatus:status messageAsDouble:[value doubleValue]];
        }
    }

    // Array
    if ([value isKindOfClass:[NSArray class]]) {
        return [CDVPluginResult resultWithStatus:status messageAsArray:value];
    }

    // Object
    if ([value isKindOfClass:[NSDictionary class]]) {
        return [CDVPluginResult resultWithStatus:status messageAsDictionary:value];
    }

    // Null
    if ([value isKindOfClass:[NSNull class]]) {
        return [CDVPluginResult resultWithStatus:status];
    }

    // Nil
    if (!value) {
        return [CDVPluginResult resultWithStatus:status];
    }

    return [CDVPluginResult resultWithStatus:status];
}

- (void)start:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSLog(@"GIMBAL: start from JS plugin");
        [self startGimbal];
        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)stop:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        NSLog(@"GIMBAL: stop from JS plugin");
        [self stopGimbal];
        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)startGimbal {
    NSLog(@"GIMBAL: starting gimbal adapter");
    [[GimbalAdapter shared] startAdapter];
}

- (void)stopGimbal {
    NSLog(@"GIMBAL: stopping gimbal");
    [[GimbalAdapter shared] stopAdapter];
}

@end
