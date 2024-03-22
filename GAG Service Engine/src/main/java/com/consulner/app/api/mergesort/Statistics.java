package com.consulner.app.api.mergesort;

import java.util.ArrayList;
import java.util.HashMap;

import com.reactive.service.model.configuration.Task;

public class Statistics {
	
	public static HashMap getNumberOfSuccessAndFailedNode(Task root) {
		HashMap<String, Object> result = new HashMap<String,Object>();
		ArrayList<HashMap> count = countSuccessAndFailed(root);
		
		result.put("success", count.get(0));
		result.put("failure", count.get(1));
		return result;
		
	}
	
	public static ArrayList<HashMap> countSuccessAndFailed(Task root) {
		ArrayList<HashMap> result = new ArrayList<HashMap>();
		
		HashMap failedHashMap = new HashMap<String,Object>();
		int failedSum = 0;
		ArrayList<Long> failedDuration = new ArrayList<Long>();
		
		HashMap successHashMap = new HashMap<String,Object>();
		int successSum = 0;
		ArrayList<Long> successDuration = new ArrayList<Long>();
		
			if(root.getService().getName().equals("Check_output")) {
				if(root.getInputs().get(1).getValue() instanceof ArrayList) {
					// this is a success case
					successSum++;
					successDuration.add((Long)root.getInputs().get(2).getValue());
				}
				else {
					// this is a failure case
					failedSum++;
					failedDuration.add((Long)root.getInputs().get(2).getValue());
				}
			}
			if (!root.getSubTasks().isEmpty()) {
				// this is not a leaf
				for(Task sub:root.getSubTasks()) {
					ArrayList<HashMap> subResult = countSuccessAndFailed(sub);
					successSum+=(Integer)subResult.get(0).get("total");
					successDuration.addAll((ArrayList)subResult.get(0).get("durations"));
					failedSum+=(Integer)subResult.get(1).get("total");
					failedDuration.addAll((ArrayList)subResult.get(1).get("durations"));
				}
			}
		failedHashMap.put("total", failedSum);
		failedHashMap.put("durations", failedDuration);
		

		successHashMap.put("total", successSum);
		successHashMap.put("durations", successDuration);
		result.add(successHashMap);result.add(failedHashMap);
		return result;
	}
}
