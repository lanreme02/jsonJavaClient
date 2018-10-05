package com.olanrewaju.networkUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.olanrewaju.address.AddressObject;

public class HelperUtils {

	// PRE - @param url, the host URL @param queryString, the Querystring.
	// @param method, the HTTP method to use
	// POST - open return a connection Object or Exception,
	public static HttpURLConnection getConnection(String url,
			String queryString, String method) throws IOException {

		if (queryString != null) {
			url += URLEncoder.encode(queryString, "UTF-8");
		}
		URL siteURL = new URL(url);

		HttpURLConnection connection = (HttpURLConnection) siteURL
				.openConnection();
		connection.setRequestMethod(method);
		connection.setConnectTimeout(3000);
		connection.connect();
		return connection;
	}

	// PRE - @param instream, Stream to read from
	// POST - return a String value of the stream,

	public static String InputStreamToStiring(InputStream inStream) {
		final StringBuilder out = new StringBuilder();
		int bufferSize = 1024;
		final char[] buffer = new char[bufferSize];
		Reader input = null;
		try {
			input = new InputStreamReader(inStream, "UTF-8"); //
				int rsz = 0;
			 while(!((rsz = input.read(buffer, 0, buffer.length)) < 0))
				out.append(buffer, 0, rsz);
			
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		return out.toString();
	}

	// PRE - Read lines from a file
	// POST - return a list of strings

	public static void readInputFile(String filePath,
			ConcurrentLinkedQueue<String> StringLineList) {

		String line = null;
		BufferedReader reader = null;
		try{
		reader = new BufferedReader(new FileReader(filePath));
		while ((line = reader.readLine()) != null) {
			StringLineList.add(line);
		}

	} catch (IOException io) {
		io.printStackTrace();
	} finally {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	}

	// PRE - @param property whose value should return
	// POST - returns NULL if no property with that name is found
	public static String getConfigProperty(String property) {

		Properties prop = new Properties();
		InputStream input = null;
		String value = null;

		try {

			input = new FileInputStream("config.properties");

			
			 String filename = "config.properties"; 
			// input = HelperUtils.class.getClassLoader().getResourceAsStream(filename);
			// if (input == null) {
				// Log error to file
				// System.out.println("Sorry, unable to find " + filename);
			//	return value;
			//}

			// load a properties file
			prop.load(input);

			// get the property value and print it out
			value = prop.getProperty(property);
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		return value;
	}

	// PRE - @param address, the object to convert to Json string
	// POST - return string value of object or NULL,

	public static String objectToJson(AddressObject address) {

		ObjectMapper mapper = new ObjectMapper().configure(
				SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		String json = null;
		try {
			json = mapper.writeValueAsString(address);
		} catch (JsonProcessingException e) {
			// most likely error free so just print message
			e.printStackTrace();
		}

		return json;

	}
}