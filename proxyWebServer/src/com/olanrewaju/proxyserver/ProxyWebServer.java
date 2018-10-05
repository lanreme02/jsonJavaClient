package com.olanrewaju.proxyserver;
import java.io.* ;
import java.net.* ;
import java.util.* ;


/**
 * 
 *  Example invocation:
 *   http://localhost:5000/WEBSITEURL
 *   EXAMPLE: http://localhost:5000/GOOGLE.COM/
 *
 */
public final class ProxyWebServer {
    public static void main(String argv[]) throws Exception {
	// Get the port number from the command line.
	int port = 5000; // (new Integer(argv[0])).intValue();
	
	ServerSocket socket = new ServerSocket(port);

	while (true) {
	   
	    Socket connection = socket.accept();
	        
	    HttpProxyRequest request = new HttpProxyRequest(connection);
	    Thread thread = new Thread(request);
	    thread.start();
	}
    }
}