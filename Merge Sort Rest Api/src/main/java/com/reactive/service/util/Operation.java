package com.reactive.service.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.reactive.service.model.configuration.Data;
import com.reactive.service.model.configuration.DataGroup;
import com.reactive.service.model.configuration.Task;
import com.reactive.service.model.specification.DecompositionRule;
import com.reactive.service.model.specification.Guard;
import com.reactive.service.model.specification.Parameter;
import com.reactive.service.model.specification.Service;

public class Operation {

	public static Task createTask(Service s, Hashtable<String, Object> inputs) {
		// TODO Auto-generated method stub
		Task t=new Task();
		t.setService(s);
		
		for(Parameter par: s.getInputParameters()) {
			Data d= new Data();
			d.setValue(inputs.get(par.getName()));
			d.setParameter(par);
			t.getInputs().add(d);
		}
		
		for(Parameter par: s.getOutputParameters()) {
			if(par.isArray()) {
				 DataGroup dg=DataGroup.createDataGroupFromParameter(par);
				// we add all the output data of the group to the task outputs
				 t.getOutputs().addAll(dg.getCollection());
				 t.getDataGroups().add(dg);
			 }else {
				 Data d= new Data();
				 d.setParameter(par);
				 t.getOutputs().add(d); 
			 }
		}
		t.setLocals(new ArrayList<>());
		return t;
	}
	
	public static boolean isApplicable(Task t,DecompositionRule rule) {
		boolean result=false;
		if(!t.isOpen()) return false;
		Guard guard = rule.getGuard();
		if(guard==null) {
			return true;
		}
		ArrayList<Object> arguments = t.getArgumentValues();
		Object res = executeMethodWithReflexion(guard.getLocation(),guard.getMethod(), arguments);
		if(res!=null) {result=(Boolean)res;}
		return result;
	}
	
	public static Object executeMethodWithReflexion(String classname,String methodName,List<Object> argumentValues) {
		Object result =null;
			try {
		            // Load the class by its string name
		            Class<?> clazz = Class.forName(classname);
		            Object[] arguments = new Object[argumentValues.size()];
		            for(int i=0;i<argumentValues.size();i++) {
		            	arguments[i]=argumentValues.get(i);
		            }
		            // Instantiate an object of the loaded class
		            Object obj = clazz.newInstance();
		            Method method = findMethodByName(clazz, methodName);
		           result= method.invoke(obj, arguments);

		            
		        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
		            e.printStackTrace();
		        } catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    
		

		
		return result;
	}
	
    private static Method findMethodByName(Class<?> clazz, String methodName) throws NoSuchMethodException {
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new NoSuchMethodException("Method " + methodName + " not found in class " + clazz.getName());
    }
    
    //test of the method execution
    public static void main(String args[]) {
    	String className="com.local.Func", methodName="f";
    	ArrayList values = new ArrayList();
    	values.add(3);values.add(5);
    	Object result=executeMethodWithReflexion(className, methodName, values);
    	System.out.println(result);
    }

}
