package com.local;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.reactive.service.app.api.InMemoryWorkspace;

import java.util.Collections;
import java.util.HashMap;

public class Base {

	private static HashMap<String,Boolean> Failed;
	private static Double faillureProbability;
	static {
		Failed = new HashMap<String, Boolean>();
		Failed.put("Failed",true);
		faillureProbability = InMemoryWorkspace.getWorkerRequestFailureProbability();
	}
	public ArrayList<Integer> sort(Object array){
		ArrayList list =(ArrayList) array;
		Collections.sort(list);
		//System.out.println("basic cases we use the normal sort : "+ list);
		return list;
		
	}
	
	public ArrayList<ArrayList<Integer>> sort_array_lists(Object array){
		ArrayList<ArrayList<Integer>> list =(ArrayList) array;
		ArrayList result = new ArrayList<>();
		//System.out.println("before basic sorts : "+ list);
		for(int i=0;i<list.size();i++ ) {
			ArrayList<Integer> el = new ArrayList<Integer>();
			Collections.sort(list.get(i));
			el.addAll(list.get(i));
			result.add(el);
		}
		//System.out.println("basic cases we use the normal sort : "+ result);
		return result;
		
	}
	
	public Integer getZero() {
		//System.out.println("zero has been set");
		return 0;
	}
	public Integer incrementNumber(Object i) {
		//System.out.println("iteration : "+ i);
		return (Integer)i +1;
	}
	
	public Object sortWithPossibleFaillure(Object array) {
        Random random = new Random();
        double randomValue = random.nextDouble();

        if (randomValue < faillureProbability || faillureProbability>=1.0) {
            // Operation to be performed when the random value is less than or equal to the probability
           // System.out.println("failling!");
            // Perform your desired operation here
            return Failed;
        } else {
            // Operation to be performed when the random value is greater than the probability
            //System.out.println("perfoming the sort");
            return sort(array);
        }
	}
	
	public boolean worker_success(Object in, Object res) {
		if (in!=null && res!=null && res instanceof ArrayList) {
			return true;
		}
		return false;
	}
	
	public boolean worker_failure(Object in, Object res) {
		if (in!=null && res!=null && !(res instanceof ArrayList)) {
			int timebeforeretry = InMemoryWorkspace.getTimeToWaitBeforeReDoWorkerRequest();
			try {
				Thread.sleep(timebeforeretry);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}
}
