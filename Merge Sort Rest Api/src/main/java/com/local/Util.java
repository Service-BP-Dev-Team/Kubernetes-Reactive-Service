package com.local;

import java.util.ArrayList;

public class Util {
	
	public boolean guardArray(Object a, Object tab) {
		ArrayList array;
		if(a!=null && tab!=null) {
			array=(ArrayList) tab;
			System.out.println("the guard tab value is a : " +a+" and tab : "+tab);
			//System.out.println(array.get(1)==null);
			//return (array.get(1)==null);
		}
		return false;
		
	}
	
	public ArrayList<Object> returnSameArray(Object array){
		System.out.println("input of return same"+ array);
		return (ArrayList) array;
	}

}
