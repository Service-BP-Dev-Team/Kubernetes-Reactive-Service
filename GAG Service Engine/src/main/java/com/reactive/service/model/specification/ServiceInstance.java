package com.reactive.service.model.specification;

public class ServiceInstance {
	String name;
	Service service;
	private boolean remote=false;
	private boolean internal=false;
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
	public boolean isRemote() {
		return remote;
	}
	public void setRemote(boolean remote) {
		this.remote = remote;
	}
	public void setInternal(boolean internal) {
		// TODO Auto-generated method stub
		this.internal=internal;
	}
	public boolean isInternal() {
		return internal;
	}
	
	
	
	
}
