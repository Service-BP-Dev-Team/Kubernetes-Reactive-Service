package com.reactive.service.model.specification;

public class ServiceInstance {
	String name;
	Service service;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Service getService() {
		return service;
	}
	public void setService(Service service) {
		this.service = service;
	}
	
}
