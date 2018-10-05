package com.olanrewaju.address;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olanrewaju.barrier.Semaphor;
import com.olanrewaju.networkUtil.HelperUtils;

public class AddressVerifier {

	/*
	 * configured via config.properties in projectDir/
	 */
	final int DEFAULTRETRYLIMIT = 0;
	final int DEFAULT_NUMBER_OF_THREAD = 1;
	int maxRetry;
	int maxThreads;
	final String RETRY_LIMIT_KEY = "retryAmountLimit";
	final String NUMBER_OF_THREADS_TO_USE_FOR_SEARCHKEY = "numberOfThreadsToUseForSearching";

	final String HTTPMETHOD = "GET";
	final String DEFAULTURL = "https://maps.googleapis.com/maps/api/geocode/json?address="; // "https://www.google.com/";
	final String NOT_FOUND = "NOTFOUND";

	Object condition = new Object();
	Map<String, String> m = new HashMap<String, String>();
	Semaphor barriar[];

	// PRE - load configuration properties from classpath
	// POST - initialize program properties
	private void loadConfigAndInitializeRunParam() {

		maxRetry = DEFAULTRETRYLIMIT;
		maxThreads = DEFAULT_NUMBER_OF_THREAD;

		try {

			int num1;
			String retryLimit = HelperUtils.getConfigProperty(RETRY_LIMIT_KEY);
			if (retryLimit != null
					&& !retryLimit.isEmpty()
					&& (num1 = Integer.parseInt(retryLimit)) > DEFAULTRETRYLIMIT) {
				maxRetry = num1;
			}

			int num2;
			String maxThreadsToUse = HelperUtils
					.getConfigProperty(NUMBER_OF_THREADS_TO_USE_FOR_SEARCHKEY);
			if (maxThreadsToUse != null
					&& !maxThreadsToUse.isEmpty()
					&& (num2 = Integer.parseInt(maxThreadsToUse)) > DEFAULT_NUMBER_OF_THREAD) {

				maxThreads = num2;
			}

		} catch (Exception e){
			e.printStackTrace();
		}finally{
			barriar = new Semaphor[maxThreads]; 
		}
	}

	// PRE - @param url, the host URL @param queryString, the Querystring.
	// @param method, the HTTP method to use
	// POST - open return a connection Object or Exception,

	public ConcurrentLinkedQueue<String> findAddress( ConcurrentLinkedQueue<String> _addressList) {

		loadConfigAndInitializeRunParam();
		ConcurrentLinkedQueue<String> resultList = new ConcurrentLinkedQueue<String>();

		if (_addressList == null || _addressList.isEmpty()) {
			/* Log Error here */
			return null;
		}

		for (int i = 0; i < maxThreads; i++) {
			Thread t = new Thread(new ValidateAdrressTask(_addressList,
					resultList));
			t.setName(String.valueOf(i));
			barriar[i] = new Semaphor();
			t.start();
		}

		waitForJobCompletion();
		return resultList;
	}

	// PRE - @param url, the host URL @param queryString, the Querystring.
	// @param method, the HTTP method to use
	// POST - open return a connection Object or Exception,

	void waitForJobCompletion() {

		for (int i = 0; i < maxThreads; i++) {
			barriar[i].acquire();
		}

	}

	// PRE - @param url, the host URL @param queryString, the Querystring.
	// @param method, the HTTP method to use
	// POST - open return a connection Object or Exception,

	public AddressObject parseResult(InputStream inStream, String userAddress)
			throws IOException { // HttpURLConnection connection

		AddressObject address = null;
		String jsonString = null;
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = null;

		jsonString = HelperUtils.InputStreamToStiring(inStream);
		root = mapper.readTree(jsonString);

		JsonNode statusNode = root.path("status");
		String status = statusNode.asText();

		if (status.equalsIgnoreCase("ok")) {
			address = new AddressObject();
			Location loc = new Location();
			address.setLocation(loc);
			address.setStatus("FOUND");
			address.setAddress(userAddress);
			JsonNode addressNode = root.path("results");

			JsonNode geometry = null;
			for (JsonNode node : addressNode) {
				geometry = node.path("geometry");
				if (geometry != null)
					break;
			}

			if (geometry != null) {
				JsonNode location = geometry.path("location");

				Double lat = location.path("lat").asDouble();
				address.getLocation().setLat(lat);
				Double lng = location.path("lng").asDouble();
				address.getLocation().setLng(lng);

			}

		}

		return address;
	}

	// PRE - @param url, the host URL @param queryString, the Querystring.
		// @param method, the HTTP method to use
		// POST - open return a connection Object or Exception,
	
	// Task of Validating Address
	class ValidateAdrressTask implements Runnable {

		ConcurrentLinkedQueue<String> addressList;
		ConcurrentLinkedQueue<String> resulttList;

		ValidateAdrressTask(ConcurrentLinkedQueue<String> _addressList,
				ConcurrentLinkedQueue<String> _resulttList) {
			this.addressList = _addressList;
			this.resulttList = _resulttList;
		}

		
		// PRE - List containing address is not empty.
		// POST - All addresses in List has been verified. List is now empty and run method completed.
			
		public void run() {
		//	try {
				String address = null;
				boolean outerloop = false;
				HttpURLConnection connection = null;
				boolean httpResponseOk = false;

				do {
					
					address = null;
					outerloop = false;
					connection = null;
					httpResponseOk = false;

					synchronized (condition) {

						if (!addressList.isEmpty()) {
							address = addressList.remove();
						}
					}

					if (address != null) {
						outerloop = true;
						int retryCount = 0;
						boolean innerLoop = true;
						AddressObject addressObj = null;

						while (innerLoop && retryCount <= maxRetry) {
							retryCount++;
							innerLoop = false;

							try {// make HTTP call

								connection = HelperUtils.getConnection(
										DEFAULTURL, address, HTTPMETHOD);
									httpResponseOk = true;

							} catch (IOException e) {

								addressObj = makeUnknowAddress(address);
								String json = HelperUtils
										.objectToJson(addressObj);
								resulttList.add(json);
								e.printStackTrace();
								break;
							}
						   if (httpResponseOk) { // check HTTp Response
								try { // parse input
									addressObj = parseResult(connection.getInputStream(),address);
									if (addressObj == null) { // Is Status OK?
															

										if (retryCount <= maxRetry) {
											innerLoop = true;
											continue;
										} else {
											// construct NOT FOUND status
											addressObj = makeUnknowAddress(address);
											String json = HelperUtils
													.objectToJson(addressObj);
											resulttList.add(json);
											break;
										}
									} else {
										/*
										 * Good result
										 */
										String json = HelperUtils
												.objectToJson(addressObj);
										resulttList.add(json);
										break;
									}

								} catch (Exception e) {
									
									addressObj = makeUnknowAddress(address);
									String json = HelperUtils
											.objectToJson(addressObj);
									resulttList.add(json);
									 e.printStackTrace();
									break;
								}

							}

						}

					}

				} while (outerloop);

			
				barriar[Integer.parseInt(Thread.currentThread().getName())]
						.release();
		}
	}

	// PRE - @param url, the host URL @param queryString, the Querystring.
	// @param method, the HTTP method to use
	// POST - open return a connection Object or Exception,

	AddressObject makeUnknowAddress(String address) {

		AddressObject addressObj = new AddressObject();
		addressObj.setAddress(address);
		addressObj.setStatus(NOT_FOUND);

		return addressObj;
	}

}
