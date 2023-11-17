package com.local;

import java.util.ArrayList;

public class Util {
	
	public ArrayList guardArray(Object a, Object tab) {
		ArrayList result = new ArrayList<>();
		result.add(false);
		ArrayList array;
		if(a!=null && tab!=null) {
			array=(ArrayList) tab;
			result.set(0, true);// we set the guard to true
			System.out.println("the guard tab value is a : " +a+" and tab : "+tab);
			//System.out.println(array.get(1)==null);
			//return (array.get(1)==null);
			//we bind two local data f and g
			result.add(3);result.add(4);
		}
		return result;
		
	}
	
	public ArrayList<Object> returnSameArray(Object array){
		System.out.println("input of return same"+ array);
		return (ArrayList) array;
	}
	
	public Object compute(Object a,Object b){
		System.out.println("a is : "+ a + " and b is : "+ b);
		return (Integer)a + (Integer)b;
	}
	
	public boolean guardSync(Object sync, Object a) {
		return true;
	}
	
	public Integer getZero() {
		System.out.println("zero has been set");
		return 0;
	}
	public Integer incrementNumber(Object i) {
		System.out.println("iteration : "+ i);
		return (Integer)i +1;
	}
}
