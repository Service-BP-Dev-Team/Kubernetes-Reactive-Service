package com.local;
import static com.consulner.app.Configuration.getObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.consulner.app.api.mergesort.SequentialSort;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.util.LinkedHashMap;
public class MergeFunc {

	
	public Object first(Object array) {
		ArrayList val = getArrayOfArray(array);
		return val.get(0);
	}
	
	public Object second(Object array) {
		ArrayList val = getArrayOfArray(array);
		return val.get(1);
	}
	
	public boolean guardDivide(Object array) {
		if(array==null) {
			return false;
		}
		ArrayList val = getArray(array);
		if(val.size()>=2) {
			return true;
		}
		return false;
	}
	public Object merge(Object arr1, Object arr2) {
		ArrayList<Long> val1 = getArray(arr1);
		ArrayList<Long> val2 = getArray(arr2);
		ArrayList<Long> list = new ArrayList<Long>(); 
		list.addAll(val1);
		list.addAll(val2);
		SequentialSort.merge(val1,val2,list);
		return new ObjectMessage(list);
		
	}
	
	public Object divide(Object array) {
		ArrayList<Long> list = getArray(array);
		 int mid = list.size() / 2;
	        ArrayList <Long> left = new ArrayList<>(list.subList(0, mid));
	        ArrayList <Long> right = new ArrayList<>(list.subList(mid, list.size()));
	    ArrayList result = new ArrayList<>();
	    result.add(new ObjectMessage(left));
	    result.add(new ObjectMessage(right));
	    return result;
	}
	
	public boolean guardSimpleSort(Object array) {
		if(array==null) {
			return false;
		}
		ArrayList<Long> val = getArray(array);
		if(val.size()<2) {
			return true;
		}
		return false;
	}
	
	public Object simpleSort(Object array) {
		ArrayList<Long> val = getArray(array);
		ArrayList<Long> inter = new ArrayList<Long>(); 
		inter.addAll(val);
		System.out.println("basic cases");
		System.out.println(inter);
		SequentialSort.mergeSort(inter);
		ArrayList result = new ArrayList<>();
		result.add(new ObjectMessage(inter));
		result.add(new ObjectMessage(new ArrayList<Long>()));
		return result;
		
	}
	
	
	
	
	
	
	
	
	
	
	public ArrayList<Long> getArray(Object array) {
		if(array instanceof String) {
			try{ObjectMessage obj= getObjectMapper().readValue((String)array, ObjectMessage.class);
			ArrayList<Long> ar = obj.getTable();
				ArrayList<Long> ArrayListOfLong = new ArrayList<Long>();
				for(Object el:ar) {
					if(el instanceof Integer) {
						ArrayListOfLong.add(((Integer)el).longValue());
					}else {
						ArrayListOfLong.add((Long)el);
					}
				}
				return ArrayListOfLong;
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(array instanceof LinkedHashMap) {
			ArrayList ar =(ArrayList) ((LinkedHashMap) array).get("table");
			//convert to long values
			ArrayList<Long> ArrayListOfLong = new ArrayList<Long>();
			for(Object el:ar) {
				if(el instanceof Integer) {
					ArrayListOfLong.add(((Integer)el).longValue());
				}else {
					ArrayListOfLong.add((Long)el);
				}
			}
			return ArrayListOfLong;
		
		}
		else {
			System.out.println(array);
			System.out.println(array.getClass());
			ArrayList<Long> ar = ((ObjectMessage) array).getTable();
			return ar;
		}
		return null;
	}

	public ArrayList getArrayOfArray(Object array) {
		if(array instanceof String) {
			try {
				ArrayList ar = getObjectMapper().readValue((String)array, ArrayList.class);
				return ar;
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			ArrayList ar = (ArrayList) array;
			
			return ar;
		}
		return null;
	}

}
