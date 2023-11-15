package com.reactive.service.assesment;

import static com.consulner.app.Configuration.getObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.SortFunc;
import com.reactive.service.model.configuration.Configuration;
import com.reactive.service.model.configuration.Task;
import com.reactive.service.model.specification.GAG;
import com.reactive.service.parser.Parser;
import com.reactive.service.parser.YAMLSpec;
import com.reactive.service.util.Context;
import com.reactive.service.util.Executor;
import com.reactive.service.util.Operation;

public class MainMergeSort {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ArrayList<YAMLSpec> myServices = new ArrayList<>();
		// reading services
		File fileService = new File("spec-merge-sort-enhanced/services.yml");
		try (InputStream inputStream = new FileInputStream(fileService)) {
			// Read the file content using the InputStream
			Yaml yaml = new Yaml(new Constructor(YAMLSpec.class));
			Iterable<Object> specs = yaml.loadAll(inputStream);
			for (Object object : specs) {
				myServices.add((YAMLSpec) object);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// reading rules
		ArrayList<YAMLSpec> myRules = new ArrayList<>();
		File fileRule = new File("spec-merge-sort-enhanced/rules.yml");
		try (InputStream inputStream = new FileInputStream(fileRule)) {
			// Read the file content using the InputStream
			Yaml yaml = new Yaml(new Constructor(YAMLSpec.class));
			Iterable<Object> specs = yaml.loadAll(inputStream);
			for (Object object : specs) {
				myRules.add((YAMLSpec) object);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		ArrayList<YAMLSpec> allSpecs = new ArrayList<YAMLSpec>();
		allSpecs.addAll(myServices);
		allSpecs.addAll(myRules);
		Parser parser = new Parser();
		parser.setSpecs(allSpecs);
		GAG g = parser.getGAG();
		Context nContext = new Context();
		Executor exec = new Executor(nContext, g);
		Configuration conf = new Configuration();
		
		ArrayList<Integer> inputTable = ToolKit.generateArray(SortFunc.ARRAY_SIZE);
		//ArrayList<Integer> inputTable = ToolKit.generateArray(10);
		Hashtable<String,Object> inputs = new Hashtable<>();
		inputs.put("inp_list", inputTable);
		// create the root task
		Task root = Operation.createTask(g.getServices().get(0), inputs);
		conf.setRoot(root);
		exec.setConfiguration(conf);
		exec.execute();
		//Configuration conf = exec.getConfiguration();
		//ObjectMapper objMapper = getObjectMapper();
	}

}
