package com.reactive.service.model.specification;

import java.io.Serializable;

public class Equation implements Serializable{
	
	private IdExpression leftpart;
	private Expression rightpart;
	

	public IdExpression getLeftpart() {
		return leftpart;
	}
	public void setLeftpart(IdExpression leftpart) {
		this.leftpart = leftpart;
	}
	
	public Expression getRightpart() {
		return rightpart;
	}
	public void setRightpart(Expression rightpart) {
		this.rightpart = rightpart;
	}
	
}
