package com.consulner.app;

import static com.consulner.app.Configuration.getErrorHandler;
import static com.consulner.app.Configuration.getObjectMapper;

import static com.consulner.app.api.ApiUtils.splitQuery;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.consulner.app.api.mergesort.MergeSortEnhancedWithAssessmentHandler;

import com.reactive.service.app.api.ServiceMessageHandler;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

public class Application {

    public static void main(String[] args) throws IOException {
    	// Create an ExecutorService with a fixed number of threads
        int numThreads = 3000; // Number of threads for parallel processing
        int port = 8000;
        //ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        ExecutorService executor = Executors.newCachedThreadPool();
        // Create an HttpServer instance
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Set the server's executor to the created ExecutorService
        server.setExecutor(executor);
      
        MergeSortEnhancedWithAssessmentHandler mergeSortEnhancedWithAssessmentHandler = new MergeSortEnhancedWithAssessmentHandler(getObjectMapper(), getErrorHandler());
        
        ServiceMessageHandler serviceMessageHandler = new ServiceMessageHandler(getObjectMapper(), getErrorHandler());

        server.createContext("/api/service",serviceMessageHandler::handle);
        server.createContext("/api/service/merge-sort-enhanced/assesment", mergeSortEnhancedWithAssessmentHandler::handle);
        //server.setExecutor(null); // creates a default executor
        server.start();
    }
}