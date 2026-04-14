package com.reactive.service.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;

import static com.consulner.app.Configuration.getObjectMapper;
public class Logger {

	public static void log(Object obj) {
		String variable="";
		  try {
			variable = getObjectMapper().writeValueAsString(obj);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} // The variable you want to log

	        try {
	            // Create a temporary file
	            File tempFile = File.createTempFile("variableLog", ".txt");

	            // Open a FileWriter to write to the temporary file
	            FileWriter writer = new FileWriter(tempFile);

	            // Write the variable's value to the file
	            writer.write(variable);

	            // Close the FileWriter
	            writer.close();

	            // Print the path of the temporary file
	            System.out.println("Variable logged to temporary file: " + tempFile.getAbsolutePath());
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	}
}

