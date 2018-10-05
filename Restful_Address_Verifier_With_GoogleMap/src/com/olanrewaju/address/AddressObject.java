package com.olanrewaju.address;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AddressObject {


	private String address;

	private String status;

	private Location location;

	private Map<String, Object> additionalProperties = new HashMap<String, Object>();


	public String getAddress() {
		return address;
	}


	public void setAddress(String address) {
		this.address = address;
	}

	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public Location getLocation() {
		return location;
	}


	public void setLocation(Location location) {
		this.location = location;
	}


	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}


	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}
