package com.local;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.HashSet;

public class SortFunc {

	public static final int ARRAY_SIZE = 100;
	public static final int NUMBER_OF_BLOCKS = 5;
	public static final int MAX_LEN = 5;
	public static final int ARR_LEN = ARRAY_SIZE / NUMBER_OF_BLOCKS;

	public boolean lengthgr(Object input) {
		ArrayList list = (ArrayList) input;
		return list.size() > MAX_LEN;
	}

	public ArrayList<ArrayList<Integer>> split(Object inputs) {
		ArrayList<Integer> list = (ArrayList<Integer>) inputs;
		int cell_len = list.size() / NUMBER_OF_BLOCKS;
		ArrayList<ArrayList<Integer>> result = new ArrayList();
		for (int i = 0; i < NUMBER_OF_BLOCKS - 1; i++) {
			ArrayList<Integer> el = new ArrayList<Integer>();
			el.addAll(list.subList(cell_len * i, cell_len * (i + 1)));
			result.add(el);
		}
		ArrayList<Integer> el = new ArrayList<Integer>();
		el.addAll(list.subList(cell_len * (NUMBER_OF_BLOCKS - 1), list.size()));
		result.add(el);
		return result;
	}

	public ArrayList<Integer> merge_f(Object inputs) {
		ArrayList array = (ArrayList) inputs;
		System.out.println("inputs after split : " + inputs);
		ArrayList<Integer> result = (ArrayList<Integer>) array.get(0);
		Integer sizeGlobal = result.size();
		for (int i = 1; i < NUMBER_OF_BLOCKS; i++) {
			result = merge_sort(result, array.get(i));
			sizeGlobal += ((ArrayList) array.get(i)).size();
		}
		System.out.println(" inputs after merges : " + result);
		System.out.println("the size of result is : " + result.size());
		System.out.println("the global size is : " + sizeGlobal);
		return result;
	}

	public ArrayList merge_sort(Object input1, Object input2) {
		ArrayList<Integer> left = (ArrayList) input1;
		ArrayList<Integer> right = (ArrayList) input2;
		ArrayList<Integer> list = new ArrayList<>();
		int leftSize = left.size();
		int rightSize = right.size();
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
		ArrayList<ArrayList> list = (ArrayList<ArrayList>) input;
		// System.out.println(" I have been executed : "+ list);
		return list.get(0).size() <= MAX_LEN;
	}

	public boolean lengthgr_in_arr(Object input) {
		ArrayList<ArrayList> array = (ArrayList<ArrayList>) input;
		return array.get(0).size() > MAX_LEN;
	}

	public boolean lengthleq(Object input) {
		ArrayList list = (ArrayList) input;
		return list.size() <= MAX_LEN;
	}

	public ArrayList<ArrayList> divideLeft(Object input) {
		ArrayList<ArrayList> inp_arr = (ArrayList<ArrayList>) input;
		ArrayList<ArrayList> left = new ArrayList<ArrayList>();
		for (int i = 0; i < NUMBER_OF_BLOCKS; i++) {
			int size = inp_arr.get(i).size();
			ArrayList<Integer> el = new ArrayList<Integer>(inp_arr.get(i).subList(0, size / 2));
			left.add(el);
		}

		return left;
	}

	public ArrayList<ArrayList> divideRight(Object input) {
		ArrayList<ArrayList> inp_arr = (ArrayList<ArrayList>) input;
		ArrayList<ArrayList> right = new ArrayList<ArrayList>();
		for (int i = 0; i < NUMBER_OF_BLOCKS; i++) {
			int size = inp_arr.get(i).size();
			ArrayList<Integer> el = new ArrayList<Integer>(inp_arr.get(i).subList(size / 2, size));
			right.add(el);
		}

		return right;
	}

	public HashSet<Integer> init_indices() {
		HashSet<Integer> result = new HashSet<Integer>();
		for (int i = 0; i < 2 * NUMBER_OF_BLOCKS; i++) {
			result.add(i);
		}
		return result;
	}
}
