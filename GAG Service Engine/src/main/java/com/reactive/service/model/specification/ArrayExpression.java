package com.reactive.service.model.specification;

/*
 * For now we don't authorize array expression to be direct parameter of 
 * function expression. They will be direct members of equation of type
 * IdExpression = IdExpression
 * 
 */
public class ArrayExpression extends IdExpression{
	
	private IdExpression index;

	public IdExpression getIndex() {
		return index;
	}

	public void setIndex(IdExpression index) {
		this.index = index;
	}
	
	public String asString() {
		//return this.getServiceInstance().getService().getName()+"."+this.getParameterName() +"["+index.asString()+"]";
		return this.getServiceInstance().getName()+"."+this.getParameterName() +"["+index.asString()+"]";
	}
	
	

}
