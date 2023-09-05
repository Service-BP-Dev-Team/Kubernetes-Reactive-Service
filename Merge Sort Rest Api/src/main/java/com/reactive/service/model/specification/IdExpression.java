package com.reactive.service.model.specification;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;



public class IdExpression extends Expression{
	private ServiceInstance serviceInstance;
	private String parameterName;
	
	
	@XmlAttribute(name="parameter")
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
