package com.consulner.app.api.mergesort;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DistributedSort {
	
	
	public static ArrayInput sort(ArrayInput input, ObjectMapper obj) {
	    ArrayInput result = new ArrayInput();
	    result.setArray(input.getArray());
	    result.setSize(input.getSize());
	    mergeSort(result.getArray(),obj);
	    return result;
	}
	

	
    public static void mergeSort(List<Long> list, ObjectMapper obj) {
        if (list.size() < 2) {
            return; // Base case: list is already sorted
        }
        
        // Divide the list into two halves
        int mid = list.size() / 2;
        List<Long> left = new ArrayList<>(list.subList(0, mid));
        List<Long> right = new ArrayList<>(list.subList(mid, list.size()));
        
        // Recursively sort the two halves
        left=mergeSortRemote(left,obj);
        right=mergeSortRemote(right,obj);
        
        // Merge the sorted halves
        merge(left, right, list);
    }
    
    public static List<Long> mergeSortRemote(List<Long> list, ObjectMapper obj) {
    	ArrayList<Long> array = new ArrayList<Long>(list);
    	ArrayInput arrayInput = new ArrayInput();
    	arrayInput.setSize(list.size());
    	arrayInput.setArray(array);
    	if(list.size()<100000) {
    		return SequentialSort.sort(arrayInput).getArray();
    	}
    	try {
			String jsonData = obj.writeValueAsString(arrayInput);
			//System.out.println(jsonData);
			String result = JsonHttpPost.postRequestAndReturnString(jsonData, "http://localhost:8000/api/mergesort/sort-array");
			try {
				ArrayInput response=obj.readValue(new ByteArrayInputStream(result.getBytes()), ArrayInput.class);
				array = response.getArray(); // the result
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return array;
    }
    public static void merge(List<Long> left, List<Long> right, List<Long> list) {
        int leftSize = left.size();
        int rightSize = right.size();
        int i = 0; // Index for the left list
        int j = 0; // Index for the right list
        int k = 0; // Index for the merged list
        
        // Compare elements from left and right lists and merge them
        while (i < leftSize && j < rightSize) {
            if (left.get(i) <= right.get(j)) {
                list.set(k++, left.get(i++));
            } else {
                list.set(k++, right.get(j++));
            }
        }
        
        // Copy any remaining elements from the left list
        while (i < leftSize) {
            list.set(k++, left.get(i++));
        }
        
        // Copy any remaining elements from the right list
        while (j < rightSize) {
            list.set(k++, right.get(j++));
        }
    }
	

}
