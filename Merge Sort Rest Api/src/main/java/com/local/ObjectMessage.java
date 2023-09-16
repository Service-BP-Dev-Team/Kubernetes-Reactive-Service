package com.local;

import java.util.ArrayList;

public class ObjectMessage {

	public ArrayList<Long> table = new ArrayList<Long>();

	public ArrayList<Long> getTable() {
		return table;
	}

	public void setTable(ArrayList<Long> table) {
		this.table = table;
	}
	public ObjectMessage() {
		
	}
	public ObjectMessage(ArrayList<Long> array) {
		table=array;
	}
}
