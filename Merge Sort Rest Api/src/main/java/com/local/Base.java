package com.local;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class Base {

	public ArrayList<Integer> sort(Object array){
		ArrayList list =(ArrayList) array;
		Collections.sort(list);
		//System.out.println("basic cases we use the normal sort : "+ list);
		return list;
		
	}
	
	public ArrayList<ArrayList<Integer>> sort_array_lists(Object array){
		ArrayList<ArrayList<Integer>> list =(ArrayList) array;
		ArrayList result = new ArrayList<>();
		System.out.println("before basic sorts : "+ list);
		for(int i=0;i<list.size();i++ ) {
			ArrayList<Integer> el = new ArrayList<Integer>();
			Collections.sort(list.get(i));
			el.addAll(list.get(i));
			result.add(el);
		}
		//System.out.println("basic cases we use the normal sort : "+ result);
		return result;
		
	}
}
