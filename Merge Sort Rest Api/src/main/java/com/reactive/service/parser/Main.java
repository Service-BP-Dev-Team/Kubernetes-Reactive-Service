package com.reactive.service.parser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.reactive.service.model.specification.GAG;
import com.reactive.service.util.Context;
import com.reactive.service.util.Executor;

public class Main {

	public static void main(String[] args) {
		
	
		        
		        ArrayList<YAMLSpec> myServices= new ArrayList<>(); 
		        //reading services 
		        File fileService = new File("spec/services.yml");
		        try (InputStream inputStream = new FileInputStream(fileService)) {
		            // Read the file content using the InputStream
		        	 Yaml yaml = new Yaml(new Constructor(YAMLSpec.class));
		        	 Iterable<Object> specs = yaml.loadAll(inputStream);
		        	 for (Object object : specs) {
		        		 myServices.add((YAMLSpec) object);
		        	 }
		        	 YAMLSpec spec = myServices.get(2);
		        	 System.out.println(spec.getName());
		        	 System.out.println(spec.getInputs().get(0).getName());
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		        //reading rules
		        ArrayList<YAMLSpec> myRules= new ArrayList<>();
		        File fileRule = new File("spec/rules.yml");
		        try (InputStream inputStream = new FileInputStream(fileRule)) {
		            // Read the file content using the InputStream
		        	 Yaml yaml = new Yaml(new Constructor(YAMLSpec.class));
		        	 Iterable<Object> specs = yaml.loadAll(inputStream);
		        	 for (Object object : specs) {
		        		 myRules.add((YAMLSpec) object);
		        	 }
		        	 YAMLSpec spec = myRules.get(0);
		        	 System.out.println(spec.getName());
		        	 System.out.println(spec.getActions().get(0));
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		        ArrayList<YAMLSpec> allSpecs = new ArrayList<YAMLSpec>();
		        allSpecs.addAll(myServices);
		        allSpecs.addAll(myRules);
		        Parser parser = new Parser();
		        parser.setSpecs(allSpecs);
		        GAG g = parser.getGAG();
		        String print1 = g.getServices().get(2).getRules().get(0).getSemanticAsString();
		        String print2 = g.getServices().get(1).getRules().get(0).getSemanticAsString();
		        System.out.println(print1);
		        System.out.println(print2);
		       // System.out.println(g.getServices().get(2).getRules().get(0).getGuard().getLocation());
		        Context nContext = new Context();
		        Executor exec=new Executor(nContext, g);
		        exec.execute();
		        System.out.println("the size of root tasks is : "+ exec.getConfiguration().getRoot().getSubTasks().size());
		    }
		

}
