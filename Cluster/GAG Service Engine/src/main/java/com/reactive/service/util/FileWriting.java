package com.reactive.service.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileWriting {

	public static void writeStringToFile(String content, String filePath) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			writer.write(content);
			System.out.println("Text written to the file successfully.");
		} catch (IOException e) {
			System.out.println("An error occurred while writing to the file: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		String content = "Hello, world!"; // Text to write to the file
		String filePath = "output.txt"; // Specify the file name or path

		writeStringToFile(content, filePath);
	}
}