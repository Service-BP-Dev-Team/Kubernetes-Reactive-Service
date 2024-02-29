package com.local;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import com.reactive.service.app.api.InMemoryWorkspace;

import java.util.HashSet;

public class SortFunc {

	public static final int ARRAY_SIZE =10000;

	public boolean lengthgr(Object input) {
	//	System.out.println("lenght start guard ");
		if(input==null) return false;
		ArrayList list = (ArrayList) input;
	//	System.out.println("lenght start second ");
		return list.size() > getMAX_LEN();
	}

	public ArrayList<ArrayList<Integer>> split(Object inputs) {
		ArrayList<Integer> list = (ArrayList<Integer>) inputs;
		int cell_len = list.size() / getNUMBER_OF_BLOCKS();
		ArrayList<ArrayList<Integer>> result = new ArrayList();
		for (int i = 0; i < getNUMBER_OF_BLOCKS() - 1; i++) {
			ArrayList<Integer> el = new ArrayList<Integer>();
			el.addAll(list.subList(cell_len * i, cell_len * (i + 1)));
			result.add(el);
		}
		ArrayList<Integer> el = new ArrayList<Integer>();
		el.addAll(list.subList(cell_len * (getNUMBER_OF_BLOCKS() - 1), list.size()));
		result.add(el);
		//System.out.println(" inputs after split : " + result);
		return result;
	}

	public ArrayList<Integer> merge_f(Object inputs) {
		ArrayList array = (ArrayList) inputs;
		//System.out.println("inputs after split : " + inputs);
		ArrayList<Integer> result = (ArrayList<Integer>) array.get(0);
		Integer sizeGlobal = result.size();
		for (int i = 1; i < getNUMBER_OF_BLOCKS(); i++) {
			result = merge_sort(result, array.get(i));
			sizeGlobal += ((ArrayList) array.get(i)).size();
		}
		//System.out.println(" inputs after merges : " + result);
		//System.out.println("the size of result is : " + result.size());
		//System.out.println("the global size is : " + sizeGlobal);
		return result;
	}

	public ArrayList merge_sort(Object input1, Object input2) {
		ArrayList<Integer> left = (ArrayList) input1;
		ArrayList<Integer> right = (ArrayList) input2;
		ArrayList<Integer> list = new ArrayList<>();
		int leftSize = left.size();
		int rightSize = right.size();
		if(leftSize==0) {return right;}if(rightSize==0) {return left;}
		int i = 0; // Index for the left list
		int j = 0; // Index for the right list

		// Compare elements from left and right lists and merge them
		while (i < leftSize && j < rightSize) {
			if (left.get(i) <= right.get(j)) {
				list.add(left.get(i++));
			} else {
				list.add(right.get(j++));
			}
		}
		// Copy any remaining elements from the left list
		while (i < leftSize) {
			list.add(left.get(i++));
		}

		// Copy any remaining elements from the right list
		while (j < rightSize) {
			list.add(right.get(j++));
		}

		return list;
	}

	public boolean lengthleq_in_arr(Object input) {
		if(input==null) return false;
		ArrayList list = (ArrayList) input;
		return list.size()/getNUMBER_OF_BLOCKS() <= getMAX_LEN();
	}

	public boolean lengthgr_in_arr(Object input) {
		if(input==null) return false;
		ArrayList array = (ArrayList) input;
		return array.size()/getNUMBER_OF_BLOCKS() > getMAX_LEN();
	}

	public boolean lengthleq(Object input) {
		if(input==null) return false;
		ArrayList list = (ArrayList) input;
		return list.size() <= getMAX_LEN();
	}

	public ArrayList divideLeft(Object input) {
		ArrayList inp_arr = (ArrayList) input;
		int size = inp_arr.size();
		ArrayList left  = new ArrayList<Integer>(inp_arr.subList(0, size / 2));
		return left;
	}

	public ArrayList<ArrayList> divideRight(Object input) {
		ArrayList inp_arr = (ArrayList) input;
		int size = inp_arr.size();
		ArrayList right  = new ArrayList<Integer>(inp_arr.subList(size / 2, size));
		return right;
	}

	public HashSet<Integer> init_indices() {
		HashSet<Integer> result = new HashSet<Integer>();
		for (int i = 0; i < 2 * getNUMBER_OF_BLOCKS(); i++) {
			result.add(i);
		}
		return result;
	}
	public ArrayList has_new_elements(Object indices,Object input1,Object input2) {
		 ArrayList left = (ArrayList)input1;
		 ArrayList right = (ArrayList)input2;
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
	         if (i < getNUMBER_OF_BLOCKS()) {
	      	    if (left.get(i)!=null) {
	      	       count++;
	      	       listToMerge.add(left.get(i));
	      	       indicesToRemove.add(i);
	      	       if (count>1) {
	      	          break;
	      	       }
	      	    }
	      	 }
	      	 else {
	      	    if (right.get(i-getNUMBER_OF_BLOCKS())!=null){
	      	       count++;
	      	       listToMerge.add(right.get(i-getNUMBER_OF_BLOCKS()));
	      	       indicesToRemove.add(i);
	      	       if (count>1) {
	      	          break;
	      	       }
	      	    }
	         }
	      }
	      if(count>1) {
	    	  result.set(0,true);
	    	  result.addAll(listToMerge);
	    	  pending.removeAll(indicesToRemove);
	    	  result.add(pending);
	    	  
	      }
	      
	      return result;

	}
	
	public boolean index_basic_defined( Object i, Object arr) {
		if(i!=null && arr!=null) {
			Integer index = (Integer)i;
			ArrayList array = (ArrayList) arr;
			if(array.get(index)!=null) {
				return true;
			}
		}
		return false;
	}
	
	public boolean base_sort_input_defined(Object input) {
		return (input!=null);
	}
	
	public static int getNUMBER_OF_BLOCKS() {
		return Integer.parseInt(InMemoryWorkspace.getEnvironmentValue("NUMBER_OF_BLOCKS"));
	}
	

	
	public static int getMAX_LEN() {
		return Integer.parseInt(InMemoryWorkspace.getEnvironmentValue("MAX_LEN"));
	}
	
	public ArrayList<Object> syncValue(Object array) {
		//System.out.println("I'm executed");
		//System.out.println(array);
		
		return (ArrayList)array;
	}
	
	// has new elements with sync 
	public ArrayList has_new_elements_sync(Object indices,Object input1,Object input2) {
		 ArrayList left = (ArrayList)input1;
		 ArrayList right = (ArrayList)input2;
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
		//when we are at the first iteration we verify that all values have been received
		 if(pending.size()==left.size()) {
			 for(Object el :left) {
				 if (el==null ) {
					 return result;
				 }					 
			 }
			 for(Object el :right) {
				 if (el==null ) {
					 return result;
				 }					 
			 }
			 // we return false in each cases above since we don't want the service to be incremental
			 
		 }
		  int count = 0;
	      for (int i: pending) {
	         if (i < getNUMBER_OF_BLOCKS()) {
	      	    if (left.get(i)!=null) {
	      	       count++;
	      	       listToMerge.add(left.get(i));
	      	       indicesToRemove.add(i);
	      	       if (count>1) {
	      	          break;
	      	       }
	      	    }
	      	 }
	      	 else {
	      	    if (right.get(i-getNUMBER_OF_BLOCKS())!=null){
	      	       count++;
	      	       listToMerge.add(right.get(i-getNUMBER_OF_BLOCKS()));
	      	       indicesToRemove.add(i);
	      	       if (count>1) {
	      	          break;
	      	       }
	      	    }
	         }
	      }
	      if(count>1) {
	    	  result.set(0,true);
	    	  result.addAll(listToMerge);
	    	  pending.removeAll(indicesToRemove);
	    	  result.add(pending);
	    	  
	      }
	      
	      return result;

	}

}
