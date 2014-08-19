package org.systronik.tcpserver;

import org.apache.cordova.CallbackContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
// Cordova
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class TCPServer extends CordovaPlugin {

  private static final String TAG = "TCPServer";
  private static final String ACTION_START_SERVER = "startServer";
  private String OutString;

  ServerSocket myServerSocket;
  boolean ServerOn = true;
  boolean ServerActivated = false;
  
  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    try {
      if (ACTION_START_SERVER.equals(action)) {
    	  JSONObject arg_object = args.getJSONObject(0);
    	  OutString = arg_object.getString("dataToSend");
    	  final int port =  Integer.parseInt(arg_object.getString("port"));
    	  if (!(OutString.contains("HTTP/1.1"))){
    		  //OutString = getExample;
    	  }
    	  Log.d(TAG, "Data to send: " + OutString);
    	  if (!ServerActivated){
    		  cordova.getThreadPool().execute(new Runnable() {
    		    public void run() {
    		    	Log.d(TAG, "Server is starting...");    	  
    				  try
    			        { 
    			            myServerSocket = new ServerSocket(port); 
    			            Log.d(TAG, "server started");
    			            ServerActivated = true;
    			            
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
    	  }
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
                  String sendString;
                  if (clientCommand != null){
                	  Log.d(TAG, "Client Says :" + clientCommand);                 	  
                  }
                  
                  if(!ServerOn) 
                  { 
                      // Special command. Quit this thread 
                	  Log.d(TAG, "Server has already stopped");                                          
                      m_bRunThread = false;   
                  } 
                  if(clientCommand.equalsIgnoreCase("quit")) { 
                      // Special command. Quit this thread 
                      m_bRunThread = false;   
                      Log.d(TAG, "Stopping client thread for client : ");                        
                  } else if(clientCommand.equalsIgnoreCase("end")) { 
                      // Special command. Quit this thread and Stop the Server
                      m_bRunThread = false;  
                      Log.d(TAG, "Stopping client thread for client : ");          
                      ServerOn = false;
                  } else if(clientCommand.contains("GET")) {										// Data with html header   
                      // Get Command
                	  Log.d(TAG, "GET - Command");
                	  sendString = OutString;
                	  int i = clientCommand.indexOf("id=");			// Replaces TransmissionId
                	  if (i > -1){
                		  String idString = clientCommand.substring(i + 3);
                		  i = idString.indexOf("http/");
                		  if ( i == -1){
                			  i = idString.indexOf("HTTP/");
                		  }
                		  if ( i > -1){
                			  idString = idString.substring(0, i);
                			  idString = idString.trim();
                			  Log.d(TAG, "ID found: " + idString);
                			  sendString = sendString.replace("TransmissionId=\"0\"", "TransmisionId=\"" + idString + "\"");
                			  i = sendString.indexOf("Content-Length");
                			  String lengthString = sendString.substring(i);
                			  lengthString = lengthString.substring(0, lengthString.indexOf("Content-Language"));
                			  idString = sendString.substring(sendString.indexOf("\r\n\r\n") + 2);        
                			  
                			  sendString = sendString.replace(lengthString, "Content-Length:" + idString.length() + "\r\n");
                		  }
                		  
                	  }
                	  output.write(sendString.getBytes());
                	  output.flush();
                	  Log.d(TAG, "Data written");
                  } else {																			// Data without html header  
                	  sendString = OutString.substring(OutString.indexOf("\r\n\r\n") + 2);   
            	  	  output.write(sendString.getBytes());
            	  	  output.flush();
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