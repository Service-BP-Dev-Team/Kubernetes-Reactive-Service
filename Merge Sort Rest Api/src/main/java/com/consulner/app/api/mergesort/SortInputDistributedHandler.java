package com.consulner.app.api.mergesort;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.consulner.app.api.Constants;
import com.consulner.app.api.Handler;
import com.consulner.app.api.ResponseEntity;
import com.consulner.app.api.StatusCode;
import com.consulner.app.errors.ApplicationExceptions;
import com.consulner.app.errors.GlobalExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

public class SortInputDistributedHandler extends Handler {

	public SortInputDistributedHandler(ObjectMapper objectMapper, GlobalExceptionHandler exceptionHandler) {
		super(objectMapper, exceptionHandler);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void execute(HttpExchange exchange) throws Exception {
		// TODO Auto-generated method stub
        byte[] response;
        if ("POST".equals(exchange.getRequestMethod())) {
            ResponseEntity e = doPost(exchange.getRequestBody());
            exchange.getResponseHeaders().putAll(e.getHeaders());
            exchange.sendResponseHeaders(e.getStatusCode().getCode(), 0);
            response = super.writeResponse(e.getBody());
            System.out.println("value writed as bytes");
        } else {
            throw ApplicationExceptions.methodNotAllowed(
                "Method " + exchange.getRequestMethod() + " is not allowed for " + exchange.getRequestURI()).get();
        }

        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
	}

	private ResponseEntity doPost(InputStream is) {
		// TODO Auto-generated method stub
		ArrayInput rq= super.readRequest(is, ArrayInput.class);
		
		//ArrayInput response = SequentialSort.sort(rq);
		ArrayInput response = DistributedSort.sort(rq, this.objectMapper);

        //  make curl here
        
        System.out.println("finished to sort data");
        return new ResponseEntity<>(response,
            getHeaders(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON), StatusCode.OK);
		
	}
	
	

}
