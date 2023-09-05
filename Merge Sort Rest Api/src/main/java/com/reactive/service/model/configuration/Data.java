package com.reactive.service.model.configuration;

import java.io.Serializable;

import com.reactive.service.model.specification.Parameter;
import com.reactive.service.util.EncapsulatedValue;
import com.reactive.service.util.NameGenerator;

public class Data implements Serializable{
	private Object value;
	private Parameter parameter;
	
	private String name ;//generated by the name generator

	public Data() {
		name=NameGenerator.newName();
	}
	public Data(Boolean setCounter) {
		if(setCounter)name=NameGenerator.newName();
	}
	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Parameter getParameter() {
		return parameter;
	}
	
	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}
    public String getDisplayName(){
    	if(this.parameter.getShortName()!=null){
    		return parameter.getShortName();
    	}
    	return name;
    }
    
    public String getFullDisplayName(){
    	if(parameter.getService()!=null){
    		return parameter.getService().getName()+"."+this.getDisplayName();
    	}
    	return getDisplayName();
    }
	public String getName() {
		return name;
	}
	public Data clone(){
		Data d= new Data(false);
		d.name=name;
		d.parameter=parameter;
		d.value = value;
		if(value instanceof EncapsulatedValue){
			EncapsulatedValue ecVal = ((EncapsulatedValue) value).clone();
			ecVal.setContainerRef(d);
			d.value= ecVal;
		}
		return d;
	}

/*
	public void setName(String name) {
		this.name = name;
	}
*/	
	

}
