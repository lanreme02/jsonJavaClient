/* Develop by: Olanrewaju Elisha 
 * Date : 08/31/2018
 * purpose : given an address, find LAT and LNG for the address using google API https://maps.googleapis.com/maps/api/geocode/json?address=<address string> 
 * 
 * 	@config.properties file  : in project root dir allows for passing parameter
 * 
 * to run : change directory to project root dir [Olanrewaju_Elisha_Project]
 *          add these to classpath:
 *          
 *          $yourpath/Olanrewaju_Elisha_Project/src/
 *          $yourpath/Olanrewaju_Elisha_Project/
 *          $yourpath/Olanrewaju_Elisha_Project/lib/jackson-annotations-2.9.6.jar
 *          $yourpath/Olanrewaju_Elisha_Project/lib/jackson-core-2.9.6.jar
 *          $yourpath/Olanrewaju_Elisha_Project/lib/jackson-databind-2.9.6.jar
 *          
 *          compile:
 *          javac src/olanrewaju/interview/app/Application.java
 *          
 *          run:
 *          java olanrewaju.interview.app.Application
 */


package com.olanrewaju.app;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.olanrewaju.address.AddressVerifier;
import com.olanrewaju.networkUtil.HelperUtils;

public class Application {
	static final String DEFAULTFILEPATH = "./resource/addresses.txt";
	static AddressVerifier addressFinder =  new AddressVerifier();
	
	// PRE - @param address, the object to convert to Json string
		// POST - return string value of object or NULL,
	static void readInputFile(String filePath,ConcurrentLinkedQueue<String> list){
		if(filePath == null || filePath.isEmpty() || list == null){
			return;
		}
			HelperUtils.readInputFile(filePath,list);
	}
	
	static ConcurrentLinkedQueue<String> runApp(String filePath, ConcurrentLinkedQueue<String> list){
		
			readInputFile(filePath,list);
			if(list == null){
				// Log error
				return null;
			}
			
		list =	addressFinder.findAddress(list);
			return list;
	}
	
	public static void main(String[] args) {
		
		 ConcurrentLinkedQueue<String> list = new ConcurrentLinkedQueue<String>();
		list = runApp(DEFAULTFILEPATH,list);
		
		
		for(String l: list)
			System.out.println(l);
		
				
				
	}
}
