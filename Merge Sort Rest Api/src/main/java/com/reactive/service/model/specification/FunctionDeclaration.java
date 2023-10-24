package com.reactive.service.model.specification;

import java.io.Serializable;

public class FunctionDeclaration implements Serializable{
	private String name;
	private String location;
	private String method;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
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
