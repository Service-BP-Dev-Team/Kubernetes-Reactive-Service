package com.reactive.service.util;

public class NameGenerator {
	private static String[] alphabet= {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","k","r","s","t","u","v","w","x","y","z"};
	private static int counter=0;
	
	public static String newName() {
		String result="";
		int indice =counter%alphabet.length;
		int mod = (counter-indice)/alphabet.length;
		String suffix="";
		if(mod!=0) {
			suffix=suffix+mod;
		}
		result=alphabet[indice]+suffix;
		counter++;
		return result;
	}
	
}
