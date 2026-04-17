package com.reactive.service.model.specification;

import java.io.Serializable;

public class FunctionDeclaration implements Serializable{
	private static FunctionDeclaration REFERENCE_DATAGROUP;
	private String name;
	private String location;
	private String method;
	private boolean multiOutput;
	private int ouputSize;
	static {
		REFERENCE_DATAGROUP = new FunctionDeclaration();
		REFERENCE_DATAGROUP.setName("~ref");
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public boolean isMultiOutput() {
		return multiOutput;
	}
	public void setMultiOutput(boolean multiOutput) {
		this.multiOutput = multiOutput;
	}
	public int getOuputSize() {
		return ouputSize;
	}
	public void setOuputSize(int ouputSize) {
		this.ouputSize = ouputSize;
	}
	public static FunctionDeclaration getREFERENCE_DATAGROUP() {
		return REFERENCE_DATAGROUP;
	}
	public static void setREFERENCE_DATAGROUP(FunctionDeclaration rEFERENCE_DATAGROUP) {
		REFERENCE_DATAGROUP = rEFERENCE_DATAGROUP;
	}
	
	
}
