package com.reactive.service.model.specification;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;



public class Parameter implements Serializable{

	private String name;
	private String shortName;
	private Service Service; // may be null if the parsing from xml fail

	
	@JsonIgnore
	public Service getService() {
		return Service;
	}
	

	public void setService(Service service) {
		Service = service;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Parameter() {
		
	}
	public Parameter(String name) {
		super();
		this.name = name;
	}
	
	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	
}
