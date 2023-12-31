package com.reactive.service.model.specification;

import java.io.Serializable;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;



public class Service implements Serializable{
      
	private String name;
	private Boolean axiom;
	
	private ArrayList<Parameter> inputParameters;
	private ArrayList<Parameter> outputParameters;
	private ArrayList<DecompositionRule> rules;
	
	// fields that apply when the service is remote
	
	private Boolean remote=false; // default false
	private String kubename;
	
	//just to add specific action when triggering a service as axiom for specific example
	private String exampleSignature="";
	
	public Service() {
		rules = new ArrayList<DecompositionRule>();
		inputParameters= new ArrayList<Parameter>();
		outputParameters= new ArrayList<Parameter>();
		axiom=false;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public Boolean isAxiom() {
		return axiom;
	}
	public void setAxiom(Boolean axiom) {
		this.axiom = axiom;
	}

	public ArrayList<Parameter> getInputParameters() {
		return inputParameters;
	}
	public void setInputParameters(ArrayList<Parameter> inputParameters) {
		this.inputParameters = inputParameters;
	}

	public ArrayList<Parameter> getOutputParameters() {
		return outputParameters;
	}
	public void setOutputParameters(ArrayList<Parameter> outputParameters) {
		this.outputParameters = outputParameters;
	}
	
	@JsonIgnore
	public ArrayList<DecompositionRule> getRules() {
		return rules;
	}
	public void setRules(ArrayList<DecompositionRule> rules) {
		this.rules = rules;
	}
	
	public Boolean isRemote() {
		return remote;
	}
	public void setRemote(Boolean remote) {
		this.remote = remote;
	}
	
	public String getExampleSignature() {
		return exampleSignature;
	}
	
	public void setExampleSignature(String exampleSignature) {
		this.exampleSignature = exampleSignature;
	}
	
	
	public Parameter getInputParameterByName(String name) {
		Parameter result=null;
		for(Parameter par: inputParameters) {
			if(par.getName().equals(name)) {
				result=par;
				break;
			}
		}
		return result;
	}
	
	public Parameter getOutputParameterByName(String name) {
		Parameter result=null;
		for(Parameter par: outputParameters) {
			if(par.getName().equals(name)) {
				result=par;
				break;
			}
		}
		return result;
	}
	
	public Parameter getParameterByName(String name) {
		Parameter result = this.getInputParameterByName(name);
		if(result==null) {result=this.getOutputParameterByName(name);}
		return result;
	}
	public String getKubename() {
		return kubename;
	}
	public void setKubename(String kubename) {
		this.kubename = kubename;
	}
	public Boolean getAxiom() {
		return axiom;
	}
	public Boolean getRemote() {
		return remote;
	}
	
	
	
	
}