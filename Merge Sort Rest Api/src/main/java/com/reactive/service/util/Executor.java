package com.reactive.service.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.reactive.service.app.api.InMemoryWorkspace;
import com.reactive.service.app.api.Notification;
import com.reactive.service.app.api.Pair;
import com.reactive.service.app.api.ServiceCall;
import com.reactive.service.model.configuration.Configuration;
import com.reactive.service.model.configuration.Data;
import com.reactive.service.model.configuration.DataGroup;
import com.reactive.service.model.configuration.PendingLocalFunctionComputation;
import com.reactive.service.model.configuration.Task;
import com.reactive.service.model.specification.ArrayExpression;
import com.reactive.service.model.specification.DecompositionRule;
import com.reactive.service.model.specification.Equation;
import com.reactive.service.model.specification.FunctionExpression;
import com.reactive.service.model.specification.GAG;
import com.reactive.service.model.specification.IdExpression;
import com.reactive.service.model.specification.Parameter;
import com.reactive.service.model.specification.Service;
import com.reactive.service.model.specification.ServiceInstance;
import static com.consulner.app.Configuration.getObjectMapper;

public class Executor {
	private GAG gag;
	private Context context;
	private Configuration configuration;

	public Executor() {
		
	}
	public Executor (Context ctx, GAG gag) {
		this.context = ctx;
		this.context.setExecutor(this);
		this.gag=gag;
	}
	public GAG getGag() {
		return gag;
	}

	public void setGag(GAG gag) {
		this.gag = gag;
	}
	
	public void execute() {
		if(configuration==null) {Task t = context.getStartingTask();}
		//System.out.println("size of pending local computation : "+configuration.getPendingLocalComputations().size());
		Hashtable<Task, List<DecompositionRule>> readyTasks= context.getReadyTasks();
		computePendingLocalComputations();
		while(readyTasks.size()!=0) {
			for(Task task: readyTasks.keySet()) {
				//System.out.println(task.getService().getName());
				applyRule(task,readyTasks.get(task).get(0));
				computePendingLocalComputations();
			}
			readyTasks= context.getReadyTasks();
		}
		
	}
	private void applyRule(Task task, DecompositionRule rule) {
		
		//match current Task;
		Hashtable<ServiceInstance,Task> serviceTask = new Hashtable<>();
		serviceTask.put( rule.getCurrentServiceInstance(),task);
		//create and match sub tasks
		ArrayList<Task> substasks = new ArrayList<Task>();
		for(ServiceInstance si : rule.getServiceInstances()) {
		 if(si!=rule.getCurrentServiceInstance()) {
			 Task t= new Task();
			 ArrayList<Data> inputs = new ArrayList<Data>();
			 ArrayList<Data> outputs = new ArrayList<Data>();
			 ArrayList<Data> locals = new ArrayList<Data>();
			 t.setInputs(inputs);
			 t.setOutputs(outputs);
			 t.setLocals(locals);
			 t.setRemote(si.isRemote());
			 //create inputs
			 for( Parameter par: si.getService().getInputParameters()) {
				 if(par.isArray()) {
					 DataGroup dg=DataGroup.createDataGroupFromParameter(par);
					// we add all the input data of the group to the task inputs
					 inputs.addAll(dg.getCollection());
					 
					 t.getDataGroups().add(dg);
				 }else {
					 Data d= new Data();
					 d.setParameter(par);
					 inputs.add(d); 
				 }
			 }
			 //create outputs
			 for(Parameter par: si.getService().getOutputParameters()) {
				 if(par.isArray()) {
					 DataGroup dg=DataGroup.createDataGroupFromParameter(par);
					// we add all the output data of the group to the task outputs
					 outputs.addAll(dg.getCollection());
					 t.getDataGroups().add(dg);
				 }else {
					 Data d= new Data();
					 d.setParameter(par);
					 outputs.add(d); 
				 }
			 }
			 t.setService(si.getService());
			 substasks.add(t);
			 serviceTask.put(si, t);
		 }
		}
		task.setSubTasks(substasks);
		createDataLink(task, rule, serviceTask);
		task.setAppliedRule(rule.getName());
		task.setOpen(false);
		
		// invoke remote task
		invokeRemote(substasks);
	}
	
	public void createDataLink(Task task, DecompositionRule rule, Hashtable<ServiceInstance,Task> servicetask) {
		for(Equation eq:rule.getSemantics()) {
			IdExpression id = eq.getLeftpart();
			ServiceInstance si = id.getServiceInstance();
			Task taskLeft = servicetask.get(si);
			List<Data> listLeft = findDataByParameterNameInTask(taskLeft,id.getParameterName(),id);
			Data dleft=listLeft.get(0);
			if(eq.getRightpart() instanceof IdExpression) {
				IdExpression right = (IdExpression) eq.getRightpart();
				Task taskRight = servicetask.get(right.getServiceInstance());
				List<Data> listRight = findDataByParameterNameInTask(taskRight, right.getParameterName(),right);
				for(int i=0;i<listLeft.size();i++) {
					PendingLocalFunctionComputation pendingComputation = new PendingLocalFunctionComputation();
					pendingComputation.setIdFunction(true);
					pendingComputation.setActualParameters(new ArrayList<Data>());
					pendingComputation.setDataToCompute(listLeft.get(i));
					pendingComputation.getActualParameters().add(listRight.get(i));
					configuration.getPendingLocalComputations().add(pendingComputation);
				}
				
			}
			else {
				PendingLocalFunctionComputation pendingComputation= new PendingLocalFunctionComputation();
				pendingComputation.setDataToCompute(dleft);
				pendingComputation.setActualParameters(new ArrayList<Data>());
				FunctionExpression funcExpr = (FunctionExpression) eq.getRightpart();
				pendingComputation.setFunctionDeclaration(funcExpr.getFunction());
				pendingComputation.setThreadFunction(funcExpr.isThreadFunction());
				for(IdExpression right: funcExpr.getIdExpressions()) {
					Task taskRight = servicetask.get(right.getServiceInstance());
					Data dright = findDataByParameterNameInTask(taskRight, right.getParameterName(),right).get(0);
					pendingComputation.getActualParameters().add(dright);
				}
				configuration.getPendingLocalComputations().add(pendingComputation);
			}
			
		}
		//System.out.println("size of pending local computation : "+configuration.getPendingLocalComputations().size());
	}
	
	public List<Data> findDataByParameterNameInTask(Task task,String parameterName, IdExpression idex) {
		List<Data> result=new ArrayList<>();
		if(idex.isArray()) {
			DataGroup dg=task.findGroupByParameterName(parameterName);
			System.out.println("array found");
			System.out.println(idex.getServiceInstance().getService().getName());
			return dg.getCollection();
		}
		if(! (idex instanceof ArrayExpression)) {
		for(Data d : task.getInputs()) {
			if(d.getParameter().getName().equals(parameterName)) {
				result.add(d);
				return result;
			}
		}
		for(Data d : task.getOutputs()) {
			if(d.getParameter().getName().equals(parameterName)) {
				result.add(d);
				return result;
			}
		}}
		for(Data d : task.getLocals()) {
			if(d.getParameter().getName().equals(parameterName)) {
				result.add(d);
				return result;
			}
		}
		// if the parameter has been not find so far its means that is a local one
		// we create the data
		
		// first we check if the data match an index in data group
		if(idex instanceof ArrayExpression) {
			Data d = new Data();
			DataGroup dg = task.findGroupByParameterName(idex.getParameterName());
			d.setGroup(dg);
			ArrayExpression arrayIdex = (ArrayExpression) idex;
			Data index = findDataByParameterNameInTask(task, arrayIdex.getIndex().getParameterName(), arrayIdex.getIndex()).get(0);
			d.setIndex(index);
			task.getLocals().add(d);
			Parameter par = new Parameter();
			par.setName(arrayIdex.asString());
			d.setParameter(par);
			result.add(d);
			return result;
		}
		else {
		// it is a local data not corresponding to an array index
		// we create a simple local data
		Data d = new Data();
		Parameter par = new Parameter();
		par.setName(parameterName);
		d.setParameter(par);
		task.getLocals().add(d);
		result.add(d);
		}
		return result;
	}
	
	// execute pending local computations
	public void computePendingLocalComputations(){
		
		List<PendingLocalFunctionComputation> readyFunctions = getReadyLocalComputations();
		while(readyFunctions.size()!=0) {
			//execute the functions
			for(PendingLocalFunctionComputation func: readyFunctions) {
				func.execute();
				configuration.getPendingLocalComputations().remove(func);// we remove after execution
			}
			readyFunctions=getReadyLocalComputations();
		}
		
		// notify the subscriber that some data has been produced
		notifySubscribers();
		
	}
	
	public List<PendingLocalFunctionComputation> getReadyLocalComputations(){
		ArrayList<PendingLocalFunctionComputation> result = new ArrayList<>();
		for(PendingLocalFunctionComputation func:configuration.getPendingLocalComputations()) {
			if(func.isReady()) {
				result.add(func);
			}
		}
		return result;
	}
	
	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}
	public Configuration getConfiguration() {
		return configuration;
	}
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
	public void invokeRemote(List<Task> tasks) {
		for(Task t : tasks) {
			if(t.isRemote()) {
			ServiceCall sc = new ServiceCall();
			sc.setTask(t);
			Service emptyService =new Service();
			emptyService.setName(t.getService().getName());
			emptyService.setKubename(t.getService().getKubename());
			t.setService(emptyService);
			/*try {
				System.out.println(getObjectMapper().writeValueAsString(t));
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			InMemoryWorkspace.processOutServiceCall(sc, this);
			}
		}
	}
	
	public void notifySubscribers() {
		 Set<String> keys = InMemoryWorkspace.outSubscriptions.keySet();
		 //transfom to static array to handle concurency
		 ArrayList<String> keyArray=new ArrayList<String>();
		 for(String id:keys) {
			 keyArray.add(id);
		 }
		 for (String id: keyArray){
				Pair<String, Data> p  = InMemoryWorkspace.outSubscriptions.get(id);
				if(p!=null) {Data d= p.getValue();
				if(d.isDefined()) {
					Notification nf = new Notification();
					nf.setData(d);
					InMemoryWorkspace.processOutNotification(nf,p.getKey());
					
					
				}}
				
			
		}
		
	}
	
	

}
