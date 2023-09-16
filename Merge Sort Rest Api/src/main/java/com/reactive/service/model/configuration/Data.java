package com.reactive.service.model.configuration;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.reactive.service.model.specification.Parameter;

import com.reactive.service.util.NameGenerator;

public class Data implements Serializable{

	private static int idCounter=0;
	
	private Parameter parameter;
	
	private Object value;
	
	private boolean defined;
	
	private String id;
	
	private String serviceCallId; // use to quickly retrieve a configuration that have a data
	
	private String host; // use to quickly retrieve the host where to send the data
	public Data() {
		idCounter++;
	    id = UUID.randomUUID().toString();
	}
	
	
	

	public void setId(String id) {
		this.id = id;
	}




	public static int getIdCounter() {
		return idCounter;
	}

	public static void setIdCounter(int idCounter) {
		Data.idCounter = idCounter;
	}
	
	public Parameter getParameter() {
		return parameter;
	}

	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
		if(value!=null) {
			this.defined=true;
		}
	}

	public boolean isDefined() {
		return defined;
	}

	public void setDefined(boolean defined) {
		this.defined = defined;
	}

	public String getGlobalID(Configuration conf) {
		return conf.getId()+id;
	}


	public String getServiceCallId() {
		return serviceCallId;
	}




	public void setServiceCallId(String serviceCallId) {
		this.serviceCallId = serviceCallId;
	}




	public String getId() {
		return id;
	}




	public String getHost() {
		return host;
	}




	public void setHost(String host) {
		this.host = host;
	}
	
	
	

}
