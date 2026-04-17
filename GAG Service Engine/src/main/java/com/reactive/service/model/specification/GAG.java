package com.reactive.service.model.specification;

import java.util.ArrayList;

public class GAG {

	private String name;
	private ArrayList<Service> services;
	
	public GAG() {
		services = new ArrayList<Service>();
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	

	public ArrayList<Service> getServices() {
		return services;
	}
	public void setServices(ArrayList<Service> services) {
		this.services = services;
	}
	
	public Service findByName(String serviceName) {
		Service result=null;
		for(Service s: services) {
			if(s.getName().equals(serviceName)) {
				return s;
			}
		}
		return result;
	}
	
	
}
