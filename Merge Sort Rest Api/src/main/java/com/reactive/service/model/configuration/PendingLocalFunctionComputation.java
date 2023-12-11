package com.reactive.service.model.configuration;

import java.util.ArrayList;
import java.util.Hashtable;

import com.reactive.service.app.api.InMemoryWorkspace;
import com.reactive.service.model.specification.FunctionDeclaration;
import com.reactive.service.util.Operation;

public class PendingLocalFunctionComputation {
	private ArrayList<Data> datasToCompute;
	private ArrayList<Data> actualParameters;
	private FunctionDeclaration functionDeclaration;
	private boolean isIdFunction = false;
	private boolean terminated = false; // this boolean help to know if the function computation is terminated
	private boolean hasBeenExecuted = false;
	private boolean isThreadFunction = false;

	public PendingLocalFunctionComputation() {
		actualParameters = new ArrayList<Data>();
		datasToCompute = new ArrayList<Data>();
	}

	public ArrayList<Data> getDatasToCompute() {
		return datasToCompute;
	}

	public void setDataToCompute(ArrayList<Data> datasToCompute) {
		this.datasToCompute = datasToCompute;
	}

	public ArrayList<Data> getActualParameters() {
		return actualParameters;
	}

	public void setActualParameters(ArrayList<Data> actualParameters) {
		this.actualParameters = actualParameters;
	}

	public FunctionDeclaration getFunctionDeclaration() {
		return functionDeclaration;
	}

	public void setFunctionDeclaration(FunctionDeclaration functionDeclaration) {
		this.functionDeclaration = functionDeclaration;
	}

	public boolean isTerminated() {
		return terminated;
	}

	public void setTerminated(boolean terminated) {
		this.terminated = terminated;
	}

	public boolean isIdFunction() {
		return isIdFunction;
	}

	public void setIdFunction(boolean isIdFunction) {
		this.isIdFunction = isIdFunction;
	}

	public boolean isReady() {
		boolean result = true;
		if (hasBeenExecuted)
			return false;
		for (Data dataToCompute : datasToCompute) {
			if (dataToCompute.isLocalMatchForArray() && !dataToCompute.isIndexDefined()) {
				return false;
				// this is really important because when we compute an element of
				// an array we need to be sure that index referencing the element
				// has been defined, otherwise we will not know which entry of the table
				// to update
			}
		}
		for (Data d : actualParameters) {
			if (!d.isDefined()) {
				return false;
			}
		}
		return result;
	}

	public Object execute() {
		Object result = null;
		if (isReady()) {
			// System.out.println("I execute a function");
			this.setHasBeenExecuted(true);
			if (isIdFunction) {
				result = actualParameters.get(0).getValue();
				datasToCompute.get(0).setValue(result);
				datasToCompute.get(0).setDefined(true);
				// System.out.println(result);
			} else {
				ArrayList<Object> params = groupParameters();
				if (!isThreadFunction) {
					// execute in an anonymous thread
					Thread t = new Thread(() -> {
						Object output = Operation.executeMethodWithReflexion(functionDeclaration.getLocation(),
								functionDeclaration.getMethod(), params);
						if (output instanceof ArrayList && functionDeclaration != null
								&& functionDeclaration.isMultiOutput()) {
							ArrayList results = (ArrayList) output;
							// this is to handle function that returns an array
							for (int i = 0; i < results.size(); i++) {
								datasToCompute.get(i).setValue(results.get(i));
								datasToCompute.get(i).setDefined(true);
							}
						} else {
							datasToCompute.get(0).setValue(output);
							datasToCompute.get(0).setDefined(true);
						}
					});
					t.start();
				} else {
					// execute in the thread and make thread available in the
					// function as first argument
					ArrayList<Long> processId = new ArrayList<Long>();
					Thread t = new Thread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Object output = Operation.executeMethodWithReflexion(functionDeclaration.getLocation(),
									functionDeclaration.getMethod(), params);
							// we place the result in a hashtable with the process key when it will be
							// defined
							InMemoryWorkspace.threadFunctionProcess.put(processId.get(0), output);

						}
					});
					result = t.getId();
					processId.add(t.getId());
					t.start();
				}
			}

		}
		return result;
	}

	public boolean isHasBeenExecuted() {
		return hasBeenExecuted;
	}

	public void setHasBeenExecuted(boolean hasBeenExecuted) {
		this.hasBeenExecuted = hasBeenExecuted;
	}

	public boolean isThreadFunction() {
		return isThreadFunction;
	}

	public void setThreadFunction(boolean isThreadFunction) {
		this.isThreadFunction = isThreadFunction;
	}

	private ArrayList<Object> groupParameters() {
		ArrayList result = new ArrayList();
		ArrayList<DataGroup> groups = new ArrayList<>();
		Hashtable<DataGroup, ArrayList> hash = new Hashtable<DataGroup, ArrayList>();
		for (Data d : actualParameters) {
			if (d.getGroup() == null) {
				result.add(d.getValue());
			} else {
				DataGroup dg = d.getGroup();
				ArrayList array = hash.get(dg);
				if (array == null) {
					array = new ArrayList<>();
					hash.put(dg, array);
					result.add(array);
				}
				array.add(d.getValue());
			}
		}
		return result;

	}

}
