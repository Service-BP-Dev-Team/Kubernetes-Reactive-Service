package com.reactive.service.parser;

import java.util.List;

public class YAMLSpec {
	private String kind;
	private String name;
	private List<YAMLParameter> inputs;
	private List<YAMLParameter> outputs;
	private String Service;
	private List<String> actions;
	private List<YAMLLocalFunction> functions;
	private YAMLGuard guard;
	private boolean remote =false;
	private String kubename;
	public String getKind() {
		return kind;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<YAMLParameter> getInputs() {
		return inputs;
	}
	public void setInputs(List<YAMLParameter> inputs) {
		this.inputs = inputs;
	}
	public List<YAMLParameter> getOutputs() {
		return outputs;
	}
	public void setOutputs(List<YAMLParameter> outputs) {
		this.outputs = outputs;
	}
	public String getService() {
		return Service;
	}
	public void setService(String service) {
		Service = service;
	}
	public List<String> getActions() {
		return actions;
	}
	public void setActions(List<String> actions) {
		this.actions = actions;
	}
	public List<YAMLLocalFunction> getFunctions() {
		return functions;
	}
	public void setFunctions(List<YAMLLocalFunction> functions) {
		this.functions = functions;
	}
	public boolean isRemote() {
		return remote;
	}
	public void setRemote(boolean remote) {
		this.remote = remote;
	}
	public YAMLGuard getGuard() {
		return guard;
	}
	public void setGuard(YAMLGuard guard) {
		this.guard = guard;
	}
	public String getKubename() {
		return kubename;
	}
	public void setKubename(String kubename) {
		this.kubename = kubename;
	}
	
	
	
	
}
