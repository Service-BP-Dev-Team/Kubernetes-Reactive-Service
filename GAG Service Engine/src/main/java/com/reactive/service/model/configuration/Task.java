package com.reactive.service.model.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactive.service.model.specification.Parameter;
import com.reactive.service.model.specification.Service;
import com.reactive.service.model.specification.ServiceInstance;

import static com.consulner.app.Configuration.getObjectMapper;

public class Task implements Serializable{
	private String AppliedRule;
	private boolean open=true;
	private Service service;
	private ArrayList<Data> inputs;
	private ArrayList<Data> outputs;
	private ArrayList<Data> locals;
	private ArrayList<Task> subTasks;
	private ArrayList<DataGroup> dataGroups; //for handling array
	private ArrayList<DataGroup> localGroups; // for handling local array
											  // not corresponding to an input
											  // or an output array
	private boolean remote = false;
	private boolean terminated = false; //when the task has been exploited and destroyed
	
	public Task() {
		inputs = new ArrayList<Data>();
		outputs = new ArrayList<Data>();
		subTasks = new ArrayList<Task>();
		locals = new ArrayList<Data>();
		dataGroups = new ArrayList<DataGroup>();
		localGroups = new ArrayList<DataGroup>();
		open=true;
	}
	public String getAppliedRule() {
		return AppliedRule;
	}
	public void setAppliedRule(String appliedRule) {
		AppliedRule = appliedRule;
	}
	public boolean isOpen() {
		return open;
	}
	public void setOpen(boolean open) {
		this.open = open;
	}
	
	public Service getService() {
		return service;
	}
	public void setService(Service service) {
		this.service = service;
	}

	
	public ArrayList<Data> getInputs() {
		return inputs;
	}
	public void setInputs(ArrayList<Data> inputs) {
		this.inputs = inputs;
	}

	public ArrayList<Data> getOutputs() {
		return outputs;
	}
	
	public void setOutputs(ArrayList<Data> outputs) {
		this.outputs = outputs;
	}

	public ArrayList<Task> getSubTasks() {
		return subTasks;
	}
	
	public void setSubTasks(ArrayList<Task> subTask) {
		this.subTasks = subTask;
	}

	@JsonIgnore
	public ArrayList<Data> getLocals() {
		return locals;
	}
	public void setLocals(ArrayList<Data> locals) {
		this.locals = locals;
	}
	public boolean isRemote() {
		return remote;
	}
	public void setRemote(boolean remote) {
		this.remote = remote;
	}
	
	public ArrayList<DataGroup> getDataGroups() {
		return dataGroups;
	}
	public void setDataGroups(ArrayList<DataGroup> dataGroups) {
		this.dataGroups = dataGroups;
	} 
	@JsonIgnore
	public ArrayList<Object> getArguments() {
		ArrayList<Object> result = new ArrayList<Object>();
		for(Data in:inputs) {
			if(in.getGroup()!=null) {
				if(!result.contains(in.getGroup())) {
				result.add(in.getGroup());
				}
			}else {
				result.add(in);
			}
		}
		return result;
	}
	
	
	@JsonIgnore
	public ArrayList<Object> getArgumentValues(){
		ArrayList<Object> set= getArguments();
		ArrayList<Object> result = new ArrayList();
		for(Object obj:set) {
			if(obj instanceof DataGroup) {
				ArrayList<Object> v=new ArrayList<>();
				for(Data el:((DataGroup) obj).getCollection()) {
					v.add(el.getValue());
				}
				result.add(v);
			}else {
				result.add(((Data)obj).getValue());
			}
		}
		return result;
	}
	@JsonIgnore
	public List<Data> getAllData(){
		ArrayList<Data> all = new ArrayList<>();
		all.addAll(inputs);
		all.addAll(outputs);
		all.addAll(locals);
		return all;
	}
	
	@JsonIgnore
	public List<Data> getAllWithoutLocalData(){
		ArrayList<Data> all = new ArrayList<>();
		all.addAll(inputs);
		all.addAll(outputs);
		return all;
	}
	
	// this method is necessary because when we send a task to
	// a remote node we do not completely serialize datagroup
	// to avoid cyclic issue : a data reference a group while the group itself reference all its data
	
	public void buildGroup() {
		List<Data> alldata = getAllWithoutLocalData();
		for(Data d:alldata) {
			if(d.getGroup()!=null) {
				DataGroup dg = findGroupById(d.getGroup().getId());
				d.setGroup(dg);
				ArrayList<Data> col = dg.getCollection();
				if(col==null) {
					col= new ArrayList<>();
				}
				col.add(d);
				dg.setCollection(col);
			}
		}
	}
	
	public DataGroup findGroupById(String id) {
		for(DataGroup dg:dataGroups) {
			if(dg.getId().equals(id)) {
				return dg;
			}
		}
		return null;
	}
	
	public DataGroup findGroupByParameterName(String parameterName) {
		for(DataGroup dg:dataGroups) {
			if(dg.getParameter().getName().equals(parameterName)) {
				return dg;
			}
		}
		// also research in local groups
		for(DataGroup dg:localGroups) {
			if(dg.getParameter().getName().equals(parameterName)) {
				return dg;
			}
		}
		return null;
	}
	public ArrayList<DataGroup> getLocalGroups() {
		return localGroups;
	}
	public void setLocalGroups(ArrayList<DataGroup> localGroups) {
		this.localGroups = localGroups;
	}
	public Data findDataByParameterName(String name) {
		// TODO Auto-generated method stub
		List<Data> allData = getAllData();
		for(Data d:allData){
			if(d.getParameter().getName().equals(name)) {
				return d;
			}
		}
		return null;
		
	}
	@JsonIgnore
	public String getJsonRepresentation() {
		String result = "";
		ObjectMapper mapper = getObjectMapper();
		try {
			result=mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	@JsonIgnore
	public boolean isTerminated() {
		return terminated;
	}
	public void setTerminated(boolean terminated) {
		this.terminated = terminated;
	}
	
	public void clearData() {
		this.AppliedRule=null;
		for(Data d:getAllData()) {
			d.clear();
		}
		this.dataGroups.clear();
		this.dataGroups=null;
		this.inputs.clear();
		this.inputs=null;
		this.locals.clear();
		this.locals=null;
		this.localGroups.clear();
		this.localGroups=null;
		this.subTasks.clear();
		this.subTasks=null;
		this.outputs.clear();
		this.outputs=null;
		this.service=null;
		
	}
	
}