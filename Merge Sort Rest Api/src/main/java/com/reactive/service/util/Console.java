package com.reactive.service.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Console {

	public static void debug(Object obj) {
		System.out.println(obj.toString());
	}

	

	public static String readConsoleLine() {
		String result = "";
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		// Reading data using readLine
		try {
			result = reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public static String readConsoleLine(String msg) {
		Console.debug(msg);
		return Console.readConsoleLine();
	}
}
