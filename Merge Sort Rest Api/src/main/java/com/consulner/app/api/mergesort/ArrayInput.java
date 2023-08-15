package com.consulner.app.api.mergesort;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
// this class allow to generate a random array
public class ArrayInput {

	private long size;
	private ArrayList<Long> array;
	
	public static ArrayInput createRandomInput (long size) {
		ArrayInput rd = new ArrayInput();
		rd.size=size;
		rd.array = new ArrayList<Long>();
	 ThreadLocalRandom random = ThreadLocalRandom.current();
		for(int i=0;i<rd.size;i++) {
			// Generate a random integer between 0 and 9
	        long randomNumber = random.nextLong(0,rd.size);
			rd.array.add(randomNumber);
		}
		return rd;
		
	}
	
	
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public ArrayList<Long> getArray() {
		return array;
	}
	public void setArray(ArrayList<Long> array) {
		this.array = array;
	}
	
	
}
