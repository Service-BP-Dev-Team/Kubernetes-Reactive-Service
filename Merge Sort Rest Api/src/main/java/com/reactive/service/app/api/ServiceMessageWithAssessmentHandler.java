package com.reactive.service.app.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import com.consulner.app.api.Constants;
import com.consulner.app.api.Handler;
import com.consulner.app.api.ResponseEntity;
import com.consulner.app.api.StatusCode;
import com.consulner.app.api.mergesort.ArrayInput;
import com.consulner.app.errors.ApplicationExceptions;
import com.consulner.app.errors.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.ObjectMessage;
import com.reactive.service.model.configuration.Configuration;
import com.reactive.service.model.configuration.OutputWatcher;
import com.reactive.service.model.configuration.Task;
import com.reactive.service.model.specification.GAG;
import com.reactive.service.util.Context;
import com.reactive.service.util.Executor;
import com.reactive.service.util.Operation;
import com.sun.net.httpserver.HttpExchange;

public class ServiceMessageWithAssessmentHandler extends Handler {

	public ServiceMessageWithAssessmentHandler(ObjectMapper objectMapper, GlobalExceptionHandler exceptionHandler) {
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
		ArrayInput request= super.readRequest(is, ArrayInput.class);
	
		Pair<Task,Pair<OutputWatcher,OutputWatcher>> launchedTask = buildTheTask(request.getSize());
		OutputWatcher response ;
		OutputWatcher outWatcher1 = launchedTask.getValue().getFirst();
		OutputWatcher outWatcher2 = launchedTask.getValue().getSecond();
		while(! (outWatcher1.isEnded() && outWatcher2.isEnded()  )) {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(outWatcher1.getEnd() > outWatcher2.getEnd()) {
			response =outWatcher1;
		}else {
			response = outWatcher2;
		}
        return new ResponseEntity<>(response,
            getHeaders(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON), StatusCode.OK);
		
	}
	
	private Pair<Task,Pair<OutputWatcher,OutputWatcher>> buildTheTask(long sortsize) {
		GAG g= InMemoryWorkspace.getGagWithRootFolder("spec-merge-sort");
		
		Executor defaulte= new Executor();
		defaulte.setGag(g);
		Context ctx = new Context();
		ctx.setExecutor(defaulte);
		defaulte.setContext(ctx);
		Hashtable<String, Object> inputs = new Hashtable<String,Object>();
		ArrayInput arr = ArrayInput.createRandomInput(sortsize);
		ObjectMessage obj=new ObjectMessage();
		obj.setTable(arr.getArray());
		inputs.put("a", obj);
		Task t= Operation.createTask(g.getServices().get(0), inputs);
		Configuration conf = new Configuration();
		conf.setRoot(t);
		OutputWatcher watcher1 = new OutputWatcher();
		OutputWatcher watcher2 = new OutputWatcher();
		t.getOutputs().get(0).setWatcher(watcher1);
		t.getOutputs().get(1).setWatcher(watcher2);
		defaulte.setConfiguration(conf);
		// execute in a separate Thread
		 Thread separateThread = new Thread(new Runnable() {
	            public void run() {
	                // Code to be executed in the separate thread
	                System.out.println("Running in a separate thread");
	                defaulte.execute();
	            }
	        });

	        // Start the separate thread
	        separateThread.start();
		
		return new Pair<Task,Pair<OutputWatcher,OutputWatcher>>(t,new Pair<OutputWatcher,OutputWatcher>(watcher1,watcher2));
	}
	

}
