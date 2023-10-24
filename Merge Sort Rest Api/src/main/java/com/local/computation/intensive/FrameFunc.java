package com.local.computation.intensive;

import java.util.ArrayList;
import java.util.Random;

public class FrameFunc {

	
	public boolean guardSeveralFrame(Object x, Object theframes) {
		Frame frame= Frame.parseFrame(theframes);
		if(frame==null) {
			return false;
		}else {
			return frame.getArray().size()>1;
		}
	}
	public boolean guardUniqueFrame(Object x, Object theframes) {
		Frame frame= Frame.parseFrame(theframes);
		if(frame==null) {
			return false;
		}else {
			return frame.getArray().size()==1;
		}
	}
	
	public Object divide(Object theframe) {
		Frame frame= Frame.parseFrame(theframe);
		ArrayList<Integer> list = frame.getArray();
		 int mid = list.size() / 2;
	        ArrayList <Integer> left = new ArrayList<>(list.subList(0, mid));
	        ArrayList <Integer> right = new ArrayList<>(list.subList(mid, list.size()));
	    ArrayList result = new ArrayList<>();
	    Frame frame1= new Frame(), frame2=new Frame();
	    frame1.setArray(left);
	    frame1.setStart(frame.getStart());
	    frame1.setEnd(frame.getStart()+mid-1);
	    frame2.setArray(right);
	    frame2.setStart(frame.getEnd()+1);
	    frame2.setEnd(frame.getEnd());
	    result.add(frame1);
	    result.add(frame2);
	    return result;
	}
	
	public Object lookup(Object x, Object theframe) {
		Frame frame = Frame.parseFrame(theframe);
		Integer val = getValInteger(x);
		//randomize using max time
		Random random = new Random();
		int rand=random.nextInt(Frame.maxLocalTime+1)*1000;
		try {
			Thread.sleep(rand);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(val==frame.getArray().get(0)) {
			return frame.getStart();
		}else {
			return Frame.emptyResponse();
		}
	}
	
	public Object first(Object array) {
		ArrayList val = (ArrayList)(array);
		return val.get(0);
	}
	
	public Object second(Object array) {
		ArrayList val = (ArrayList)(array);
		return val.get(1);
	}
	
	public Object merge(Object result1, Object result2) {
		PositionResult pos1=PositionResult.ParsePositionResult(result1);
		PositionResult pos2=PositionResult.ParsePositionResult(result2);
		PositionResult result = PositionResult.merge(pos1, pos2);
		return result;
	}
	
	
	public Integer getValInteger(Object el) {
		Integer val=null;
		if(el==null) {
			return null;
		}
		if(el instanceof String) {
				val= Integer.parseInt((String) el);
			}
		else if(el instanceof Integer) {
				val=(Integer) el;
			}
		return val;
	}
}
