package com.reactive.service.parser;

import java.util.ArrayList;
import java.util.List;

import com.reactive.service.model.specification.*;

public class Parser {
	private List<YAMLSpec> specs;
	private List Pool;
	private GAG gag;
	private static final String SERVICE_KIND = "service";
	private static final String RULE_KIND = "rule";
	

	public List<YAMLSpec> getSpecs() {
		return specs;
	}

	public void setSpecs(List<YAMLSpec> specs) {
		this.specs = specs;
	}
	
	public GAG getGAG() {
		GAG g = new GAG();
		g.setServices(new ArrayList<Service>());
		this.gag=g;
		//process service
		for(YAMLSpec spec : specs) {
			if(spec.getKind().trim().equals(SERVICE_KIND)) {
				processService(spec, g);
			}
		}
		//process rule
		for(YAMLSpec spec : specs) {
			if(spec.getKind().trim().equals(RULE_KIND)) {
				processRule(spec);
			}
		}		
		return g;
				
				
	}
	
	private void processService(YAMLSpec S, GAG g) {
		Service s = new Service();
		s.setName(S.getName().trim());
		s.setRemote(S.isRemote());
		s.setInputParameters(new ArrayList<Parameter>());
		s.setOutputParameters(new ArrayList<Parameter>());
		s.setRemote(S.isRemote());
		s.setKubename(S.getKubename());
		for(YAMLParameter param : S.getInputs()) {
			Parameter par = new Parameter();
			par.setName(param.getName().trim());
			par.setService(s);
			par.setArray(param.isArray());
			par.setSize(param.getSize());
			s.getInputParameters().add(par);
		}
		for(YAMLParameter param : S.getOutputs()) {
			Parameter par = new Parameter();
			par.setName(param.getName().trim());
			par.setService(s);
			par.setArray(param.isArray());
			par.setSize(param.getSize());
			s.getOutputParameters().add(par);
		}
		s.setRules(new ArrayList<DecompositionRule>());
		g.getServices().add(s);
	}

	private void processRule(YAMLSpec R) {
		DecompositionRule rule = new DecompositionRule();
		Service s=findByName(R.getService().trim());
		s.getRules().add(rule);
		rule.setName(R.getName().trim());
		if(R.getFunctions()!=null)processFunction(R.getFunctions(),rule);
		ServiceInstance currentServiceInstance = new ServiceInstance();
		currentServiceInstance.setService(s);
		currentServiceInstance.setName(s.getName()+"-"+rule.getInstanceCounter());
		rule.incrementInstanceCounter();
		rule.getServiceInstances().add(currentServiceInstance);
		rule.setCurrentServiceInstance(currentServiceInstance);
		
		if(R.getActions()!=null)processActions(R.getActions(), rule);
		processRuleGuard(R, rule);
	}
	
	private void processFunction(List<YAMLLocalFunction> Lfunc , DecompositionRule r) {
		
		for(YAMLLocalFunction lf : Lfunc) {
			FunctionDeclaration func = new FunctionDeclaration();
			func.setLocation(lf.getClassPath());
			func.setMethod(lf.getMethod());
			func.setName(lf.getLabel());
			func.setMultiOutput(lf.isMultiOutput());
			func.setOuputSize(lf.getOutputSize());
			r.getFunctionDeclarations().add(func);
		}
	}
	private void processActions(List<String> actions, DecompositionRule rule) {
		for(String action : actions) {
		String[] actionSplit = action.trim().split("=");
		String left=actionSplit[0].trim();
		String right=actionSplit[1].trim();
		String[] computationPart=right.split("\\(");
		if(computationPart.length>1) {
		//check if it is a thread function
		String threadPrefix="__thread ";
		String computationSymbol=computationPart[0].trim();
		boolean threadFunction =false;
			if(computationSymbol.startsWith(threadPrefix)) {
				threadFunction=true;
				//System.out.println(computationSymbol);
				computationSymbol=computationSymbol.substring(threadPrefix.length(),computationSymbol.length());
				computationSymbol=computationSymbol.trim();
			}
		FunctionDeclaration f = findFunctionByName(rule, computationSymbol);
			if(f!=null) {
				processFunctionAction(left,right, f, rule,threadFunction);
			}else {
				processServiceAction(left,right, rule);
			}
		}else {
			processAssignmentAction(left, right, rule);
		}
		
		}
		
	}
	
	private void processServiceAction(String left, String right, DecompositionRule rule) {
		String computationSymbol=right.split("\\(")[0].trim();
		boolean remote=false;
		String callPrefix="__call ";
		if(computationSymbol.startsWith(callPrefix)) {
			remote=true;
			//System.out.println(computationSymbol);
			computationSymbol=computationSymbol.substring(callPrefix.length(),computationSymbol.length());
			computationSymbol=computationSymbol.trim();
		}
		String cutright = right.split("\\(")[1];
		cutright=cutright.substring(0,cutright.length()-1);
		String[] ctleftTable= left.split("\\(");
		String cutleft=left;
		if(ctleftTable.length>1) {
		cutleft = ctleftTable[1];
		cutleft=cutleft.substring(0,cutleft.length()-1);
		}
		String[] rvariables = cutright.split(",");
		String[] lvariables = cutleft.split(",");
		// create idexpressions and equations for inputs
		int inputCounter=0;
		ServiceInstance si = createServiceInstanceByName(rule, computationSymbol);
		si.setRemote(remote);
		for(String rv : rvariables) {
			IdExpression idl = new IdExpression();
			idl.setServiceInstance(si);
			String parameterName = si.getService().getInputParameters().get(inputCounter).getName();
			idl.setParameterName(parameterName);
			rule.getData().add(idl);
			idl.setArray(this.isArrayIdExpression(parameterName, si));
			inputCounter++;
			
			IdExpression idr = getRuleIdExpression(rule, rv.trim());
			
			Equation eq = new Equation();
			eq.setLeftpart(idl);
			eq.setRightpart(idr);
			rule.getSemantics().add(eq);
		}

		// create idexpressions and equations for outputs
		int outputCounter=0;
		for(String lv : lvariables) {
			IdExpression idr = new IdExpression();
			idr.setServiceInstance(si);
			String parameterName = si.getService().getOutputParameters().get(outputCounter).getName();
			idr.setParameterName(parameterName);
			rule.getData().add(idr);
			idr.setArray(this.isArrayIdExpression(parameterName, si));
			outputCounter++;
			
			IdExpression idl = getRuleIdExpression(rule, lv.trim());
			Equation eq = new Equation();
			eq.setLeftpart(idl);
			eq.setRightpart(idr);
			rule.getSemantics().add(eq);
		}
	}
	private void processFunctionAction(String left, String right, FunctionDeclaration f, DecompositionRule rule, boolean threadFunction) {
		IdExpression idl = getRuleIdExpression(rule, left);
		
		String cut = right.split("\\(")[1];
		cut=cut.substring(0,cut.length()-1);
		String[] variables = cut.split(","); 
		FunctionExpression fe = new FunctionExpression();
		fe.setFunction(f);
		fe.setThreadFunction(threadFunction);
		if(!(variables.length==1 && variables[0].trim().equals("")) )
		{// the test above is to handle function 
			for(String v : variables) {
			IdExpression idr=getRuleIdExpression(rule, v.trim());
			fe.getIdExpressions().add(idr);
		}}
		Equation eq = new Equation();
		eq.setLeftpart(idl);
		eq.setRightpart(fe);
		rule.getSemantics().add(eq);
	}
	
	private void processAssignmentAction(String left, String right, DecompositionRule rule) {
		IdExpression idl = getRuleIdExpression(rule, left);
		IdExpression idr = getRuleIdExpression(rule, right);
		Equation eq = new Equation();
		eq.setLeftpart(idl);
		eq.setRightpart(idr);
		rule.getSemantics().add(eq);
		
		
	}
	
	private void processRuleGuard(YAMLSpec R, DecompositionRule rule){
		if(R.getGuard()!=null) {
			Guard guard = new Guard();
			YAMLGuard ymlGuard = R.getGuard();
			guard.setLocation(ymlGuard.getClassPath());
			guard.setMethod(ymlGuard.getMethod());
			if (ymlGuard.getBinding()!=null && ymlGuard.getBinding().size() > 0) {
				for(YAMLParameter ymlPar: ymlGuard.getBinding()) {
					Parameter par = new Parameter();
					par.setArray(ymlPar.isArray());
					par.setName(ymlPar.getName());
					par.setSize(ymlPar.getSize());
					guard.getBinding().add(par);
				}
			}
			rule.setGuard(guard);
		}
		
	}
	
	private Service findByName(String name) {
		Service s= null;
		for( Service sv : gag.getServices()) {
			if (sv.getName().equals(name)) {
				s=sv;
				break;
			}
		}
		return s;	
	}
	
	private String[] formatLeftRight(String st) {
		String[] result = st.trim().split("=");
		return result;
	}
	
	private FunctionDeclaration findFunctionByName(DecompositionRule rule, String name) {
		FunctionDeclaration res=null;
		for( FunctionDeclaration func : rule.getFunctionDeclarations() ) {
			if(func.getName().equals(name)) {res=func;break;}
		}
		return res;
	}
	private ServiceInstance createServiceInstanceByName(DecompositionRule rule, String name) {
		ServiceInstance res= new ServiceInstance();
		System.out.println(name);
		Service s = this.findByName(name);
		res.setService(s);
		res.setName(s.getName()+"-"+rule.getInstanceCounter());
		rule.incrementInstanceCounter();
		rule.getServiceInstances().add(res);
		return res;
	}
	
	private IdExpression getRuleIdExpression(DecompositionRule rule, String parameterName) {
		IdExpression id = null;
		// the novel version of the method
		id=processIdExpression(rule, parameterName);
		return id;
	}
	
	private IdExpression processIdExpression(DecompositionRule rule,String parameterName) {
		IdExpression id = null;
		// find in existing data
		for(IdExpression el: rule.getData()) {
			if(el.getParameterName().equals(parameterName) && el.getServiceInstance()==rule.getCurrentServiceInstance()) {
				id=el;
				break;
			}
		}
		if(id!=null) {
			return id;
		}
		// we didn't find the idexpression
		// we create a new one
		String[] parameterSplit = parameterName.split("\\[");
		// the id expression is an array
		if (parameterSplit.length>1) {
			ArrayExpression arr = new ArrayExpression();
			String indexName = parameterSplit[1].split("\\]")[0].trim();
			IdExpression index = processIdExpression(rule, indexName);
			arr.setIndex(index);
			arr.setParameterName(parameterSplit[0].trim());
			arr.setServiceInstance(rule.getCurrentServiceInstance());
			rule.getData().add(arr);
			return arr;
		}
		// the id expression is not an array
		else {
			id = new IdExpression();
			id.setParameterName(parameterName);
			id.setServiceInstance(rule.getCurrentServiceInstance());
			id.setArray(this.isArrayIdExpression(parameterName, rule.getCurrentServiceInstance()));
			rule.getData().add(id);
			return id;
		}
	}
	
	private boolean isArrayIdExpression(String parameterName, ServiceInstance si) {
		boolean result = false;
		System.out.println(parameterName);
		Parameter parameter = si.getService().getParameterByName(parameterName);
		if (parameter!=null && parameter.isArray()) {
			return true;
		}
		
		return result;
	}
	
}
