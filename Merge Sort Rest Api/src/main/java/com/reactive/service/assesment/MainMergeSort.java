package com.reactive.service.assesment;

import static com.consulner.app.Configuration.getObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.consulner.app.Application;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.SortFunc;
import com.reactive.service.app.api.InMemoryWorkspace;
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
		// create the server
				try {
					Application.main(new String[] {});
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// execute the gag
				GAG g = InMemoryWorkspace.getGagWithRootFolder("spec-merge-sort-enhanced");
				// System.out.println(g);

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
		long start = System.currentTimeMillis();
		exec.execute();
		long end = System.currentTimeMillis();
		long finish = end-start;
		System.out.println("finish of the time with service: "+ finish);
		
		start= System.currentTimeMillis();
		Collections.sort(inputTable);
		end = System.currentTimeMillis();
		finish=end-start;
		System.out.println("finish of the time without service: "+ finish);
		
		//Configuration conf = exec.getConfiguration();
		//ObjectMapper objMapper = getObjectMapper();
	}

}
