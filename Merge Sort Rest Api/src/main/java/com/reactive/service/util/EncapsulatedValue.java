package com.reactive.service.util;

import java.io.Serializable;

import com.reactive.service.model.configuration.Configuration;
import com.reactive.service.model.configuration.Data;
import com.reactive.service.model.configuration.PendingLocalFunctionComputation;

// A class for better variable reference management
public class EncapsulatedValue implements Serializable {

	private Object value ;
	private EncapsulatedValue next;
	private Data containerRef; // the data to which this object is a value;
	// the program can work whitout this. It is only need for graphical debug;
	
	public EncapsulatedValue() {
		value=null;
		next = null;
	}
	public EncapsulatedValue(Object val) {
		value=val;
		next = null;
	}
	
	public void addReference(EncapsulatedValue EV) {
		next=EV;
	}

	public Object getValue() {
		Object val = value;
		EncapsulatedValue myNext = next;
		while(myNext!=null) {
			val=myNext.value;
			myNext=myNext.next;
		}
		if(val!=null){value=val;};//refresh Value
		return val;
	}

	public void setValue(Object value) {
		this.value = value;
		//this.next = null ; putting the next to null when setting value is optimal for execution
		// but not for debug 
	}
	
	public boolean isNull() {
		
		return (getValue()==null);
	}
	
	// return a data or a computing function
	public static Object getComputingReference(Data d,Configuration conf) {
		EncapsulatedValue ecD=(EncapsulatedValue) d.getValue();
		Object result=null;
		if(ecD.getAssociatedData()==null) {
			// look now for a function computing this data
			PendingLocalFunctionComputation func=null;
			for(int i=0;i<conf.getPendingLocalComputations().size();i++) {
				PendingLocalFunctionComputation el = conf.getPendingLocalComputations().get(i);
				if(el.getDataToCompute().equals(d)) {
					func=el;
					break;
				}
				
			}
			result=func;
			
		}else {
			result=ecD.getAssociatedData();
		}
		return result;
	}
	
	//return wheter a data is a global input or not
	public static boolean isGlobalInput(Data d, Configuration conf) {
		return (getComputingReference(d,conf)==null);
	}
	
	
	public Data getContainerRef() {
		return containerRef;
	}
	public void setContainerRef(Data containerRef) {
		this.containerRef = containerRef;
	}
	
	public Data getAssociatedData() {
		EncapsulatedValue myNext=next;
		if(myNext!=null) {
			return myNext.containerRef;
		}
		return null;
	}
	
	public EncapsulatedValue clone (){
		EncapsulatedValue enc = new EncapsulatedValue();
		enc.containerRef=containerRef;
		enc.next=next;
		enc.value=value;
		return enc;
	}
	
	
	
}
