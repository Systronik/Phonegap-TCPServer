//
//  MEGBluetoothSerial.m
//  Bluetooth Serial Cordova Plugin
//
//  Created by Don Coleman on 5/21/13.
//
//

#import "TCPServer.h"

@interface TCPServer()

@end

@implementation TCPServer{
    
    
}

- (void)pluginInitialize {
    
    NSLog(@"TCP Server Plugin init");
    NSLog(@"(c)2014 Systronik GmbH");
    [super pluginInitialize];
}

#pragma mark - Cordova Plugin Methods

- (void)startServer:(CDVInvokedUrlCommand *)command {
    
    NSLog(@"startServer");
    NSString *arg1 = [command.arguments objectAtIndex:0];
    
    serverCallback = [command.callbackId copy];
    CDVPluginResult *pluginResult = nil;
    [pluginResult setKeepCallbackAsBool:TRUE];
    
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"TCP Server started"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:serverCallback];
}

@end
