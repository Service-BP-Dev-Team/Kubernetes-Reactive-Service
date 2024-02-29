package com.reactive.service.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

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
	
	public static void printMemory() {
	       MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
	        MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();
	        MemoryUsage nonHeapMemoryUsage = memoryBean.getNonHeapMemoryUsage();

	        System.out.println("Heap Memory Usage:");
	        System.out.println("  Initial: " + heapMemoryUsage.getInit() / (1024 * 1024) + " MB");
	        System.out.println("  Used: " + heapMemoryUsage.getUsed() / (1024 * 1024) + " MB");
	        System.out.println("  Committed: " + heapMemoryUsage.getCommitted() / (1024 * 1024) + " MB");
	        System.out.println("  Max: " + heapMemoryUsage.getMax() / (1024 * 1024) + " MB");

	        System.out.println("\nNon-Heap Memory Usage:");
	        System.out.println("  Initial: " + nonHeapMemoryUsage.getInit() / (1024 * 1024) + " MB");
	        System.out.println("  Used: " + nonHeapMemoryUsage.getUsed() / (1024 * 1024) + " MB");
	        System.out.println("  Committed: " + nonHeapMemoryUsage.getCommitted() / (1024 * 1024) + " MB");
	        System.out.println("  Max: " + nonHeapMemoryUsage.getMax() / (1024 * 1024) + " MB");
	    
	}
}
