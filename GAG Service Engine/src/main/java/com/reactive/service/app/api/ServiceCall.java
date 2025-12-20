package com.reactive.service.app.api;

import java.util.UUID;

import com.reactive.service.model.configuration.Task;

public class ServiceCall {

	private String id; // !important each service call has its own id in order to manage notification
	private String sender;
	private String receiver;
	private Task task;
	
	public ServiceCall() {
		id = UUID.randomUUID().toString();
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Task getTask() {
		return task;
	}
	public void setTask(Task task) {
		this.task = task;
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
