package com.consulner.app.api.mergesort;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.reactive.service.app.api.Pair;
import com.reactive.service.app.api.ServiceCall;
import com.reactive.service.model.configuration.Configuration;
import com.reactive.service.model.configuration.OutputWatcher;
import com.reactive.service.model.configuration.Task;
import com.reactive.service.model.specification.GAG;
import com.reactive.service.util.Console;
import com.reactive.service.util.Context;
import com.reactive.service.util.Executor;
import com.reactive.service.util.FileWriting;
import com.reactive.service.util.Operation;
import com.sun.net.httpserver.HttpExchange;

public class MergeSortEnhancedWithAssessmentHandler extends Handler {

	public MergeSortEnhancedWithAssessmentHandler(ObjectMapper objectMapper, GlobalExceptionHandler exceptionHandler) {
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
		RandomInputRequest rq= super.readRequest(is, RandomInputRequest.class);
		int size =rq.getSize();
		System.out.println("start data execution");

		Console.printMemory();
		Pair<OutputWatcher,Executor> p=createTheTask(size);
		OutputWatcher watcher = p.getFirst();
		Executor exec = p.getSecond();
		int timeElapsed = 0;
		while(!watcher.isEnded()) {
			try {
				
				Thread.sleep(1000);
				timeElapsed+=1000;
				if(timeElapsed >= 100000) { // stop after 100 seconds
					String log = p.getSecond().getConfiguration().getRoot().getJsonRepresentation();
					// write log on a file
					FileWriting.writeStringToFile(log,"./spec-merge-sort-enhanced/log.json");
					break;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//compute statistics
		HashMap stat = Statistics.getNumberOfSuccessAndFailedNode(exec.getConfiguration().getRoot());
        watcher.setAdditionnalExecutionInformation(stat);
		exec.clearAllData(); // we free up the memory
        // we will compute all the statistics here
        System.out.println("finished to create data");
        System.gc();
        Console.printMemory();
        return new ResponseEntity<>(watcher,
            getHeaders(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON), StatusCode.OK);
		
	}
	
	public Pair<OutputWatcher,Executor> createTheTask(int size) {
		GAG g = InMemoryWorkspace.getGagWithRootFolder("spec-merge-sort-enhanced");
		// System.out.println(g);

		Context nContext = new Context();
		Executor exec = new Executor(nContext, g);
		exec.setSubExecutor(true); // we do this to prevent the executor to clear the memory itself
		Configuration conf = new Configuration();

		ArrayList<Integer> inputTable = ToolKit.generateArray(size);
//ArrayList<Integer> inputTable = ToolKit.generateArray(10);
		Hashtable<String, Object> inputs = new Hashtable<>();
		inputs.put("inp_list", inputTable);
// create the root task
		Task root = Operation.createTask(g.getServices().get(0), inputs);
		conf.setRoot(root);
		exec.setConfiguration(conf);
		ServiceCall sc = new ServiceCall();
		sc.setTask(root);
		exec.setServiceCallId(sc.getId());
		InMemoryWorkspace.inMemoryCalls.put(sc.getId(), exec);
		OutputWatcher watcher = new OutputWatcher();
		root.getOutputs().get(0).setWatcher(watcher);
		exec.execute();
		return new Pair(watcher,exec);
	}

	
	

}
