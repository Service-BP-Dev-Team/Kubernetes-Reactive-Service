package com.reactive.service.model.configuration;

import java.util.ArrayList;

import com.reactive.service.model.specification.FunctionDeclaration;

public class PendingLocalFunctionComputation {
	private Data dataToCompute;
	private ArrayList<Data> actualParameters;
	private FunctionDeclaration functionDeclaration;
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

	
}
