package com.consulner.app.api.mergesort;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonHttpPost {

	
public static InputStream postRequest(String jsonData,String urlString) {
        
        try {
            // Set the URL to send the JSON data to
            URL url = new URL(urlString);
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            // Write the JSON data to the request body
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonData.getBytes());
            outputStream.flush();
            
            // Get the response from the server
            int responseCode = connection.getResponseCode();
            return connection.getInputStream();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ByteArrayInputStream("{}".getBytes()); //return an empty json string in case of errors
    }
	
    public static String postRequestAndReturnString(String jsonData,String urlString) {
        
        try {
            // Set the URL to send the JSON data to
            URL url = new URL(urlString);
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            // Write the JSON data to the request body
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonData.getBytes());
            outputStream.flush();
            
            // Get the response from the server
            int responseCode = connection.getResponseCode();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            connection.disconnect();
            
            // Print the response

            return response.toString();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "{}"; //return an empty json string in case of errors
    }
}
