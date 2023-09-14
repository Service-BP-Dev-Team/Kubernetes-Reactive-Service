package com.reactive.service.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.reactive.service.model.configuration.Configuration;
import com.reactive.service.model.configuration.Task;
import com.reactive.service.model.specification.DecompositionRule;
import com.reactive.service.model.specification.GAG;
import com.reactive.service.model.specification.Parameter;
import com.reactive.service.model.specification.Service;

public class Context {

	private Executor executor;
	private Console console;

	public Context() {
		this.console = new Console();
	}
	public Executor getExecutor() {
		return executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public Task getStartingTask() {
		// TODO Auto-generated method stub
		GAG gag= executor.getGag();
		int cpt=0;
		console.debug("Select a Service to Start");
		for(Service s: gag.getServices()) {
			cpt++;
			console.debug(cpt+"-"+s.getName());
		}
		String choice = console.readConsoleLine();
		Service s=gag.getServices().get(Integer.parseInt(choice)-1);
		Hashtable<String, Object> inputs = new Hashtable<>();
		
		for(Parameter par : s.getInputParameters()) {
			String value = console.readConsoleLine("Enter the value of the parameter "+par.getName()+" :");
			inputs.put(par.getName(), value);
		}
		Configuration conf = new Configuration();
		Task result= Operation.createTask(s,inputs);
		conf.setRoot(result);
		executor.setConfiguration(conf);
		return result;
	}
	
	public Hashtable<Task,List<DecompositionRule>> getReadyTasks(){

		// return the task that are ready using reflexion to execute guards.
		List <Task> tasks = getPendingTasks();
		
		// remove from the list the remote tasks
		List <Task> localtasks = new ArrayList<>();
		for(Task t: tasks) {
			if(!t.isRemote()) {
				localtasks.add(t);
			}
		}
		Hashtable<Task,List<DecompositionRule>> result = new Hashtable<Task,List<DecompositionRule>>();
		for(Task t: localtasks) {
			List<DecompositionRule> list = getReadyRules(t);
			if(list!=null && !list.isEmpty()) {
				result.put(t, list);
			}
		}
		
		return result;
	}
	
	// get the list of open node tasks
	public List<Task> getPendingTasks(){
		Configuration conf = this.executor.getConfiguration();
		return getPendingTasksFromRoot(conf.getRoot());
	}
	
	public ArrayList<Task> getPendingTasksFromRoot(Task root) {
		ArrayList<Task> openTasks = new ArrayList<Task>();
		if (root.isOpen()) {
			openTasks.add(root);
		} else {
			for (Task element : root.getSubTasks()) {
				ArrayList<Task> subOpenTasks = getPendingTasksFromRoot( element);
				openTasks.addAll(subOpenTasks);
			}
		}
		return openTasks;
	}
	
	public List<DecompositionRule> getReadyRules(Task task){
		ArrayList<DecompositionRule> result = new ArrayList<>();
		for(DecompositionRule rule: task.getService().getRules()) {
			if(Operation.isApplicable(task, rule)) {
				result.add(rule);
			}
		}
		return result;
	}
	
	
}
