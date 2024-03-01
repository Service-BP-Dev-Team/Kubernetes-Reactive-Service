package com.reactive.service.app.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.consulner.app.Configuration;

public class JsonHttpPost {

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
            //byte[] bytesToSend = GzipCompressor.compress(jsonData);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

            writer.write(jsonData);
            writer.flush();
            //outputStream.write(jsonData.get);
            //outputStream.write(bytesToSend);
           // outputStream.flush();
            
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
    
public static String postRequestAndReturnStringWithObject(Message message,String urlString) {
        
        try {
            // Set the URL to send the JSON data to
            URL url = new URL(urlString);
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            
            // Write the JSON data to the request body
            OutputStream outputStream = connection.getOutputStream();
            ObjectMapper objmapper=Configuration.getObjectMapper();
            //outputStream.write(jsonData.get);
            //outputStream.write(bytesToSend);
            objmapper.writeValue(outputStream, message);
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
