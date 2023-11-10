package com.reactive.service.model.configuration;

import java.util.ArrayList;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.reactive.service.model.specification.IdExpression;
import com.reactive.service.model.specification.Parameter;
import com.reactive.service.model.specification.ServiceInstance;

public class DataGroup {
	String name;
	ArrayList<Data> collection;
	Parameter parameter;
	private String id;
	
	public DataGroup() {
		
	    id = UUID.randomUUID().toString();
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@JsonIgnore
	public ArrayList<Data> getCollection() {
		return collection;
	}
	public void setCollection(ArrayList<Data> collection) {
		this.collection = collection;
	}
	public static DataGroup createDataGroupFromParameter( Parameter par) {
		DataGroup dg = new DataGroup();
		dg.setName(par.getName());
		dg.setParameter(par);
		ArrayList<Data> col = new ArrayList<>();
		for(int i=0;i<par.getSize();i++) {
			Data d= new Data();
			d.setParameter(par);
			col.add(d);
			d.setGroup(dg);
		}
		dg.setCollection(col);
		return dg;
	}
	
	public static DataGroup createLocalDataGroupFromIdExpression( IdExpression id) {
		DataGroup dg = new DataGroup();
		dg.setName(id.getParameterName());
		Parameter par = new Parameter();
		par.setName(id.getParameterName());
		dg.setParameter(par);
		ArrayList<Data> col = new ArrayList<>();
		for(int i=0;i<id.getSize();i++) {
			Data d= new Data();
			d.setParameter(par);
			col.add(d);
			d.setGroup(dg);
		}
		dg.setCollection(col);
		return dg;
	}
	public Parameter getParameter() {
		return parameter;
	}
	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	
	
	
	
}
