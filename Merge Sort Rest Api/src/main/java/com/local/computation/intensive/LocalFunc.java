package com.local.computation.intensive;

import java.util.Map;

import com.reactive.service.app.api.InMemoryWorkspace;
import java.util.Random;

public class LocalFunc {
	
	public boolean guardProcessFinish(Object processId, Object frame, Object knownPosition){
		Long val=null;
		if(processId==null) {
			return false;
		}
		if(processId instanceof String) {
				val= Long.parseLong((String) processId);
			}
		else if(processId instanceof Long) {
				val=(Long) processId;
			}
		Object output=InMemoryWorkspace.threadFunctionProcess.get(val);
		if(output!=null) {
			return true;
		}
		return false;	
		
	}
	
	public boolean guardNotProcessFinish(Object processId, Object frame, Object knownPosition) {
		return !guardProcessFinish(processId, frame, knownPosition);
	}
	
	public boolean guardKillProcess(Object processId, Object frame, Object knownPosition) {
		
		Integer val=null;
		if(knownPosition==null || frame==null) {
			return false;
		}
		if(knownPosition instanceof String) {
				val= Integer.parseInt((String) knownPosition);
			}
		else if(knownPosition instanceof Integer) {
				val=(Integer) knownPosition;
			}
		if(val!=-1) {
			Frame myframe = Frame.parseFrame(frame);
			int dif = Math.abs(val-myframe.getStart());
			if(dif>Frame.maxDistance) {
				return false;
			}
			else {
				return true;
			}
		}
		return false;	
	}
	
	
	/** Local functions **/
	
	public Object waitComputation() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Frame.emptyResponse();
	}
	
	
	public Object kill(Object processId) {
		Long val=null;
		if(processId==null) {
			return false;
		}
		if(processId instanceof String) {
				val= Long.parseLong((String) processId);
			}
		else if(processId instanceof Long) {
				val=(Long) processId;
			}
		// kill the process

		// Get all live threads and their stack traces
		Map<Thread, StackTraceElement[]> threadMap = Thread.getAllStackTraces();

		// Find the thread with the matching ID
		Thread targetThread = null;
		for (Thread thread : threadMap.keySet()) {
		    if (thread.getId() == val) {
		        targetThread = thread;
		        break;
		    }
		}

		// Stop the thread if found
		if (targetThread != null) {
		    targetThread.stop(); // Deprecated and considered unsafe!
		} else {
		    System.out.println("Thread not found");
		}
		return Frame.emptyResponse();
	}
	
	public Object fetch(Object processId) {
		Long val=null;
		if(processId==null) {
			return false;
		}
		if(processId instanceof String) {
				val= Long.parseLong((String) processId);
			}
		else if(processId instanceof Long) {
				val=(Long) processId;
			}
		Object output=InMemoryWorkspace.threadFunctionProcess.get(val);
		return output;
	}
	
	public Object emptyResponse() {
		return Frame.emptyResponse();
	}
	

	
	
}
