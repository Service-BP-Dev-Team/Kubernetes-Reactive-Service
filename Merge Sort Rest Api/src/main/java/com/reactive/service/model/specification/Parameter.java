package com.reactive.service.model.specification;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;

public class Parameter implements Serializable{

	private String name;
	private String shortName;
	private Service Service; // may be null if the parsing from xml fail

	
	@XmlTransient
	@JsonIgnore
	public Service getService() {
		return Service;
	}
	
	 public void afterUnmarshal(Unmarshaller u, Object parent) {
		    this.setService((Service)parent);
		  }

	public void setService(Service service) {
		Service = service;
	}

	@XmlAttribute
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
	@XmlAttribute
	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	
}
