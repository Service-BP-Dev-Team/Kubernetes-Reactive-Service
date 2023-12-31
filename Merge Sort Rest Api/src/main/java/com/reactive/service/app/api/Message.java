package com.reactive.service.app.api;
import java.io.IOException;

import com.consulner.app.Configuration;
import com.consulner.app.api.Constants;
import com.consulner.app.api.mergesort.JsonHttpPost;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Message {

	private String type;
	private Notification notification;
	private Bind bind;
	private ServiceCall serviceCall;
	public static final String BIND_MESSAGE_TYPE="BIND_MESSAGE_TYPE";
	public static final String NOTIFICATION_MESSAGE_TYPE="NOTIFICATION_MESSAGE_TYPE";
	public static final String SERVICECALL_MESSAGE_TYPE="SERVICECALL_MESSAGE_TYPE";
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Notification getNotification() {
		return notification;
	}
	public void setNotification(Notification notification) {
		this.notification = notification;
	}
	public Bind getBind() {
		return bind;
	}
	public void setBind(Bind bind) {
		this.bind = bind;
	}
	public ServiceCall getServiceCall() {
		return serviceCall;
	}
	public void setServiceCall(ServiceCall serviceCall) {
		this.serviceCall = serviceCall;
	}
	
	public static String sendMessage(Message message) {
		ObjectMapper objmapper=Configuration.getObjectMapper();
		String result=null;
		//String sender = InMemoryWorkspace.getHostIp();
		String receiver = null;
		if(message.type.equals(Message.NOTIFICATION_MESSAGE_TYPE)) {
			receiver=message.notification.getReceiver();
		}
		else if(message.type.equals(Message.SERVICECALL_MESSAGE_TYPE)) {
			receiver=message.serviceCall.getReceiver();
		}else {
			receiver = message.bind.getReceiver();
		}
		try {
			String content =objmapper.writeValueAsString(message);
			//System.out.println(content);
			result = JsonHttpPost.postRequestAndReturnString(content, "http://"+receiver+"/api/service");

			//objmapper.readValue(result, Message.class);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	

	
}
