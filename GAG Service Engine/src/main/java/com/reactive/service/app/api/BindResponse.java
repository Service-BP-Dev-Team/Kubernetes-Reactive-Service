package com.reactive.service.app.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class BindResponse {
	String ipAddress;
	String clientIp;

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}
	
	@JsonIgnore
	public String getClientIpWithPort() {
		return clientIp+":8000";
	}
	
	
	
	
}
