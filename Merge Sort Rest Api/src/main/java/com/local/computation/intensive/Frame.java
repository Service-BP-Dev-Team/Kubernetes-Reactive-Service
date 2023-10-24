package com.local.computation.intensive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import static com.consulner.app.Configuration.getObjectMapper;

public class Frame {
		
	private int start;
	private int end;
	private ArrayList<Integer> array;
	public static final int maxDistance = 5;
	public static final int maxLocalTime = 20;
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	public ArrayList<Integer> getArray() {
		return array;
	}
	public void setArray(ArrayList<Integer> array) {
		this.array = array;
	}
	
	public static Frame parseFrame(Object obj) {
		if(obj == null) return null;
		if(obj instanceof Frame) {
			return (Frame) obj;
		}
		else if(obj instanceof LinkedHashMap) {
			LinkedHashMap map = (LinkedHashMap)obj;
			Frame frame = new Frame();
			frame.setStart((Integer)map.get("start"));
			frame.setStart((Integer)map.get("end"));
			frame.setArray((ArrayList)map.get("array"));
			return frame;
		}
		else if(obj instanceof String) {
			ObjectMapper objMapper = getObjectMapper();
			try {
				Frame  frame = objMapper.readValue((String)obj,Frame.class);
				return frame;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public static Object emptyResponse() {
		return -1;
	}
}
