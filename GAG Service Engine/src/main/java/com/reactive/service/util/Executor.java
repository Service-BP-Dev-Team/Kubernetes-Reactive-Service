package com.reactive.service.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Executor {
	private GAG gag;
	private Context context;
	private Configuration configuration;
	private String serviceCallId = "";
	private boolean subExecutor = false;
	private ConcurrentHashMap<String, Pair<String, Data>> outSubscriptions = new ConcurrentHashMap<String, Pair<String, Data>>();
	private boolean running = false;
	private Lock lock = new ReentrantLock();
	private Boolean terminated = false;
	private HashSet <Data> inputs = new HashSet<Data>();
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

	public String getServiceCallId() {
		return serviceCallId;
	}

	public void setServiceCallId(String serviceCallId) {
		this.serviceCallId = serviceCallId;
	}

	public boolean isSubExecutor() {
		return subExecutor;
	}

	public void setSubExecutor(boolean subExecutor) {
		this.subExecutor = subExecutor;
	}

	public ConcurrentHashMap<String, Pair<String, Data>> getOutSubscriptions() {
		return outSubscriptions;
	}

	public void setOutSubscriptions(ConcurrentHashMap<String, Pair<String, Data>> outSubscriptions) {
		this.outSubscriptions = outSubscriptions;
	}
	
	

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public Boolean getTerminated() {
		return terminated;
	}

	public void setTerminated(Boolean terminated) {
		this.terminated = terminated;
	}

	public void lightExecute(Data d) {
		// if(running) return ; //when it is already running we do nothing
		
		Runnable runner = () -> {
			
		
			
			lock.lock();
			
			if (terminated) {
				lock.unlock();
				return;
			}
			if (d!=null && !inputs.contains(d)) {
				// the inputs has already been treated
				lock.unlock();
				return;
			}
			try {
				//inputs.remove(d);
				running = true;
				removeAlreadyTreatedInputs();
				computePendingLocalComputations();
				// Really important is mandatory to compute pending local computations before
				// looking for ready task
				//System.out.println(terminated);
				//System.out.println(context);
				Hashtable<Task, List<Pair<DecompositionRule, ArrayList>>> readyTasks = context.getReadyTasks();
				// System.out.println("" + readyTasks);
				

				while (readyTasks.size() != 0) {
					for (Task task : readyTasks.keySet()) {
						Pair<DecompositionRule, ArrayList> firstApplicable = readyTasks.get(task).get(0);
						applyRule(task, firstApplicable.getFirst(), firstApplicable.getSecond());
						//removeAlreadyTreatedInputs();
						computePendingLocalComputations();
					}
					readyTasks = context.getReadyTasks();
				}
				
				running = false;
				if (!continuous()) {
					terminated=true;
					if(!subExecutor) {
					clearAllData();
					}
				}
			} finally {
				lock.unlock();
			}
		};
		Thread.startVirtualThread(runner);
		//new Thread(runner).start();
	}

	public void execute() {
		if (configuration == null) {
			Task t = context.getStartingTask();
		}
		//System.out.println("execute large");
		Runnable runner = () -> {
			// initialization
			lightExecute(null);

			/*
			 * while (continuous()) { // System.out.println("continous is true");
			 * 
			 * // terminateTasks(); // we sleeping after freeing some memory with
			 * terminateTasks() try {
			 * Thread.sleep(InMemoryWorkspace.getReadyTaskWaitTime()); } catch
			 * (InterruptedException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); }
			 * 
			 * } // we clear all data clearAllData();
			 */
		};
		Thread.startVirtualThread(runner);

	}

	public boolean continuous() {
		Boolean result = false;
		for (Data output : configuration.getRoot().getOutputs()) {
			if (!output.isDefined() || outSubscriptions.get(output.getId()) != null) {
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
				//t.setRemote(si.isRemote());
				t.setExternalCall(si.isRemote());
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
		task.getSubTasks().addAll(substasks);
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
				// System.out.println(id.asString()+"="+right.asString());
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
			// System.out.println("array found " + parameterName);
			// System.out.println(idex.getServiceInstance().getService().getName());
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

		// wait for all local function to end first
		/*
		 * boolean canNotify = false; while (!canNotify) { canNotify=true;
		 * for(OutputWatcher wt:watchers) { if(!wt.isEnded()) { canNotify=false; break;
		 * } } try { Thread.sleep(5); } catch (InterruptedException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); } }
		 */
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
		if (configuration != null)
			makeFastAccess(configuration.getRoot().getInputs());
	}

	public void invokeRemote(List<Task> tasks) {
		for (Task t : tasks) {
			// invoke now remote service in separate threads
			
			final boolean localCall = !t.isExternalCall();
			t.setRemote(true); // now we put all task to remote to notice the fact that we want to execute 
			// all tasks remotely in a different executor. 
			// localCall tell us if we need to retrieve the ip of an available engine in the cluster
			// to exetute the task
			Runnable runner = () -> {
				
					String receiver = null;
					if(!localCall) {
						receiver=InMemoryWorkspace.bind(t.getService().getKubename());
					}
					if (localCall ||(receiver.equals(InMemoryWorkspace.getLocalHostIp())
							&& !InMemoryWorkspace.isForcedTCPOnLocalhost())) {

						// we do not use tcp when the call is local

						Task newTask = new Task();
						newTask.setDataGroups(t.getDataGroups());
						newTask.setInputs(t.getInputs());
						newTask.setLocals(t.getLocals());
						newTask.setOutputs(t.getOutputs());
						newTask.setService(t.getService());
						newTask.setSubTasks(t.getSubTasks());
						ServiceCall sc = new ServiceCall();
						sc.setTask(newTask);
						Configuration conf = new Configuration();
						conf.setRoot(sc.getTask());
						conf.getRoot().setRemote(false);// transform the task to a local one
						conf.setId(sc.getId());
						// add the configuration
						Executor exec = new Executor();
						Context ctx = new Context();
						ctx.setExecutor(exec);
						exec.setContext(ctx);
						exec.setSubExecutor(true);
						exec.setConfiguration(conf);
						//make fast access
						makeFastAccess(newTask.getOutputs());
						exec.setGag(gag);
						exec.setServiceCallId(sc.getId());
						
						InMemoryWorkspace.inMemoryCalls.put(sc.getId(), exec);
						// execute the task
						exec.execute();
					} else {
						//make fast access
						makeFastAccess(t.getOutputs());
						ServiceCall sc = new ServiceCall();
						sc.setTask(t);
						Service emptyService = new Service();
						emptyService.setName(t.getService().getName());
						emptyService.setKubename(t.getService().getKubename());
						t.setService(emptyService);

						InMemoryWorkspace.processOutServiceCall(sc, receiver, this);
					}
				
				// else {
				// makeFastAccess(t);
				// }
			};
			Thread.startVirtualThread(runner);
		}
	}

	public void notifySubscribers() {

		Set<String> keys = outSubscriptions.keySet();
		// transfom to static array to handle concurency
		ArrayList<String> keyArray = new ArrayList<String>();
		for (String id : keys) {
			keyArray.add(id);
		}
		for (String id : keyArray) {
			Pair<String, Data> p = outSubscriptions.get(id);
			if (p != null) {
				Data d = p.getValue();
				if (d.isDefined()) {
					Notification nf = new Notification();
					nf.setData(d);
					// now notify in separate thread

					outSubscriptions.remove(nf.getData().getId());
					//Runnable runner = () -> {
						InMemoryWorkspace.processOutNotification(nf, p.getKey());
					//};
					//Thread.startVirtualThread(runner);
				}
			}

		}

	}

	private void terminateTasks(Task t) {

		terminateIfPossible(t);
	}

	private void clearAllData() {
		terminated=true;
			// when it is a subs executor the task to terminate tasks
			// and data is left to the parent executor
			// wit some time for the notification to be performed before clearing
			// everything
			final Task root = configuration.getRoot();
					InMemoryWorkspace.inMemoryCalls.remove(serviceCallId);
					configuration.clearData();
					configuration = null;
					this.gag = null;
					this.context = null;
					this.serviceCallId = null;

					terminateTasks(root);
					// clear all data recursively
					clearTaskData(root);
		
		
		
		/*
		 * System.out.println("the memory call size is : "+
		 * InMemoryWorkspace.inMemoryCalls.size());
		 * System.out.println("the memory in subscription size is : "+
		 * InMemoryWorkspace.inSubscriptions.size());
		 * System.out.println("the memory out subscription size is : "+
		 * outSubscriptions.size());
		 * System.out.println("the memory discard already subscription size is  : "+
		 * InMemoryWorkspace.discardNotificationsAlreadyDone.size());
		 */
	}

	private void clearTaskData(Task t) {
		for (Data ind : t.getInputs()) {
			InMemoryWorkspace.discardNotificationsAlreadyDone.remove(ind.getId());
		}
		for (Task st : t.getSubTasks()) {
			clearTaskData(st);
		}
		t.clearData();

	}

	private void checkTerminated(Task t) {
		for (Task st : t.getSubTasks()) {
			if (!st.isTerminated()) {
				return;
			}
		}
		// here we see that all the substask are terminated
		// now we check if all the data are terminated
		for (Data d : t.getAllWithoutLocalData()) {
			// check if the data is terminated
			if (!d.isTerminated()) {
				// try to terminate the data
				if (InMemoryWorkspace.inSubscriptions.get(d.getId()) != null
						|| outSubscriptions.get(d.getId()) != null) {
					return;
				}
				d.setTerminated(true);
			}

		}
		// here it means that all the data has been terminated
		// we terminate the task
		t.setTerminated(true);

	}

	private void terminateIfPossible(Task t) {
		if (!t.isTerminated()) {
			for (Task st : t.getSubTasks()) {
				terminateIfPossible(st);
			}
			checkTerminated(t);
		} else {
			// we do nothing
		}

	}
	// this method is used to take into account all inputs
	private void makeFastAccess(List<Data> datas) {
		//System.out.println("make fast access");
		lock.lock();
		try {
			for (Data d : datas) {
				inputs.add(d);
				d.setExecutor(this);
				}
			
		} finally {
			lock.unlock();
		}
		
	}
	
	// this method allow to not process already processed inputs
	// it should be called on a block preceded by lock.lock()
	public void removeAlreadyTreatedInputs() {
		ArrayList<Data> toRemove = new ArrayList<Data>();
		for(Data d : inputs) {
			if (d.isDefined()){
				toRemove.add(d);
			}
		}
		inputs.removeAll(toRemove);
		
	}

}
