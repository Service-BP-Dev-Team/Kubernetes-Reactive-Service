package com.consulner.app.api.mergesort;

import java.util.ArrayList;
import java.util.Random;

public class ToolKit {

	
	public static ArrayList<Integer> generateArray( int size){
		ArrayList<Integer> result = new ArrayList<Integer>();
		Random rand = new Random(); 
		for(int i=0; i<size;i++) {
			result.add(rand.nextInt(size));
		}
		return result;
	}
}
