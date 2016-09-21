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


@implementation GimbalPlugin

// Config keys
NSString *const GimbalAPIKey = @"com.urbanairship.gimbal_api_key";
BOOL *const GimbalAutoStart = @"com.urbanairship.gimbal_auto_start";


- (void)pluginInitialize {
    
    NSDictionary *settings = self.commandDelegate.settings;
    
    if (!settings[GimbalAPIKey]) {
        NSLog(@"No Gimbal API key found, Gimbal cordova plugin initialization failed.");
        
        return;
    }
    
    NSLog(@"Initializing Urban Airship Gimbal cordova plugin.");

    [Gimbal setAPIKey:settings[GimbalAPIKey] options:nil];
    
    if (settings[GimbalAutoStart]) {
        if (GimbalAutoStart) {
            [[GimbalAdapter shared] startAdapter];
        }
    }
}

- (void)start:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        [self startGimbal]
        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)stop:(CDVInvokedUrlCommand *)command {
    [self performCallbackWithCommand:command withBlock:^(NSArray *args, UACordovaCompletionHandler completionHandler) {
        [self stopGimbal]
        completionHandler(CDVCommandStatus_OK, nil);
    }];
}

- (void)startGimbal {

    [[GimbalAdapter shared] startAdapter];

}

- (void)stopGimbal {

    [[GimbalAdapter shared] stopAdapter];

}

@end
