package com.reactive.service.model.specification;

import java.util.ArrayList;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlIDREF;
import jakarta.xml.bind.annotation.XmlRootElement;


public class FunctionExpression extends Expression {
	
	private FunctionDeclaration function;
	private ArrayList<IdExpression> idExpressions;
	
	public FunctionExpression() {
		idExpressions= new ArrayList<IdExpression>();
	}
	@XmlAttribute @XmlIDREF
	public FunctionDeclaration getFunction() {
		return function;
	}
	public void setFunction(FunctionDeclaration function) {
		this.function = function;
	}
	@XmlElement(name="arg")
	public ArrayList<IdExpression> getIdExpressions() {
		return idExpressions;
	}
	public void setIdExpressions(ArrayList<IdExpression> idExpressions) {
		this.idExpressions = idExpressions;
	}
	
	

}
