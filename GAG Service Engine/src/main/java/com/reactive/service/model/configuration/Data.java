package com.reactive.service.model.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.reactive.service.app.api.InMemoryWorkspace;
import com.reactive.service.model.specification.Parameter;
import com.reactive.service.util.Executor;
import com.reactive.service.util.NameGenerator;

public class Data implements Serializable{

	private static int idCounter=0;
	
	private Parameter parameter;
	
	private Object value;
	
	private boolean defined;
	
	private String id;
	
	private String serviceCallId; // use to quickly retrieve a configuration that have a data
	
	private String host; // use to quickly retrieve the host where to send the data
	
	private DataGroup group; 
	private Data index;
	private OutputWatcher watcher;
	private boolean terminated; //when the data has been exploited and destroyed
	public Data() {
		idCounter++;
	    id = UUID.randomUUID().toString();
	}
	
	
	

	public void setId(String id) {
		this.id = id;
	}




	public static int getIdCounter() {
		return idCounter;
	}

	public static void setIdCounter(int idCounter) {
		Data.idCounter = idCounter;
	}
	
	public Parameter getParameter() {
		return parameter;
	}

	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}

	public Object getValue() {
		return value;
	}

	
	public void setValue(Object value) {
		this.value = value;
		//System.out.println("set value executed");
		if(value!=null) {
			this.defined=true;
			if(index!=null) {
			Data match = getMacthedDataDefined();
			// if the match is not set/defined because the
			// index is not yet set/defined 
			// what should we do ?
				if(!match.isDefined()) {
					// we define its match when it is not defined
					match.setValue(value);
				}
			}
			if(watcher!=null) {
				watcher.setExecutionToEndWithData(this);
				//watcher.setData(this);
			}
		}
		// launch the executor as a new data has been defined
		if(value!=null) {
			//setDefined(true);
			Executor exec = InMemoryWorkspace.fastTaskAndCallAccess.get(this.getId());
			if (exec!=null) {
			//	System.out.println("lightexecute");
				exec.lightExecute(this);
			}
		}
	}

	public boolean isDefined() {
		// index data are defined if they corresponding data
		// in a data group is defined
		if(index==null) {
			return defined;
		}else {
			if(defined)return defined;
			Data match = getMacthedDataDefined();
			if(match!=null && match.isDefined()) {
				setValue(match.getValue());
				return true;
			}
			return false;
		}
	}
	@JsonIgnore
	public Data getMacthedDataDefined() {
		if (index==null) return null;
		if(index.isDefined()) {
			Object idx = index.getValue();
			Integer valIdx=0;
			if(idx instanceof Integer) {
				valIdx = (Integer) idx;
			}else {
				valIdx=Integer.parseInt((String)idx);
			}
			Data realData = group.getCollection().get(valIdx);
			return realData;
		}
		return null;
	}

	public void setDefined(boolean defined) {
		this.defined = defined;
	}

	public String getGlobalID(Configuration conf) {
		return conf.getId()+id;
	}


	public String getServiceCallId() {
		return serviceCallId;
	}




	public void setServiceCallId(String serviceCallId) {
		this.serviceCallId = serviceCallId;
	}




	public String getId() {
		return id;
	}




	public String getHost() {
		return host;
	}




	public void setHost(String host) {
		this.host = host;
	}



	@JsonIgnore
	public OutputWatcher getWatcher() {
		return watcher;
	}




	public void setWatcher(OutputWatcher watcher) {
		this.watcher = watcher;
	}




	public DataGroup getGroup() {
		return group;
	}




	public void setGroup(DataGroup group) {
		this.group = group;
	}



	@JsonIgnore
	public Data getIndex() {
		return index;
	}
	
	@JsonIgnore
	public boolean isLocalMatchForArray() {
		return (index!=null);
	}
	
	@JsonIgnore
	public boolean isIndexDefined() {
		if (!isLocalMatchForArray()) return false;
		if (index.isDefined()) {
			return true;
		}
		return false;
	}




	public void setIndex(Data index) {
		this.index = index;
	}



	@JsonIgnore
	public boolean isTerminated() {
		return terminated;
	}




	public void setTerminated(boolean terminated) {
		this.terminated = terminated;
	}
	
	public void clear() {
		this.defined=false;
		this.parameter=null;
		this.group=null;
		this.id=null;
		this.index=null;
		if(this.value instanceof ArrayList) {
			ArrayList array = (ArrayList)this.value;
			array.clear();
		}
		this.value=null;
				
	}
	
	
	
	

}
