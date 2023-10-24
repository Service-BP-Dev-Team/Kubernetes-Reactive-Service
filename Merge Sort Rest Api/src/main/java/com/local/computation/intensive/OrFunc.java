package com.local.computation.intensive;

public class OrFunc {

	public boolean guardPos1(Object pos1, Object pos2, Object pos3) {
		Object ipos1 = getValue(pos1),ipos2 = getValue(pos2),ipos3 = getValue(pos3);
		if(ipos1!=null) {
			Integer val = (Integer) ipos1;
			if(val!=-1) {
				//fecth the lowest
				return true;
			}
		}
		return false;
	}
	
	public boolean guardPos2(Object pos1, Object pos2, Object pos3) {
		Object ipos1 = getValue(pos1),ipos2 = getValue(pos2),ipos3 = getValue(pos3);
		if(ipos2!=null) {
			Integer val = (Integer) ipos2;
			if(val!=-1) {
				return true;
			}
		}
		return false;
	}
	
	public boolean guardPos3(Object pos1, Object pos2, Object pos3) {
		Object ipos1 = getValue(pos1),ipos2 = getValue(pos2),ipos3 = getValue(pos3);
		if(ipos3!=null) {
			Integer val = (Integer) ipos3;
			if(val!=-1) {
				return true;
			}
			else if(val==-1 && ((Integer)ipos1)==-1 && ((Integer)ipos2)==-1) {
				return true;
			}
		}
		return false;
	}
	
	public Object getValue(Object pos) {
		if(pos==null)return null;
		if(pos instanceof String) {
			return Integer.parseInt((String) pos);
		}
		if(pos instanceof Integer) {
			return (Integer) pos;
		}
		return null;
		
	}
}
