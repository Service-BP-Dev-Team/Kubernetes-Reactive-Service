package com.consulner.app;

import static com.consulner.app.Configuration.getErrorHandler;
import static com.consulner.app.Configuration.getObjectMapper;
import static com.consulner.app.Configuration.getUserService;
import static com.consulner.app.api.ApiUtils.splitQuery;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.consulner.app.api.mergesort.BuildRandomInputHandler;
import com.consulner.app.api.mergesort.SortInputDistributedHandler;
import com.consulner.app.api.mergesort.SortInputHandler;
import com.consulner.app.api.user.RegistrationHandler;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

class Application {

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
        RegistrationHandler registrationHandler = new RegistrationHandler(getUserService(), getObjectMapper(),
            getErrorHandler());
        BuildRandomInputHandler randomInputHandler = new BuildRandomInputHandler(getObjectMapper(), getErrorHandler()); 
        SortInputHandler sortInputHandler = new SortInputHandler(getObjectMapper(), getErrorHandler());
        SortInputDistributedHandler sortInputDistributedHandler = new SortInputDistributedHandler(getObjectMapper(), getErrorHandler());
        server.createContext("/api/users/register", registrationHandler::handle);
        server.createContext("/api/mergesort/create-input", randomInputHandler::handle);
        server.createContext("/api/mergesort/sort", sortInputHandler::handle);
        server.createContext("/api/mergesort/sort-array", sortInputDistributedHandler::handle);

        HttpContext context =server.createContext("/api/hello", (exchange -> {

            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, List<String>> params = splitQuery(exchange.getRequestURI().getRawQuery());
                String noNameText = "Anonymous";
                String name = params.getOrDefault("name", List.of(noNameText)).stream().findFirst().orElse(noNameText);
                String respText = String.format("Hello %s!", name);
                exchange.sendResponseHeaders(200, respText.getBytes().length);
                OutputStream output = exchange.getResponseBody();
                output.write(respText.getBytes());
                output.flush();
            } else {
                exchange.sendResponseHeaders(405, -1);// 405 Method Not Allowed
            }
            exchange.close();
        }));
        context.setAuthenticator(new BasicAuthenticator("myrealm") {
            @Override
            public boolean checkCredentials(String user, String pwd) {
                return user.equals("admin") && pwd.equals("admin");
            }
        });

        //server.setExecutor(null); // creates a default executor
        server.start();
    }
}
