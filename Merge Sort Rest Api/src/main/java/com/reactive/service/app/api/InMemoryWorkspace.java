package com.reactive.service.app.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.consulner.app.Application;
import com.consulner.app.api.mergesort.ArrayInput;
import com.consulner.app.errors.ApplicationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.ObjectMessage;
import com.reactive.service.macro.Macro;
import com.reactive.service.model.configuration.Configuration;
import com.reactive.service.model.configuration.Data;
import com.reactive.service.model.configuration.Task;
import com.reactive.service.model.specification.GAG;
import com.reactive.service.model.specification.Service;
import com.reactive.service.parser.ENV_Manager;
import com.reactive.service.parser.Parser;
import com.reactive.service.parser.YAMLSpec;
import com.reactive.service.util.Context;
import com.reactive.service.util.Executor;
import com.reactive.service.util.Logger;
import com.reactive.service.util.Operation;

import static com.consulner.app.Configuration.getObjectMapper;

public class InMemoryWorkspace {

	public static final ConcurrentHashMap<String, Executor> inMemoryCalls = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<String, Pair<String, Data>> inSubscriptions = new ConcurrentHashMap<String, Pair<String, Data>>();
	public static final ConcurrentHashMap<String, Pair<String, Data>> outSubscriptions = new ConcurrentHashMap<String, Pair<String, Data>>();
	public static final ConcurrentHashMap<Long, Object> threadFunctionProcess = new ConcurrentHashMap<>();
	public static final String defaultGAGFolder = "spec-merge-sort";
	private static GAG gag;

	public static void addCall(ServiceCall sc) {
		ServiceCall local = new ServiceCall();
		Configuration conf = new Configuration();
		conf.setRoot(sc.getTask());
		conf.getRoot().setRemote(false);// transform the task to a local one
		conf.setId(sc.getId());
		// group data. This is necessary to handle array of data
		conf.getRoot().buildGroup();

		// bind to local service
		conf.getRoot().setService(getGag().findByName(sc.getTask().getService().getName()));
		// create and add subscriptions to undefined input
		for (Data din : sc.getTask().getInputs()) {
			if (!din.isDefined()) {
				din.setServiceCallId(local.getId());
				inSubscriptions.put(din.getId(), new Pair<>(sc.getSender(), din));
			}
		}
		// create and add subscriptions to all output
		for (Data dout : sc.getTask().getOutputs()) {

			outSubscriptions.put(dout.getId(), new Pair<>(sc.getSender(), dout));
			dout.setServiceCallId(sc.getId());
		}
		// add the configuration
		Executor exec = new Executor();
		Context ctx = new Context();
		ctx.setExecutor(exec);
		exec.setConfiguration(conf);
		exec.setGag(gag);
		exec.setContext(ctx);
		inMemoryCalls.put(local.getId(), exec);

		// Logger.log(sc); we unlog
		// execute the GAG
		exec.execute();
	}

	public static void processInNotification(Notification nf) {
		Pair<String, Data> p = inSubscriptions.get(nf.getData().getId());
		Data d = p.getValue();
		if (d != null) {
			d.setValue(nf.getData().getValue());
			inSubscriptions.remove(nf.getData().getId());

			Executor executor = inMemoryCalls.get(d.getServiceCallId());
			if (executor != null) {
				// System.out.println("find the executor");
				executor.execute();
			}
		}
	}

	public static void processOutNotification(Notification nf, String host) {
		// Pair<String,Data> p =outSubscriptions.get(nf.getData().getId());
		// String host= p.getKey();
		Message m = new Message();
		m.setType(Message.NOTIFICATION_MESSAGE_TYPE);
		m.setNotification(nf);
		nf.setReceiver(host);
		nf.setSender(getHostIp());
		outSubscriptions.remove(nf.getData().getId());
		Message.sendMessage(m);
	}

	public static void processOutServiceCall(ServiceCall sc, Executor exec) {
		// configure the task of add service call
		inMemoryCalls.put(sc.getId(), exec);
		// set the task to remote
		sc.getTask().setRemote(true);
		// set the service call id on data
		// and the host to send the data
		String receiver = bind(sc.getTask().getService().getKubename());
		// Logger.log(receiver);
		for (Data d : sc.getTask().getInputs()) {
			d.setServiceCallId(sc.getId());
			d.setHost(receiver);
			// create subscriptions for undefined data
			if (!d.isDefined()) {
				outSubscriptions.put(d.getId(), new Pair<String, Data>(d.getHost(), d));
			}
		}
		for (Data d : sc.getTask().getOutputs()) {
			d.setServiceCallId(sc.getId());
			// register in subscriptions
			inSubscriptions.put(d.getId(), new Pair<String, Data>(d.getHost(), d));
		}
		// send the message
		Message m = new Message();
		m.setType(Message.SERVICECALL_MESSAGE_TYPE);
		m.setServiceCall(sc);
		sc.setSender(getLocalHostIp()); // very important to get the ip local
		sc.setReceiver(receiver);
		Message.sendMessage(m);

	}

	public static String bind(String kubename) {
		String result = null;
		Message m = new Message();
		m.setType(Message.BIND_MESSAGE_TYPE);
		Bind bind = new Bind();
		bind.setReceiver(kubename);
		bind.setSender(getHostIp());
		m.setBind(bind);
		ObjectMapper objMapper = getObjectMapper();
		try {
			BindResponse bindResponse = objMapper.readValue(Message.sendMessage(m), BindResponse.class);
			result = bindResponse.getIpAddress();
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static String processBindMessage(Bind bind) {
		return getHostIp();
	}

	public static Configuration getCall(String id) {
		return inMemoryCalls.get(id).getConfiguration();
	}

	public static String getHostIp() {
		String result = "";
		try {
			InetAddress ipAddr = InetAddress.getLocalHost();
			result = ipAddr.getHostAddress();
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		}
		return result + ":8000";
	}

	public static GAG getGag() {
		if (gag == null) {
			if (defaultGAGFolder != null) {
				getGagWithRootFolder(defaultGAGFolder);
			} else {
				getGagWithRootFolder("spec");

			}
		}
		return gag;
	}

	public static GAG getGagWithRootFolder(String rootFolder) {
		if (gag == null) {
			// fetch the GAG
			ArrayList<YAMLSpec> myServices = new ArrayList<>();
			// reading services
			// we replace env file in services
			String serviceWithEnvVariable;
			try {
				serviceWithEnvVariable = ENV_Manager.replaceEnvVariables(rootFolder + "/services.yml",
						rootFolder + "/.env");
				System.out.println(serviceWithEnvVariable);
				Yaml yamlService = new Yaml(new Constructor(YAMLSpec.class));
				Iterable<Object> specsService = yamlService.loadAll(serviceWithEnvVariable);
				for (Object object : specsService) {
					myServices.add((YAMLSpec) object);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// reading rules
			ArrayList<YAMLSpec> myRules = new ArrayList<>();
			String allRulesWithEnvVariable;
			try {
				allRulesWithEnvVariable = ENV_Manager.replaceEnvVariables(rootFolder + "/rules.yml",
						rootFolder + "/.env");

				String rulesExpanded = Macro.expandAllMacro(allRulesWithEnvVariable);
				System.out.println(rulesExpanded);
				Yaml yamlRule = new Yaml(new Constructor(YAMLSpec.class));
				Iterable<Object> specsRule = yamlRule.loadAll(rulesExpanded);
				for (Object object : specsRule) {
					myRules.add((YAMLSpec) object);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			ArrayList<YAMLSpec> allSpecs = new ArrayList<YAMLSpec>();
			allSpecs.addAll(myServices);
			allSpecs.addAll(myRules);
			Parser parser = new Parser();
			parser.setSpecs(allSpecs);
			gag = parser.getGAG();
			// System.out.println(gag);
		}
		return gag;
	}

	public static void setGag(GAG gag) {
		InMemoryWorkspace.gag = gag;
	}

	public static void main(String args[]) {
		mainMergeSort();
	}

	public static void mainMergeSort() {

		// create the server
		try {
			Application.main(new String[] {});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// execute the gag
		GAG g = getGagWithRootFolder("spec-merge-sort");
		// System.out.println(g);

		Executor defaulte = new Executor();
		defaulte.setGag(g);
		Context ctx = new Context();
		ctx.setExecutor(defaulte);
		defaulte.setContext(ctx);
		Hashtable<String, Object> inputs = new Hashtable<String, Object>();
		ArrayInput arr = ArrayInput.createRandomInput(13);
		ObjectMessage obj = new ObjectMessage();
		obj.setTable(arr.getArray());
		inputs.put("a", obj);
		Task t = Operation.createTask(g.getServices().get(0), inputs);
		Configuration conf = new Configuration();
		conf.setRoot(t);
		defaulte.setConfiguration(conf);
		defaulte.execute();
	}

	public static void mainSimpleTest() {
		// System.out.println(getHostIp());
		ServiceCall sc = new ServiceCall();
		Task t = new Task();
		t.getInputs().add(new Data());
		t.setService(new Service());

		try {
			// create the server
			Application.main(new String[] {});
			// execute the gag
			GAG g = getGag();
			// System.out.println(g);
			String print1 = g.getServices().get(2).getRules().get(0).getSemanticAsString();
			// System.out.println(print1);
			Executor defaulte = new Executor();
			defaulte.setGag(g);
			Context ctx = new Context();
			ctx.setExecutor(defaulte);
			defaulte.setContext(ctx);
			defaulte.execute();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getLocalHostIp() {
		String result = bind("localhost:8000");
		// Logger.log(result);
		return result;
	}
}
