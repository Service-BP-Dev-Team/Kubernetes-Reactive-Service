package com.reactive.service.app.api;

import java.io.InputStream;
import java.io.OutputStream;

import com.consulner.app.api.Constants;
import com.consulner.app.api.Handler;
import com.consulner.app.api.ResponseEntity;
import com.consulner.app.api.StatusCode;
import com.consulner.app.errors.ApplicationExceptions;
import com.consulner.app.errors.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

public class ServiceMessageHandler extends Handler {

	public ServiceMessageHandler(ObjectMapper objectMapper, GlobalExceptionHandler exceptionHandler) {
		super(objectMapper, exceptionHandler);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void execute(HttpExchange exchange) throws Exception {
		// TODO Auto-generated method stub
        byte[] response;
        if ("POST".equals(exchange.getRequestMethod())) {
            ResponseEntity e = doPost(exchange.getRequestBody(),this.retrieveIpAddress(exchange));
            exchange.getResponseHeaders().putAll(e.getHeaders());
            exchange.sendResponseHeaders(e.getStatusCode().getCode(), 0);
            response = super.writeResponse(e.getBody());
           
        } else {
            throw ApplicationExceptions.methodNotAllowed(
                "Method " + exchange.getRequestMethod() + " is not allowed for " + exchange.getRequestURI()).get();
        }

        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
	}

	private ResponseEntity doPost(InputStream is, String clientIp) {
		// TODO Auto-generated method stub
		Message request= super.readRequest(is, Message.class);
		Object response;
		if(request.getType().equals(Message.BIND_MESSAGE_TYPE)) {
			response= new BindResponse();
			String ip = InMemoryWorkspace.processBindMessage(request.getBind());
			((BindResponse) response).setIpAddress(ip);
			((BindResponse) response).setClientIp(clientIp);
		}
		else if(request.getType().equals(Message.NOTIFICATION_MESSAGE_TYPE)) {
			InMemoryWorkspace.processInNotification(request.getNotification());
			response= new EmptyResponse();
		}
		else {
			InMemoryWorkspace.addCall(request.getServiceCall());
			response = new EmptyResponse();
		}

        return new ResponseEntity<>(response,
            getHeaders(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON), StatusCode.OK);
		
	}
	
	public String retrieveIpAddress(HttpExchange httpExchange) {
		// Retrieve the client's IP address
        String clientIP = httpExchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (clientIP == null || clientIP.isEmpty()) {
            clientIP = httpExchange.getRemoteAddress().getAddress().getHostAddress();
        }
        return clientIP;
	}
	
	

}
