package com.reactive.service.model.specification;

import java.util.ArrayList;


public class FunctionExpression extends Expression {
	
	private FunctionDeclaration function;
	private ArrayList<IdExpression> idExpressions;
	private boolean threadFunction;
	public FunctionExpression() {
		idExpressions= new ArrayList<IdExpression>();
	}
	
	public FunctionDeclaration getFunction() {
		return function;
	}
	public void setFunction(FunctionDeclaration function) {
		this.function = function;
	}
	
	public ArrayList<IdExpression> getIdExpressions() {
		return idExpressions;
	}
	public void setIdExpressions(ArrayList<IdExpression> idExpressions) {
		this.idExpressions = idExpressions;
	}

	public boolean isThreadFunction() {
		return threadFunction;
	}

	public void setThreadFunction(boolean threadFunction) {
		this.threadFunction = threadFunction;
	}
	
	

}
