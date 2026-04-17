package com.reactive.service.parser;

public class YAMLLocalFunction {
	private String classPath;
	private String method;
	private String label;
	private String endpoint;
	private boolean multiOutput;
	private int outputSize;
	public String getClassPath() {
		return classPath;
	}
	public void setClassPath(String classPath) {
		this.classPath = classPath;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	public boolean isMultiOutput() {
		return multiOutput;
	}
	public void setMultiOutput(boolean multiOutput) {
		this.multiOutput = multiOutput;
	}
	public int getOutputSize() {
		return outputSize;
	}
	public void setOutputSize(int outputSize) {
		this.outputSize = outputSize;
	}
	
	
	
}
