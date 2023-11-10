package com.reactive.service.model.specification;


public class IdExpression extends Expression{
	protected ServiceInstance serviceInstance;
	protected String parameterName;
	protected boolean array;
	protected int size; // this is defined in execution time for local
						// variable that correspond to array
	
	
	public boolean isArray() {
		return array;
	}
	public void setArray(boolean array) {
		this.array = array;
	}
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
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	
	
	
	
}
