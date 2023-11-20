package com.consulner.app.api.mergesort;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import com.consulner.app.api.Constants;
import com.consulner.app.api.Handler;
import com.consulner.app.api.ResponseEntity;
import com.consulner.app.api.StatusCode;
import com.consulner.app.errors.ApplicationExceptions;
import com.consulner.app.errors.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.SortFunc;
import com.reactive.service.app.api.InMemoryWorkspace;
import com.reactive.service.assesment.ToolKit;
import com.reactive.service.model.configuration.Configuration;
import com.reactive.service.model.configuration.OutputWatcher;
import com.reactive.service.model.configuration.Task;
import com.reactive.service.model.specification.GAG;
import com.reactive.service.util.Context;
import com.reactive.service.util.Executor;
import com.reactive.service.util.Operation;
import com.sun.net.httpserver.HttpExchange;

public class SortInputWithAssessmentHandler extends Handler {

	public SortInputWithAssessmentHandler(ObjectMapper objectMapper, GlobalExceptionHandler exceptionHandler) {
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
			throw ApplicationExceptions
					.methodNotAllowed(
							"Method " + exchange.getRequestMethod() + " is not allowed for " + exchange.getRequestURI())
					.get();
		}

		OutputStream os = exchange.getResponseBody();
		os.write(response);
		os.close();
	}

	private ResponseEntity doPost(InputStream is) {
		// TODO Auto-generated method stub
		RandomInputRequest rq = super.readRequest(is, RandomInputRequest.class);
		long size = rq.getSize();
		String jsonData = "{\"size\":" + size + "}";
		System.out.println("posting data 1");
		String result = JsonHttpPost.postRequestAndReturnString(jsonData,
				Constants.SERVICE_NAME + "/api/mergesort/create-input");
		System.out.println("posting data 2");

		jsonData = result;
		OutputWatcher watcher = new OutputWatcher();
		result = JsonHttpPost.postRequestAndReturnString(jsonData,
				Constants.SERVICE_NAME + "/api/mergesort/sort-array");
		ArrayInput response = super.readRequest(new ByteArrayInputStream(result.getBytes()), ArrayInput.class);
		watcher.setExecutionToEnd();
		// make curl here

		System.out.println("finished to create data");
		return new ResponseEntity<>(watcher, getHeaders(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON),
				StatusCode.OK);

	}


}
