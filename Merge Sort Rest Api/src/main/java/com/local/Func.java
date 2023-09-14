package com.local;

public class Func {
	
	public int f(Object a, Object b) {
		int vala=0;
		if(a instanceof String) {
			vala= Integer.parseInt((String)a);
		}else {
			vala=(Integer)a;
		}
		int valb=0;
		if(b instanceof String) {
			valb= Integer.parseInt((String)b);
		}else {
			valb=(Integer)b;
		}
		return vala+valb;
	}
	
	public int g(Object a) {
		if(a instanceof String) {
			return Integer.parseInt((String)a)+5;
		}
		return (Integer)a + 5;
	}
	
	public boolean guards1(Object a, Object b) {
		return (a!=null) && (b!=null);
	}
	
	public boolean guards2(Object a) {
		return (a!=null);
	}

}
