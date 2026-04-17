package com.reactive.service.app.api;

import com.reactive.service.model.configuration.Data;

public class Notification {
	private String globalId;
	private String sender;
	private String receiver;
	private Data data;
	public String getGlobalId() {
		return globalId;
	}
	public void setGlobalId(String globalId) {
		this.globalId = globalId;
	}
	public Data getData() {
		return data;
	}
	public void setData(Data data) {
		this.data = data;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String caller) {
		this.sender = caller;
	}
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	
	
	
	
	
}
