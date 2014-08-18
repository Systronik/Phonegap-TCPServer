package org.systronik.tcpserver;

import android.view.WindowManager;
import org.apache.cordova.CallbackContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

// Cordova
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

public class TCPServer extends CordovaPlugin {

	private static final String TAG = "TCPServer";
  
  private static final String ACTION_KEEP_AWAKE = "keepAwake";
  private static final String ACTION_ALLOW_SLEEP_AGAIN = "allowSleepAgain";
  private static final String ACTION_START_SERVER = "startServer";
  private ServerSocket server;
  private String ReceivedString;
  private String getExample = "HTTP/1.1 200 OK\r\nDate: Mon, 23 May 2005 22:38:34 GMT\r\nServer: Apache/1.3.3.7 (Unix) (Red-Hat/Linux)\r\nLast-Modified: Wed, 08 Jan 2003 23:11:55 GMT\r\nETag: \"3f80f-1b6-3e1cb03b\"\r\nContent-Type: text/html; charset=UTF-8\r\nContent-Length: 124\r\nAccept-Ranges: bytes\r\nConnection: close\r\n\r\n<html>\r\n<head>\r\n<title>An Example Page</title>\r\n</head>\n<body>\r\nHello World, this is a very simple 1234 document.\r\n</body>\r\n</html>\r\n";
  private String OutString = "HTTP/1.1 200 OK\r\nServer: Apache/1.3.29 (Unix) PHP/4.3.4\r\nContent-Length: 209\r\nContent-Language: de\r\nConnection: close\r\nContent-Type: text/html\r\n\r\n<!DOCTYPE html><html><head><title>TODO supply a title</title><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"></head><body><div>Hello World</div></body>/html>\r\n";

  ServerSocket myServerSocket;
  boolean ServerOn = true;
  
  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    try {
      if (ACTION_START_SERVER.equals(action)) {
    	  cordova.getThreadPool().execute(new Runnable() {
    		    public void run() {
    		    	Log.d(TAG, "Server is starting...");    	  
    				  try
    			        { 
    			            myServerSocket = new ServerSocket(53000); 
    			            Log.d(TAG, "server started"); 
    			        } 
    			        catch(IOException ioe) 
    			        { 
    			        	Log.d(TAG, "Could not start server");  
    			            System.exit(-1); 
    			        }     			        
    			        // Successfully created Server Socket. Now wait for connections. 
    			        while(ServerOn) 
    			        {                        
    			            try
    			            { 
    			                // Accept incoming connections. 
    			                Socket clientSocket = myServerSocket.accept(); 	 
    			                // accept() will block until a client connects to the server. 
    			                // If execution reaches this point, then it means that a client 
    			                // socket has been accepted. 
    			 
    			                // For each client, we will start a service thread to 
    			                // service the client requests. This is to demonstrate a 
    			                // Multi-Threaded server. Starting a thread also lets our 
    			                // MultiThreadedSocketServer accept multiple connections simultaneously. 
    			                
    			                // Start a Service thread 
    			                ClientServiceThread cliThread = new ClientServiceThread(clientSocket);
    			                cliThread.start(); 
    			 
    			            } 
    			            catch(IOException ioe) 
    			            { 
    			            	Log.d(TAG, "Client accept exception"); 
    			                ioe.printStackTrace(); 
    			            } 
    			 
    			        }
    			 
    			        try
    			        { 
    			            myServerSocket.close(); 
    			            Log.d(TAG, "Server stopped");  
    			        } 
    			        catch(Exception ioe) 
    			        { 
    			        	Log.d(TAG, "Problem stopping server socket"); 
    			            System.exit(-1); 
    			        } 
    			        callbackContext.success();  
    		    }
    	  });
    	  return true;
      
      } else {
        callbackContext.error("Function: " + action + " is not a supported function");
        return false;
      }
    } catch (Exception e) {
      callbackContext.error(e.getMessage());
      return false;
    }
  }
  
  class ClientServiceThread extends Thread 
  { 
      Socket myClientSocket;
      boolean m_bRunThread = true; 

      public ClientServiceThread() 
      { 
          super(); 
      } 

      ClientServiceThread(Socket s) 
      { 
          myClientSocket = s; 

      } 

      public void run() 
      {            
          // Obtain the input stream and the output stream for the socket 
          // A good practice is to encapsulate them with a BufferedReader 
          // and a PrintWriter as shown below. 
          BufferedReader in = null; 
          //PrintWriter out = null; 

          // Print out details of this connection
          Log.d(TAG, "Accepted Client Address - " + myClientSocket.getInetAddress().getHostName()); 
          try
          {                                
              in = new BufferedReader(new InputStreamReader(myClientSocket.getInputStream())); 
              //out = new PrintWriter(new OutputStreamWriter(myClientSocket.getOutputStream())); 
              OutputStream output = myClientSocket.getOutputStream();
              // At this point, we can read for input and reply with appropriate output. 
              
              // Run in a loop until m_bRunThread is set to false 
              while(m_bRunThread) 
              {                    
                  // read incoming stream 
                  String clientCommand = in.readLine(); 
                  if (clientCommand != null){
                	  Log.d(TAG, "Client Says :" + clientCommand);                 	  
                  }
                  

                  if(!ServerOn) 
                  { 
                      // Special command. Quit this thread 
                	  Log.d(TAG, "Server has already stopped");                       
                      //out.println("Server has already stopped"); 
                      //out.flush(); 
                      m_bRunThread = false;   

                  } 
                  
                  
                  //long time = System.currentTimeMillis();
                  
					
                  if(clientCommand.equalsIgnoreCase("quit")) { 
                      // Special command. Quit this thread 
                      m_bRunThread = false;   
                      Log.d(TAG, "Stopping client thread for client : ");                        
                  } else if(clientCommand.equalsIgnoreCase("end")) { 
                      // Special command. Quit this thread and Stop the Server
                      m_bRunThread = false;  
                      Log.d(TAG, "Stopping client thread for client : ");          
                      ServerOn = false;
                  } else if(clientCommand.contains("GET")) { 
                      // Get Command
                	  output.write(getExample.getBytes());
                	  output.flush();
                  } else {
                          // Process it
                	  	  Log.d(TAG, "Server sending...");                	  	  
                          //out.println("Server Says : " + clientCommand); 
                          //out.print(&OutString[0]);
                          //out.flush(); 
                  }
              } 
          } 
          catch(Exception e) 
          { 
              e.printStackTrace(); 
          } 
          finally
          { 
              // Clean up 
              try
              {                    
                  in.close(); 
                  //out.close();
                  myClientSocket.close();
                  Log.d(TAG, "...Stopped");     
              } 
              catch(IOException ioe) 
              { 
                  ioe.printStackTrace(); 
              } 
          } 
      } 
  }   
}