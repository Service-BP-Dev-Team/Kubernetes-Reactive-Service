package com.reactive.service.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.reactive.service.app.api.Pair;
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
	
	public Hashtable<Task,List<Pair<DecompositionRule,ArrayList>>> getReadyTasks(){

		// return the task that are ready using reflexion to execute guards.
		List <Task> tasks = getPendingTasks();
		
		// remove from the list the remote tasks
		List <Task> localtasks = new ArrayList<>();
		for(Task t: tasks) {
			if(!t.isRemote()) {
				localtasks.add(t);
			}
		}
		Hashtable<Task,List<Pair<DecompositionRule,ArrayList>>> result = new Hashtable<Task,List<Pair<DecompositionRule,ArrayList>>>();
		for(Task t: localtasks) {
			List<Pair<DecompositionRule,ArrayList>> list = getReadyRules(t);
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
		} else if(!root.isRemote()){
			for (Task element : root.getSubTasks()) {
				ArrayList<Task> subOpenTasks = getPendingTasksFromRoot( element);
				openTasks.addAll(subOpenTasks);
			}
		}
		return openTasks;
	}
	
	public List<Pair<DecompositionRule,ArrayList>> getReadyRules(Task task){
		ArrayList<Pair<DecompositionRule,ArrayList>> result = new ArrayList<>();
		for(DecompositionRule rule: task.getService().getRules()) {
			Object guardResult= Operation.isApplicable(task, rule);
			if(guardResult instanceof ArrayList) {
				// it means the guard have some bindings
				ArrayList guardArray = (ArrayList) guardResult;
				if((Boolean)guardArray.get(0)) {
					ArrayList binding = new ArrayList();
					int i=1;
					for(Parameter bind:rule.getGuard().getBinding()) {
						binding.add(new Pair<Parameter,Object>(bind,guardArray.get(i)));
						i++;
					}
					Pair p= new Pair(rule,binding);
					result.add(p);
				}
			}else {
				if((Boolean) guardResult) {
					Pair p= new Pair(rule,new ArrayList<>());
					result.add(p);
				}
			}
		}
		return result;
	}
	
	
}
