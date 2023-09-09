package com.reactive.service.model.configuration;

import java.io.Serializable;

import com.reactive.service.model.specification.Parameter;

import com.reactive.service.util.NameGenerator;

public class Data implements Serializable{

	private static int idCounter=0;
	
	private Parameter parameter;
	
	private Object value;
	
	private boolean defined;
	
	private int id;
	
	public Data() {
		id = idCounter;
		idCounter++;
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	
	
	

}
