package com.reactive.service.model.specification;

import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlAttribute;

public class Guard implements Serializable {
	private String location;
	private String method;

	@XmlAttribute
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	@XmlAttribute
	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

}
