package com.reactive.service.model.specification;

import java.io.Serializable;
import java.util.ArrayList;


public class Guard implements Serializable {
	private String location;
	private String method;
	private ArrayList<Parameter> binding;

	
	public Guard() {
		binding = new ArrayList<>();
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

	public ArrayList<Parameter> getBinding() {
		return binding;
	}

	public void setBinding(ArrayList<Parameter> binding) {
		this.binding = binding;
	}
	
	

}
