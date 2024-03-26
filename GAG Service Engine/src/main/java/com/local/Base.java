package com.local;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

import com.reactive.service.app.api.InMemoryWorkspace;

import java.util.Collections;
import java.util.HashMap;

public class Base {

	private static HashMap<String, Boolean> Failed;
	private static Double faillureProbability;
	private static int faillureDuration;
	private static Semaphore computingRessource;
	static {
		Failed = new HashMap<String, Boolean>();
		Failed.put("Failed", true);
		faillureProbability = InMemoryWorkspace.getWorkerRequestFailureProbability();
		faillureDuration = InMemoryWorkspace.getWorkerRequestFailDuration();
		computingRessource = InMemoryWorkspace.getComputingRessource();
	}

	public ArrayList<Integer> sort(Object array) {
		ArrayList list = (ArrayList) array;
		Collections.sort(list);
		// System.out.println("basic cases we use the normal sort : "+ list);
		return list;

	}

	public ArrayList<ArrayList<Integer>> sort_array_lists(Object array) {
		ArrayList<ArrayList<Integer>> list = (ArrayList) array;
		ArrayList result = new ArrayList<>();
		// System.out.println("before basic sorts : "+ list);
		for (int i = 0; i < list.size(); i++) {
			ArrayList<Integer> el = new ArrayList<Integer>();
			Collections.sort(list.get(i));
			el.addAll(list.get(i));
			result.add(el);
		}
		// System.out.println("basic cases we use the normal sort : "+ result);
		return result;

	}

	public Integer getZero() {
		// System.out.println("zero has been set");
		return 0;
	}

	public Integer incrementNumber(Object i) {
		// System.out.println("iteration : "+ i);
		return (Integer) i + 1;
	}

	public Object sortWithPossibleFaillure(Object array) {

		ArrayList<Object> result = new ArrayList<Object>();
		long startTime = System.currentTimeMillis();
		try {
			computingRessource.acquire();

			Random random = new Random();
			double randomValue = (1 + random.nextInt(100)) / 100.0;
			
			long time = 0;
			// System.out.println("The random value is: "+randomValue);
			if (randomValue < faillureProbability || faillureProbability >= 1) {
				// Operation to be performed when the random value is less than or equal to the
				// probability
				// System.out.println("failling!");
				// Perform your desired operation here

				try {
					// Start the timer

					Thread.sleep(faillureDuration);

					result.add(Failed);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				ArrayList<Integer> arraySorted = sort(array);
				//System.out.println("performing the sort !");
				result.add(arraySorted);

			}
			long endTime = System.currentTimeMillis();
			time = endTime - startTime;
			result.add(time);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			computingRessource.release();
		}
		return result;

	}

	public boolean worker_success(Object in, Object res, Object time) {
		if (in != null && res != null && time!=null && res instanceof ArrayList) {
			//System.out.println(time);
			return true;
		}
		return false;
	}

	public boolean worker_failure(Object in, Object res, Object time) {
		if (in != null && res != null && time!=null && !(res instanceof ArrayList)) {
			int timebeforeretry = 1;
			try {
				Thread.sleep(timebeforeretry); // by sleeping by we priorityze other threads
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	public Object first(Object array) {
		return ((ArrayList) array).get(0);
	}

	public Object second(Object array) {
		return ((ArrayList) array).get(1);
	}
}
