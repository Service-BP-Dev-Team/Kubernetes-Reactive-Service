package com.consulner.app.api.mergesort;

//object json that will be passed through the request as input
public class RandomInputRequest {
	int size;
	
	public RandomInputRequest() {
		this.size=1;
	}
	public RandomInputRequest(int size) {
		this.size=size;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	

}
