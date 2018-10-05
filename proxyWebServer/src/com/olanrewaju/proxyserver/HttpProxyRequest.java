package com.olanrewaju.proxyserver;
import java.io.*;
import java.net.*;
import java.util.*;

final class HttpProxyRequest implements Runnable {
	final static String CRLF = "\r\n";
	Socket socket;
	Socket proxySoc;
	
	// Constructor
	public HttpProxyRequest(Socket socket) throws Exception {
		this.socket = socket;
	}

	// Implement the run() method of the Runnable interface.
	public void run() {
		try {

			// Get the request line of the HTTP request message.
			processRequest();
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	private void processRequest() throws Exception {
		// Get a reference to the socket's input and output streams.
		InputStream is = socket.getInputStream();
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		
		// Set up input stream filters.
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		// setup proxy connection streams
		String request = buildRequest(br);
		
		DataOutputStream proxyOs = new DataOutputStream(
				proxySoc.getOutputStream());

		outStream(proxyOs, request);
		
		proxyOs.flush();

		InputStream proxyIs = proxySoc.getInputStream();
	
		sendBytes(proxyIs, os);
		os.close();
		proxyOs.close();
		proxySoc.close();
		socket.close();
	}

	private String buildRequest(BufferedReader br) throws IOException {
		
		String requestLine = br.readLine();

		
		//System.out.println(requestLine);
		StringBuilder buff = new StringBuilder();
		StringTokenizer tokens = new StringTokenizer(requestLine);
		buff.append(tokens.nextToken() + " ");
		String path = tokens.nextToken();
		
		//build socket
		String domain = path.split("/")[1];
		proxySoc = new Socket(domain, 80);
		
		
		String filepath =  path.substring(domain.length()+1);
		buff.append(filepath + " ");
		String http = tokens.nextToken();
		buff.append(http);
		buff.append(CRLF);
		buff.append("HOST:" + domain + " 80"+CRLF);
		
		String line = null;
		br.readLine(); // ignore Host header 
		while(( line = br.readLine()).length() != 0)
			buff.append(line+CRLF);
		
		buff.append(CRLF);
			System.out.println(buff.toString());
		return buff.toString();
	}

	private static void sendBytes(InputStream  fis, OutputStream os)
			throws Exception {
		// Construct a 1K buffer
		byte[] buffer = new byte[1024];
		int bytes = 0;

		// Copy requested file into the socket's output stream.
		while ((bytes = fis.read(buffer)) != -1) {
			os.write(buffer, 0, bytes);
		}
	}

	void outStream(DataOutputStream os, String fileName) throws Exception {
		// Send the status line.
		os.writeBytes(fileName);

	}


}
