package com.reactive.service.model.specification;

import java.io.Serializable;


public class Guard implements Serializable {
	private String location;
	private String method;


	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

}
