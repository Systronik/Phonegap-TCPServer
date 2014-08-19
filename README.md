Phonegap-TCPServer
==================

Phonegap TCP Server Plugin

This plugin implements a simple TCP server.
To start the server, install the plugin and run:

tcpserver.startServer(data,port);	//data is the data to be send to the clients and port is the server port.

after application has been initialised.

The server runs in a separated thread, meaning the server is running even if main application is suspended.