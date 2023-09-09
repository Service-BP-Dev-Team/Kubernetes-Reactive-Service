package com.reactive.service.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.reactive.service.model.configuration.Configuration;
import com.reactive.service.model.configuration.Data;
import com.reactive.service.model.configuration.PendingLocalFunctionComputation;
import com.reactive.service.model.configuration.Task;
import com.reactive.service.model.specification.DecompositionRule;
import com.reactive.service.model.specification.Equation;
import com.reactive.service.model.specification.FunctionExpression;
import com.reactive.service.model.specification.GAG;
import com.reactive.service.model.specification.IdExpression;
import com.reactive.service.model.specification.Parameter;
import com.reactive.service.model.specification.ServiceInstance;

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
		Task t = context.getStartingTask();
		Hashtable<Task, List<DecompositionRule>> readyTasks= context.getReadyTasks();
		while(readyTasks.size()!=0) {
			for(Task task: readyTasks.keySet()) {
				System.out.println(task.getService().getName());
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
			 //create inputs
			 for( Parameter par: si.getService().getInputParameters()) {
				 Data d= new Data();
				 d.setParameter(par);
				 inputs.add(d);
			 }
			 //create ouputs
			 for(Parameter par: si.getService().getOutputParameters()) {
				 Data d= new Data();
				 d.setParameter(par);
				 outputs.add(d);
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
	}
	
	public void createDataLink(Task task, DecompositionRule rule, Hashtable<ServiceInstance,Task> servicetask) {
		for(Equation eq:rule.getSemantics()) {
			IdExpression id = eq.getLeftpart();
			ServiceInstance si = id.getServiceInstance();
			Task taskLeft = servicetask.get(si);
			Data dleft=findDataByParameterNameInTask(taskLeft,id.getParameterName());
			PendingLocalFunctionComputation pendingComputation= new PendingLocalFunctionComputation();
			pendingComputation.setDataToCompute(dleft);
			pendingComputation.setActualParameters(new ArrayList<Data>());
			if(eq.getRightpart() instanceof IdExpression) {
				IdExpression right = (IdExpression) eq.getRightpart();
				pendingComputation.setIdFunction(true);
				Task taskRight = servicetask.get(right.getServiceInstance());
				Data dright = findDataByParameterNameInTask(taskRight, right.getParameterName());
				pendingComputation.getActualParameters().add(dright);
			}
			else {
				FunctionExpression funcExpr = (FunctionExpression) eq.getRightpart();
				pendingComputation.setFunctionDeclaration(funcExpr.getFunction());
				for(IdExpression right: funcExpr.getIdExpressions()) {
					Task taskRight = servicetask.get(right.getServiceInstance());
					Data dright = findDataByParameterNameInTask(taskRight, right.getParameterName());
					pendingComputation.getActualParameters().add(dright);
				}
			}
			configuration.getPendingLocalComputations().add(pendingComputation);
		}
	}
	
	public Data findDataByParameterNameInTask(Task task,String parameterName) {
		Data result=null;
		for(Data d : task.getInputs()) {
			if(d.getParameter().getName().equals(parameterName)) {
				return d;
			}
		}
		for(Data d : task.getOutputs()) {
			if(d.getParameter().getName().equals(parameterName)) {
				return d;
			}
		}
		for(Data d : task.getLocals()) {
			if(d.getParameter().getName().equals(parameterName)) {
				return d;
			}
		}
		// if the parameter has been not find so far its means that is a local one
		// we create the data
		result = new Data();
		Parameter par = new Parameter();
		par.setName(parameterName);
		result.setParameter(par);
		task.getLocals().add(result);
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
	
	
	

}
