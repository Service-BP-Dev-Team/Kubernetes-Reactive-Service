package com.reactive.service.model.specification;

import java.io.Serializable;
import java.util.ArrayList;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlIDREF;

public class DecompositionRule implements Serializable {

	private String name;
	private Guard guard;
	private ArrayList<Equation> semantics;
	private ArrayList<ServiceInstance> serviceInstances; 
	private ServiceInstance currentServiceInstance;
	private ArrayList<FunctionDeclaration> functionDeclarations;
	private ArrayList<IdExpression> data;
	private int instanceCounter=0;
	
	public DecompositionRule() {
		serviceInstances = new ArrayList<ServiceInstance>();
		functionDeclarations = new ArrayList<FunctionDeclaration>();
		data = new ArrayList<IdExpression>();
		semantics = new ArrayList<Equation>();
	}
	@XmlAttribute
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Guard getGuard() {
		return guard;
	}
	public void setGuard(Guard guard) {
		this.guard = guard;
	}
	
	
	public ArrayList<ServiceInstance> getServiceInstances() {
		return serviceInstances;
	}
	public void setServiceInstances(ArrayList<ServiceInstance> serviceInstances) {
		this.serviceInstances = serviceInstances;
	}
	public ArrayList<FunctionDeclaration> getFunctionDeclarations() {
		return functionDeclarations;
	}
	public void setFunctionDeclarations(ArrayList<FunctionDeclaration> functionDeclarations) {
		this.functionDeclarations = functionDeclarations;
	}
	public int getInstanceCounter() {
		return instanceCounter;
	}
	public void setInstanceCounter(int instanceCounter) {
		this.instanceCounter = instanceCounter;
	}
	
	public void incrementInstanceCounter() {
		this.instanceCounter++;
	}
	public ServiceInstance getCurrentServiceInstance() {
		return currentServiceInstance;
	}
	public void setCurrentServiceInstance(ServiceInstance currentServiceInstance) {
		this.currentServiceInstance = currentServiceInstance;
	}
	public ArrayList<IdExpression> getData() {
		return data;
	}
	public void setData(ArrayList<IdExpression> data) {
		this.data = data;
	}
	public ArrayList<Equation> getSemantics() {
		return semantics;
	}
	public void setSemantics(ArrayList<Equation> semantics) {
		this.semantics = semantics;
	}
	
	public String getSemanticAsString() {
		String result = "";
		for(Equation eq: semantics) {
			String line = eq.getLeftpart().asString()+" = ";
			if(eq.getRightpart() instanceof IdExpression) {
				line+=((IdExpression)eq.getRightpart()).asString()+";\n";
			}else {
				FunctionExpression fE= (FunctionExpression) eq.getRightpart();
				line+=fE.getFunction().getName()+" ";
				for(IdExpression ide : fE.getIdExpressions()) {
					line+= ide.asString()+" ";
				}
				line+=";\n";
			}
			result+=line;
		}
		return result;
	}
	
	
}
