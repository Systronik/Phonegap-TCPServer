#import "TCPServer.h"
#import "GCDAsyncSocket.h"


@implementation TCPServer
// instance variables
{
	dispatch_queue_t socketQueue;
	
	GCDAsyncSocket *listenSocket;
	NSMutableArray *connectedSockets;
	
	BOOL isRunning;

    NSString *serverCallback;
    NSString *dataToSend;
}

// method implementations
- (void)pluginInitialize {
    NSLog(@"TCP Server Plugin: (c)2014 Systronik GmbH");
    NSLog(@"TCP Server Plugin: Initializing...");
    [super pluginInitialize];
    
    // Setup our socket.
    socketQueue = dispatch_queue_create("socketQueue", NULL);
    
    listenSocket = [[GCDAsyncSocket alloc] initWithDelegate:self delegateQueue:socketQueue];
    
    // Setup an array to store all accepted client connections
    connectedSockets = [[NSMutableArray alloc] initWithCapacity:1];
    
    isRunning = NO;
}

#pragma mark - Cordova Plugin Methods

- (void)startServer:(CDVInvokedUrlCommand *)command {
    // Initialize
    NSLog(@"TCP Server Plugin: Starting Server...");
    
    CDVPluginResult *pluginResult = nil;
    [pluginResult setKeepCallbackAsBool:TRUE];

    serverCallback = [command.callbackId copy];

    // get port number and data to send
    NSString *parameters = [command.arguments objectAtIndex:0];
    int port = [[parameters valueForKey:@"port"] integerValue];
    dataToSend = [parameters valueForKey:@"dataToSend"];
    
    NSLog(@"TCP Server Plugin: Data to send: %@", dataToSend);


    // Server already running?
    if (isRunning)
    {
        NSLog(@"TCP Server Plugin: Server already running on port %hu. Updated data to send.", [listenSocket localPort]);
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[NSString stringWithFormat:@"Server already running on port %hu. Updated data to send.", [listenSocket localPort]]];
    }
    else
    {
        // Start Server
        NSError *error = nil;
        if([listenSocket acceptOnPort:port error:&error])
        {
            isRunning = YES;
            NSLog(@"TCP Server Plugin: Server started on port %hu", [listenSocket localPort]);
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[NSString stringWithFormat:@"Server started on port %hu", [listenSocket localPort]]];
        }
        else
        {
            isRunning = NO;
            NSLog(@"TCP Server Plugin: Could not start Server: %@", error);
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[NSString stringWithFormat:@"Could not start Server"]];
        }

    }

    // Return Result
    [self.commandDelegate sendPluginResult:pluginResult callbackId:serverCallback];
}


- (void)socket:(GCDAsyncSocket *)sock didAcceptNewSocket:(GCDAsyncSocket *)newSocket
{
	// This method is executed on the socketQueue (not the main thread)
    
	@synchronized(connectedSockets)
	{
		[connectedSockets addObject:newSocket];
	}
	
	NSString *host = [newSocket connectedHost];
	UInt16 port = [newSocket connectedPort];
	
	dispatch_async(dispatch_get_main_queue(), ^{
		@autoreleasepool {
        
			NSLog(@"Accepted client %@:%hu", host, port);
            
		}
	});
    
    // Ready to read data
	[newSocket readDataToData:[GCDAsyncSocket CRLFData] withTimeout:-1 tag:0];
}


- (void)socket:(GCDAsyncSocket *)sock didReadData:(NSData *)data withTag:(long)tag
{
	// This method is executed on the socketQueue (not the main thread)

    NSString *dataToSendToSpecificClient = [[NSString alloc] initWithString:dataToSend];
	
//	dispatch_async(dispatch_get_main_queue(), ^{
//		@autoreleasepool
//        {
            
            // get received data
			NSData *strData = [data subdataWithRange:NSMakeRange(0, [data length] - 2)];
			NSString *receivedData = [[NSString alloc] initWithData:strData encoding:NSUTF8StringEncoding];
            
			if (receivedData)
			{
				NSLog(@"TCP Server Plugin: Received data: %@", receivedData);
			}
			else
			{
				NSLog(@"Error converting received data into UTF-8 String");
			}

            // do we have a correct HTML-Request?
            if ( [receivedData hasPrefix:@"GET"] && ([receivedData containsString:@"HTTP/"] || [receivedData containsString:@"http/"]) )
            {
                // did we received an ID?
                if ([receivedData containsString:@"id="])
                {
                    // Read out received ID
                    NSString *idString = [[NSString alloc] initWithString:receivedData];
                    
                    idString = [idString substringFromIndex:[idString rangeOfString:@"id="].location + 3];
                    idString = [idString substringToIndex:[idString rangeOfString:@"HTTP/" options:NSCaseInsensitiveSearch].location];
                    idString = [idString stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
                    
                    //NSLog(@"TCP Server Plugin: Received ID: %@", idString);
                    
                    // Write received ID into data to send to specific client
                    dataToSendToSpecificClient = [dataToSendToSpecificClient stringByReplacingOccurrencesOfString:@"TransmissionId=\"0\"" withString:[NSString stringWithFormat:@"TransmissionId=\"%@\"", idString]];

                    // Update content length
                    NSString *contentString = [[NSString alloc] initWithString:dataToSendToSpecificClient];
                    contentString = [contentString substringFromIndex:[contentString rangeOfString:@"\r\n\r\n"].location + 4];
                    //NSLog(@"TCP Server Plugin: Content string: %@", contentString);
                    //NSLog(@"TCP Server Plugin: Content length: %i", [contentString length]);
                    
                    NSString *contentLengthString = [[NSString alloc] initWithString:dataToSendToSpecificClient];
                    contentLengthString = [contentLengthString substringFromIndex:[contentLengthString rangeOfString:@"Content-Length"].location];
                    contentLengthString = [contentLengthString substringToIndex:[contentLengthString rangeOfString:@"Content-Language"].location];
                    
                    dataToSendToSpecificClient = [dataToSendToSpecificClient stringByReplacingOccurrencesOfString:contentLengthString withString:[NSString stringWithFormat:@"Content-Length: %i\r\n", [contentString length]]];
                    //NSLog(@"TCP Server Plugin: Data to send to specific Client: %@", dataToSendToSpecificClient);
                }
            }
            else
            {
                // create non-HTML
                dataToSendToSpecificClient = [dataToSendToSpecificClient substringFromIndex:[dataToSendToSpecificClient rangeOfString:@"\r\n\r\n"].location + 4];
            }

	
//		}
//	});
    
    // Send data to client
    [sock writeData:[dataToSendToSpecificClient dataUsingEncoding:NSUTF8StringEncoding] withTimeout:-1 tag:0];
}

@end

















