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
import com.reactive.service.model.configuration.OutputWatcher;
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

	public Executor(Context ctx, GAG gag) {
		this.context = ctx;
		this.context.setExecutor(this);
		this.gag = gag;
	}

	public GAG getGag() {
		return gag;
	}

	public void setGag(GAG gag) {
		this.gag = gag;
	}

	public void execute() {
		if (configuration == null) {
			Task t = context.getStartingTask();
		}
		// System.out.println("size of pending local computation :
		// "+configuration.getPendingLocalComputations().size());
		Thread thread = new Thread(() -> {
			while (continuous()) {
				// System.out.println("continous is true");
				Hashtable<Task, List<Pair<DecompositionRule, ArrayList>>> readyTasks = context.getReadyTasks();
				//System.out.println("" + readyTasks);
				computePendingLocalComputations();

				while (readyTasks.size() != 0) {
					for (Task task : readyTasks.keySet()) {
						Pair<DecompositionRule, ArrayList> firstApplicable = readyTasks.get(task).get(0);
						applyRule(task, firstApplicable.getFirst(), firstApplicable.getSecond());
						computePendingLocalComputations();
					}
					readyTasks = context.getReadyTasks();
				}
				try {
					Thread.sleep(InMemoryWorkspace.getReadyTaskWaitTime());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		thread.start();

	}

	public boolean continuous() {
		Boolean result = false;
		for (Data output : configuration.getRoot().getOutputs()) {
			if (!output.isDefined() || InMemoryWorkspace.outSubscriptions.get(output.getId())!=null) {
				// when a data is not defined or
				// has been defined but not yet notified to subscribers
				result = true;
				break;
			}
		}
		return result;
	}

	private void applyRule(Task task, DecompositionRule rule, ArrayList bindings) {

		// match current Task;
		task.setOpen(false); // we lock the task
		Hashtable<ServiceInstance, Task> serviceTask = new Hashtable<>();
		serviceTask.put(rule.getCurrentServiceInstance(), task);
		// create and match sub tasks
		ArrayList<Task> substasks = new ArrayList<Task>();
		for (ServiceInstance si : rule.getServiceInstances()) {
			if (si != rule.getCurrentServiceInstance()) {
				Task t = new Task();
				ArrayList<Data> inputs = new ArrayList<Data>();
				ArrayList<Data> outputs = new ArrayList<Data>();
				ArrayList<Data> locals = new ArrayList<Data>();
				t.setInputs(inputs);
				t.setOutputs(outputs);
				t.setLocals(locals);
				t.setRemote(si.isRemote());
				// create inputs
				for (Parameter par : si.getService().getInputParameters()) {
					if (par.isArray()) {
						DataGroup dg = DataGroup.createDataGroupFromParameter(par);
						// we add all the input data of the group to the task inputs
						inputs.addAll(dg.getCollection());

						t.getDataGroups().add(dg);
					} else {
						Data d = new Data();
						d.setParameter(par);
						inputs.add(d);
					}
				}
				// create outputs
				for (Parameter par : si.getService().getOutputParameters()) {
					if (par.isArray()) {
						DataGroup dg = DataGroup.createDataGroupFromParameter(par);
						// we add all the output data of the group to the task outputs
						outputs.addAll(dg.getCollection());
						t.getDataGroups().add(dg);
					} else {
						Data d = new Data();
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
		// apply bindings defined by the guard
		applyBindings(task, bindings);
		task.setAppliedRule(rule.getName());
		task.setOpen(false);

		// invoke remote task
		invokeRemote(substasks);
	}

	public void createDataLink(Task task, DecompositionRule rule, Hashtable<ServiceInstance, Task> servicetask) {
		for (Equation eq : rule.getSemantics()) {
			IdExpression id = eq.getLeftpart();
			ServiceInstance si = id.getServiceInstance();
			Task taskLeft = servicetask.get(si);

			if (eq.getRightpart() instanceof IdExpression) {
				// first we verify if the right part is an array

				IdExpression right = (IdExpression) eq.getRightpart();
				List<Data> listLeft;
				Task taskRight = servicetask.get(right.getServiceInstance());
				List<Data> listRight = findDataByParameterNameInTask(taskRight, right.getParameterName(), right);
				//System.out.println(id.asString()+"="+right.asString());
				if (right.isArray()) {
					id.setArray(true);
					id.setSize(listRight.size());
					listLeft = findDataByParameterNameInTask(taskLeft, id.getParameterName(), id);
				} else {
					listLeft = findDataByParameterNameInTask(taskLeft, id.getParameterName(), id);
				}
				;
				Data dleft = listLeft.get(0);

				for (int i = 0; i < listLeft.size(); i++) {
					PendingLocalFunctionComputation pendingComputation = new PendingLocalFunctionComputation();
					pendingComputation.setIdFunction(true);
					pendingComputation.setActualParameters(new ArrayList<Data>());
					pendingComputation.getDatasToCompute().add(listLeft.get(i));
					pendingComputation.getActualParameters().add(listRight.get(i));
					configuration.getPendingLocalComputations().add(pendingComputation);
				}

			} else {
				FunctionExpression funcExpr = (FunctionExpression) eq.getRightpart();
				if (funcExpr.getFunction().isMultiOutput()) {
					id.setArray(true);
					id.setSize(funcExpr.getFunction().getOuputSize());
				}
				List<Data> listLeft = findDataByParameterNameInTask(taskLeft, id.getParameterName(), id);
				Data dleft = listLeft.get(0);
				PendingLocalFunctionComputation pendingComputation = new PendingLocalFunctionComputation();
				pendingComputation.getDatasToCompute().addAll(listLeft);
				pendingComputation.setActualParameters(new ArrayList<Data>());

				pendingComputation.setFunctionDeclaration(funcExpr.getFunction());
				pendingComputation.setThreadFunction(funcExpr.isThreadFunction());
				for (IdExpression right : funcExpr.getIdExpressions()) {
					Task taskRight = servicetask.get(right.getServiceInstance());
					List<Data> listRight = findDataByParameterNameInTask(taskRight, right.getParameterName(), right);
					pendingComputation.getActualParameters().addAll(listRight);
				}
				configuration.getPendingLocalComputations().add(pendingComputation);
			}

		}
		// System.out.println("size of pending local computation :
		// "+configuration.getPendingLocalComputations().size());
	}

	public List<Data> findDataByParameterNameInTask(Task task, String parameterName, IdExpression idex) {
		List<Data> result = new ArrayList<>();
		if (idex.isArray()) {
			DataGroup dg = task.findGroupByParameterName(parameterName);
			if (dg == null) {
				// we create a new local array
				dg = DataGroup.createLocalDataGroupFromIdExpression(idex);
				task.getLocalGroups().add(dg);
				task.getLocals().addAll(dg.getCollection());
			}
			//System.out.println("array found " + parameterName);
			//System.out.println(idex.getServiceInstance().getService().getName());
			return dg.getCollection();
		}
		if (!(idex instanceof ArrayExpression)) {
			for (Data d : task.getInputs()) {
				if (d.getParameter().getName().equals(parameterName)) {
					result.add(d);
					return result;
				}
			}
			for (Data d : task.getOutputs()) {
				if (d.getParameter().getName().equals(parameterName)) {
					result.add(d);
					return result;
				}
			}
		}
		for (Data d : task.getLocals()) {
			if (d.getParameter().getName().equals(parameterName)) {
				result.add(d);
				return result;
			}
		}
		// if the parameter has been not find so far its means that is a local one
		// we create the data

		// first we check if the data match an index in data group
		if (idex instanceof ArrayExpression) {
			Data d = new Data();
			DataGroup dg = task.findGroupByParameterName(idex.getParameterName());
			d.setGroup(dg);
			ArrayExpression arrayIdex = (ArrayExpression) idex;
			Data index = findDataByParameterNameInTask(task, arrayIdex.getIndex().getParameterName(),
					arrayIdex.getIndex()).get(0);
			d.setIndex(index);
			task.getLocals().add(d);
			Parameter par = new Parameter();
			par.setName(arrayIdex.asString());
			d.setParameter(par);
			result.add(d);
			return result;
		} else {
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

	// method to apply bindings
	public void applyBindings(Task task, ArrayList bindings) {
		for (Object el : bindings) {
			Pair<Parameter, Object> p = (Pair<Parameter, Object>) el;
			Data d = task.findDataByParameterName(p.getFirst().getName());
			d.setValue(p.getSecond());
		}

	}

	// execute pending local computations
	public void computePendingLocalComputations() {

		List<PendingLocalFunctionComputation> readyFunctions = getReadyLocalComputations();
		
		while (readyFunctions.size() != 0) {
			// execute the functions
			for (PendingLocalFunctionComputation func : readyFunctions) {
				
				func.execute();
				configuration.getPendingLocalComputations().remove(func);// we remove after execution
			}
			readyFunctions = getReadyLocalComputations();
		}

		// notify the subscriber that some data has been produced
		
		//wait for all local function to end first
		/*boolean canNotify = false;
		while (!canNotify) {
			canNotify=true;
			for(OutputWatcher wt:watchers) {
				if(!wt.isEnded()) {
					canNotify=false;
					break;
				}
			}
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		notifySubscribers();

	}

	public List<PendingLocalFunctionComputation> getReadyLocalComputations() {
		ArrayList<PendingLocalFunctionComputation> result = new ArrayList<>();
		for (PendingLocalFunctionComputation func : configuration.getPendingLocalComputations()) {
			if (func.isReady()) {
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
		for (Task t : tasks) {
			if (t.isRemote()) {
				ServiceCall sc = new ServiceCall();
				sc.setTask(t);
				Service emptyService = new Service();
				emptyService.setName(t.getService().getName());
				emptyService.setKubename(t.getService().getKubename());
				t.setService(emptyService);
				/*
				 * try { System.out.println(getObjectMapper().writeValueAsString(t)); } catch
				 * (JsonProcessingException e) { // TODO Auto-generated catch block
				 * e.printStackTrace(); }
				 */
				InMemoryWorkspace.processOutServiceCall(sc, this);
			}
		}
	}

	public void notifySubscribers() {
		Set<String> keys = InMemoryWorkspace.outSubscriptions.keySet();
		// transfom to static array to handle concurency
		ArrayList<String> keyArray = new ArrayList<String>();
		for (String id : keys) {
			keyArray.add(id);
		}
		for (String id : keyArray) {
			Pair<String, Data> p = InMemoryWorkspace.outSubscriptions.get(id);
			if (p != null) {
				Data d = p.getValue();
				if (d.isDefined()) {
					Notification nf = new Notification();
					nf.setData(d);
					InMemoryWorkspace.processOutNotification(nf, p.getKey());

				}
			}

		}

	}

}
