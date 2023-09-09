package com.reactive.service.model.configuration;

import java.util.ArrayList;

import com.reactive.service.model.specification.FunctionDeclaration;
import com.reactive.service.util.Operation;

public class PendingLocalFunctionComputation {
	private Data dataToCompute;
	private ArrayList<Data> actualParameters;
	private FunctionDeclaration functionDeclaration;
	private boolean isIdFunction=false;
	private boolean terminated=false; //this boolean help to know if the function computation is terminated

	public PendingLocalFunctionComputation() {
		actualParameters =new ArrayList<Data>();
	}
	public Data getDataToCompute() {
		return dataToCompute;
	}

	public void setDataToCompute(Data dataToCompute) {
		this.dataToCompute = dataToCompute;
	}

	public ArrayList<Data> getActualParameters() {
		return actualParameters;
	}

	public void setActualParameters(ArrayList<Data> actualParameters) {
		this.actualParameters = actualParameters;
	}

	public FunctionDeclaration getFunctionDeclaration() {
		return functionDeclaration;
	}

	public void setFunctionDeclaration(FunctionDeclaration functionDeclaration) {
		this.functionDeclaration = functionDeclaration;
	}
	public boolean isTerminated() {
		return terminated;
	}
	public void setTerminated(boolean terminated) {
		this.terminated = terminated;
	}
	public boolean isIdFunction() {
		return isIdFunction;
	}
	public void setIdFunction(boolean isIdFunction) {
		this.isIdFunction = isIdFunction;
	}
	
	public boolean isReady() {
		boolean result = true;
		for(Data d:actualParameters) {
			if(!d.isDefined()) {
				return false;
			}
		}
		return result;
	}
	
	public Object execute() {
		Object result=null;
		if(isReady()) {
			
			if(isIdFunction) {
				result = actualParameters.get(0).getValue();
				System.out.println(result);
			}
			else {
				ArrayList<Object> params= new ArrayList<>();
				for(Data d : actualParameters) {
					params.add(d.getValue());
				}
				result = Operation.executeMethodWithReflexion(functionDeclaration.getLocation(), functionDeclaration.getMethod(), params);
				//System.out.println(result);
			}
			dataToCompute.setDefined(true);
			dataToCompute.setValue(result);
		}
		return result;
	}
	

	
	
}
