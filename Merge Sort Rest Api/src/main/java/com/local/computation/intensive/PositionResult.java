package com.local.computation.intensive;

import java.io.IOException;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.consulner.app.Configuration.getObjectMapper;
public class PositionResult {
	private ArrayList<Integer> positions;

	public ArrayList<Integer> getPositions() {
		return positions;
	}

	public void setPositions(ArrayList<Integer> positions) {
		this.positions = positions;
	}
	
	public static PositionResult merge(PositionResult pos1,PositionResult pos2) {
		PositionResult result= new PositionResult();
		ArrayList<Integer> list = new ArrayList<>();
		list.addAll(pos1.getPositions());
		list.addAll(pos2.getPositions());
		result.setPositions(list);
		return result;
	}
	public static PositionResult ParsePositionResult(Object obj) {
		if(obj==null) {
			return null;
		}
		if(obj instanceof PositionResult) {
			return (PositionResult) obj;
		}
		if(obj instanceof String) {
			String objString=(String)obj;
			if(objString.matches("-?\\d+")) {
				PositionResult posResult= new PositionResult();
				ArrayList<Integer> posis= new ArrayList<>();
				posis.add(Integer.parseInt(objString));
				posResult.setPositions(posis);
				return posResult;
			}else {
				try {
					PositionResult posResult= getObjectMapper().readValue(objString, PositionResult.class);
					return posResult;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	
}
