package com.local;

import java.util.ArrayList;
import java.util.HashSet;

public class MergeIncremental {

	public boolean lenght_is_one(Object in_length, Object in_array) {
		if(in_length!=null) {
			return ((Integer) in_length ) == 1;
		}
		return false;
	}
	
	public boolean lenght_is_greater_than_one(Object in_length, Object in_array) {
		if(in_length!=null) {
			return ((Integer) in_length ) > 1;
		}
		return false;
	}
	
	public ArrayList merge_sort(Object input1, Object input2) {
		return (new SortFunc()).merge_sort(input1, input2);
	}
	
	public HashSet<Integer> init_indices(Object length) {
		Integer size = (Integer)length;
		HashSet<Integer> result = new HashSet<Integer>();
		for (int i = 0; i < size; i++) {
			result.add(i);
		}
		return result;
	}
	
	public ArrayList has_new_elements(Object indices,Object input) {
		 ArrayList array = (ArrayList)input;
		 ArrayList listToMerge = new ArrayList<>(); 
		 ArrayList indicesToRemove = new ArrayList();
		 ArrayList result = new ArrayList<>();
		 result.add(false);//by default we return false
		 if(indices==null) {
			 // the indices to consider are required to detect
			 // new elements
			 return result;
		 }
		 HashSet<Integer> pending=(HashSet<Integer>)indices;
		  int count = 0;
	      for (int i: pending) {
	         if (array.get(i)!=null) {
	      	       count++;
	      	       listToMerge.add(array.get(i));
	      	       indicesToRemove.add(i);
	      	       if (count>1) {
	      	          break;
	      	       }
	      	    
	      	 }
	      }
	      if((count==1 && pending.size()==1) || count >1) {
	    	
	    	  result.set(0,true);
	    	  if(count==1 && pending.size()==1) {listToMerge.add(new ArrayList());} 
	    	  // the above is true when the list has only one elements
	    	  result.addAll(listToMerge);
	    	  pending.removeAll(indicesToRemove);
	    	  result.add(pending);
	      }
	      
	      return result;

	}
	
	public boolean indices_is_empty(Object indices,Object input) {
		if(indices != null) {
			 HashSet<Integer> pending=(HashSet<Integer>)indices;
			 if(pending.size()==0) {
				 return true;
			 }
		}
		return false;
	}
	
	public Integer divide_length(Object length) {
		Integer size = (Integer) length;
		return size/2 + (size%2);
	}
	
	public ArrayList<Integer> empty_array() {
		ArrayList<Integer> result = new ArrayList<Integer>();
		result.add(-1);
		return result;
		
	}
	
}
