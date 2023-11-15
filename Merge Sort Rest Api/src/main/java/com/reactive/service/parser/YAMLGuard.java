package com.reactive.service.parser;

import java.util.ArrayList;

public class YAMLGuard  extends YAMLLocalFunction{

	private ArrayList<YAMLParameter> binding;
	
	public YAMLGuard() {
		binding= new ArrayList<>();
	}

	public ArrayList<YAMLParameter> getBinding() {
		return binding;
	}

	public void setBinding(ArrayList<YAMLParameter> binding) {
		this.binding = binding;
	}
}
