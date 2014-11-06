#import "TCPServer.h"
#import "GCDAsyncSocket.h"

// TODO: Wirklich notwendig?
#define WELCOME_MSG  0
#define ECHO_MSG     1
#define WARNING_MSG  2

#define READ_TIMEOUT 15.0
#define READ_TIMEOUT_EXTENSION 10.0


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

    // get parameters
    serverCallback = [command.callbackId copy];
    
    NSString *parameters = [command.arguments objectAtIndex:0];
    int port = [[parameters valueForKey:@"port"] integerValue];

    // Save data to send
    dataToSend = [parameters valueForKey:@"dataToSend"];
    NSLog(@"TCP Server Plugin: Data to send: %@", dataToSend);

    // Start Server
    NSError *error = nil;
    if(![listenSocket acceptOnPort:port error:&error])
    {
        isRunning = NO;
        NSLog(@"TCP Server Plugin: Error starting server: %@", error);
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[NSString stringWithFormat:@"Server not started"]];
    }
    else
    {
        isRunning = YES;
        NSLog(@"TCP Server Plugin: Server started on port %hu", [listenSocket localPort]);
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[NSString stringWithFormat:@"Server started on port %hu", [listenSocket localPort]]];
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
    
    // TODO
//    NSString *welcomeMsg = dataToSend;
//	NSData *welcomeData = [welcomeMsg dataUsingEncoding:NSUTF8StringEncoding];
//	
//	[newSocket writeData:welcomeData withTimeout:-1 tag:WELCOME_MSG];
	
	[newSocket readDataToData:[GCDAsyncSocket CRLFData] withTimeout:READ_TIMEOUT tag:0];
}


- (void)socket:(GCDAsyncSocket *)sock didReadData:(NSData *)data withTag:(long)tag
{
	// This method is executed on the socketQueue (not the main thread)
	
	dispatch_async(dispatch_get_main_queue(), ^{
		@autoreleasepool {
		
			NSData *strData = [data subdataWithRange:NSMakeRange(0, [data length] - 2)];
			NSString *msg = [[NSString alloc] initWithData:strData encoding:NSUTF8StringEncoding];
			if (msg)
			{
				NSLog(@"TCP Server Plugin: Receceived data: %@", msg);
			}
			else
			{
				NSLog(@"Error converting received data into UTF-8 String");
			}
		
		}
	});
	
	// Echo message back to client
    // TODO
    NSData *dataToSend2 = [dataToSend dataUsingEncoding:NSUTF8StringEncoding];
	[sock writeData:dataToSend2 withTimeout:-1 tag:ECHO_MSG];
}

@end

















