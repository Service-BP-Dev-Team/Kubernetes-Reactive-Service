package com.reactive.service.model.specification;


public class IdExpression extends Expression{
	private ServiceInstance serviceInstance;
	private String parameterName;
	
	
	public String getParameterName() {
		return parameterName;
	}
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}
	public ServiceInstance getServiceInstance() {
		return serviceInstance;
	}
	public void setServiceInstance(ServiceInstance serviceInstance) {
		this.serviceInstance = serviceInstance;
	}
	
	public String asString() {
		return this.getServiceInstance().getService().getName()+"."+this.getParameterName();
	}
	
	
	
}
