package com.consulner.app.api.mergesort;

import java.util.ArrayList;
import java.util.List;

public class SequentialSort {
	
	
	public static ArrayInput sort(ArrayInput input) {
	    ArrayInput result = new ArrayInput();
	    result.setArray(input.getArray());
	    result.setSize(input.getSize());
	    mergeSort(result.getArray());
	    return result;
	}
	

	
    public static void mergeSort(List<Long> list) {
        if (list.size() < 2) {
            return; // Base case: list is already sorted
        }
        
        // Divide the list into two halves
        int mid = list.size() / 2;
        List<Long> left = new ArrayList<>(list.subList(0, mid));
        List<Long> right = new ArrayList<>(list.subList(mid, list.size()));
        
        // Recursively sort the two halves
        mergeSort(left);
        mergeSort(right);
        
        // Merge the sorted halves
        merge(left, right, list);
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
