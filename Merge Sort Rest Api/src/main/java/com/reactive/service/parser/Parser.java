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
		for(YAMLParameter param : S.getInputs()) {
			Parameter par = new Parameter();
			par.setName(param.getName().trim());
			par.setService(s);
			s.getInputParameters().add(par);
		}
		for(YAMLParameter param : S.getOutputs()) {
			Parameter par = new Parameter();
			par.setName(param.getName().trim());
			par.setService(s);
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
		String computationSymbol=computationPart[0].trim();
		FunctionDeclaration f = findFunctionByName(rule, computationSymbol);
			if(f!=null) {
				processFunctionAction(left,right, f, rule);
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
		for(String rv : rvariables) {
			IdExpression idl = new IdExpression();
			idl.setServiceInstance(si);
			idl.setParameterName(si.getService().getInputParameters().get(inputCounter).getName());
			rule.getData().add(idl);
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
			idr.setParameterName(si.getService().getOutputParameters().get(outputCounter).getName());
			rule.getData().add(idr);
			outputCounter++;
			
			IdExpression idl = getRuleIdExpression(rule, lv.trim());
			Equation eq = new Equation();
			eq.setLeftpart(idl);
			eq.setRightpart(idr);
			rule.getSemantics().add(eq);
		}
	}
	private void processFunctionAction(String left, String right, FunctionDeclaration f, DecompositionRule rule) {
		IdExpression idl = getRuleIdExpression(rule, left);
		
		String cut = right.split("\\(")[1];
		cut=cut.substring(0,cut.length()-1);
		String[] variables = cut.split(","); 
		FunctionExpression fe = new FunctionExpression();
		fe.setFunction(f);
		for(String v : variables) {
			IdExpression idr=getRuleIdExpression(rule, v.trim());
			fe.getIdExpressions().add(idr);
		}
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
			guard.setLocation(R.getGuard().getClassPath());
			guard.setMethod(R.getGuard().getMethod());
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
		Service s = this.findByName(name);
		res.setService(s);
		res.setName(s.getName()+"-"+rule.getInstanceCounter());
		rule.incrementInstanceCounter();
		rule.getServiceInstances().add(res);
		return res;
	}
	
	private IdExpression getRuleIdExpression(DecompositionRule rule, String parameterName) {
		IdExpression id = null;
		for(IdExpression el: rule.getData()) {
			if(el.getParameterName().equals(parameterName) && el.getServiceInstance()==rule.getCurrentServiceInstance()) {
				id=el;
				break;
			}
		}
		if(id==null) {
			id= new IdExpression();
			id.setParameterName(parameterName);
			id.setServiceInstance(rule.getCurrentServiceInstance());
			rule.getData().add(id);
		}
		return id;
	}
	//private IdExpression =
	
}
