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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.consulner.app.Application;
import com.consulner.app.api.mergesort.ArrayInput;
import com.consulner.app.errors.ApplicationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	//public static final ConcurrentHashMap<String, Pair<String, Data>> outSubscriptions = new ConcurrentHashMap<String, Pair<String, Data>>();
	//map service call to defined data
	
	public static final ConcurrentHashMap<Long, Object> threadFunctionProcess = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<String, Boolean> discardNotificationsAlreadyDone = new ConcurrentHashMap<>();
	public static final String defaultGAGFolder = "spec-merge-sort-enhanced";
	public static final Map<String, String> environmentVariables = new HashMap<String, String>();
	public static final String KEY_READY_TASK_WAIT_TIME = "READY_TASK_WAIT_TIME"; // time where each gag process will
																					// wait before
	// checking if they are new rules that can be applied
	// to pending service (open tasks)
	// this time can be modified by the .env file of the yaml spec.
	public static final int VALUE_READY_TASK_WAIT_TIME = 5;
	// this represent the default value of the precedent key;
	public static final String KEY_SYNC_IN_NOTIFICATION_TIME = "SYNC_IN_NOTIFICATION_TIME"; // sometimes it may happen
																							// that a notification
																							// happens,
	// without having being registered. More precisely a configuration receives a
	// notification of a data she is not yet aware of.
	// This is link to the high level of concurrency : a notification of a input
	// task data is processed
	// before the service call linked to the task is processed. In this case, the
	// notification process has to wait
	// for the linked service call (i.e the service call containing the task that
	// bears the data) to be processed first.
	// this time will defined the time that the notification process, will be asked
	// to wait, in order for the service call
	// to be processed first.
	// this time can be modified by the .env file of the yaml spec.
	public static final int VALUE_IN_NOTIFICATION_TIME = 3;
	// this represent the default value of the precedent key;
	
	public  static final String KEY_TCP_ON_LOCALHOST="DO_TCP_FOR_CALL_TO_LOCALHOST";
	//the above key is used to check if we have use tcp on the localhost when calling
	// a remote service, if set to false the execution is enhanced
	// since tcp communication are avoided when a component sen a message to itself
	public static final int VALUE_TCP_ON_LOCALHOST=0;
	// by default we force to have tcp on localhost
	
	//the key bellow is use to know the threshold for compression,
	// when the data to send is too big we compress the data (service call, notification, bind, etc) using
	// the gzip algorithn before sending it to the network;
	public  static final String KEY_NETWORK_DATA_COMPRESSION_THRESHOLD="NETWORK_DATA_COMPRESSION_THRESHOLD";
	
	// the value of the key above by default
	public  static final int VALUE_NETWORK_DATA_COMPRESSION_THRESHOLD=1000;
	
	
	private static GAG gag;
	
	private static String localHostIP;

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
		//synchronized (inSubscriptions) {

			for (Data din : sc.getTask().getInputs()) {
				if (!din.isDefined()) {
					din.setServiceCallId(local.getId());
					inSubscriptions.put(din.getId(), new Pair<>(sc.getSender(), din));
				}
			}

			Executor exec = new Executor();
			for (Data dout : sc.getTask().getOutputs()) {

				exec.getOutSubscriptions().put(dout.getId(), new Pair<>(sc.getSender(), dout));
				dout.setServiceCallId(sc.getId());
			}

		//}

		// add the configuration
		Context ctx = new Context();
		ctx.setExecutor(exec);
		exec.setConfiguration(conf);
		exec.setGag(gag);
		exec.setContext(ctx);
		inMemoryCalls.put(local.getId(), exec);
		exec.setServiceCallId(local.getId());// we register the service call id
		// so that we can cleared them later
		// Logger.log(sc); we unlog
		// execute the GAG
		exec.execute();
	}

	public static void processInNotification(Notification nf) {

		Runnable runner = () -> {
			Pair<String, Data> p = null;
			Boolean alreadyNotified = false;
			while (p == null) {
				p = inSubscriptions.get(nf.getData().getId());
				alreadyNotified = discardNotificationsAlreadyDone.get(nf.getData().getId());
				if (alreadyNotified != null && alreadyNotified) {
					System.out.println("already notified");
					break;
				}else {
					alreadyNotified=false;
				}
				if (p == null) {
					//System.out.println(nf.getData().getValue() + " " + nf.getData().getId());
				} else {
					//System.out.println("p is no more null");
				}
				try {
					Thread.sleep(getSyncInNotificationTime());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (!alreadyNotified) {
				Data d = p.getValue();
				if (d != null) {
					d.setValue(nf.getData().getValue());
					inSubscriptions.remove(nf.getData().getId());
					
					discardNotificationsAlreadyDone.put(nf.getData().getId(), true);

					Executor executor = inMemoryCalls.get(d.getServiceCallId());
					if (executor != null) {
						// System.out.println("find the executor");
						// we do not launch the executor anymore
						// we use now a continous execute function
						// executor.execute();
					}
				}
			}

		};
		Thread.startVirtualThread(runner);
	}

	public static void processOutNotification(Notification nf, String host) {
		// Pair<String,Data> p =outSubscriptions.get(nf.getData().getId());
		// String host= p.getKey();
		Message m = new Message();
		m.setType(Message.NOTIFICATION_MESSAGE_TYPE);
		m.setNotification(nf);
		nf.setReceiver(host);
		nf.setSender(getHostIp());
		//synchronized (outSubscriptions) {
		//	outSubscriptions.remove(nf.getData().getId());
		//}
		if (host.equals(getLocalHostIp()) && !isForcedTCPOnLocalhost()) {
			// we do not perform the call through tcp when it is localhost
			// we clone the notification
			String nfText;
			try {
				nfText = getObjectMapper().writeValueAsString(nf);
				Notification nfCopy = getObjectMapper().readValue(nfText,Notification.class); 
				processInNotification(nfCopy);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
		}
		else { Message.sendMessage(m);}
	}

	public static void processOutServiceCall(ServiceCall sc,String receiver, Executor exec) {
		// configure the task of add service call
		inMemoryCalls.put(sc.getId(), exec);
		// set the task to remote
		sc.getTask().setRemote(true);
		// set the service call id on data
		// and the host to send the data
		//String receiver = bind(sc.getTask().getService().getKubename());
		// Logger.log(receiver);
		//synchronized (outSubscriptions) {
			for (Data d : sc.getTask().getInputs()) {
				d.setServiceCallId(sc.getId());
				d.setHost(receiver);
				// create subscriptions for undefined data
				if (!d.isDefined()) {
					exec.getOutSubscriptions().put(d.getId(), new Pair<String, Data>(d.getHost(), d));
				}
			}
		//}
		//synchronized (inSubscriptions) {
			for (Data d : sc.getTask().getOutputs()) {
				d.setServiceCallId(sc.getId());
				// register in subscriptions
				inSubscriptions.put(d.getId(), new Pair<String, Data>(d.getHost(), d));
			}
		//}

		// send the message
		Message m = new Message();
		m.setType(Message.SERVICECALL_MESSAGE_TYPE);
		m.setServiceCall(sc);
		sc.setSender(getLocalHostIp()); // very important to get the ip local
		sc.setReceiver(receiver);
		/*
		if (receiver.equals(getLocalHostIp()) && !isForcedTCPOnLocalhost()) {
			// we do not perform the call through tcp when it is localhost
			// clone the service call through serialization
			try {
				String scValueString = getObjectMapper().writeValueAsString(sc);
				ServiceCall scCopy = getObjectMapper().readValue(scValueString, ServiceCall.class);
				// unclone the service call
				System.out.println("local call");
				addCall(scCopy);
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
		
		//else { 
			Message.sendMessage(m);
		//	}

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
			Map<String, String> EnvVariables = null;
			try {
				EnvVariables = ENV_Manager.readEnvFile(rootFolder + "/.env");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				String serviceYamlString = ENV_Manager.readFileToString(rootFolder + "/services.yml");
				serviceWithEnvVariable = ENV_Manager.replaceEnvVariablesInYaml(serviceYamlString, EnvVariables);
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
				String allRulesYamlSpecString = ENV_Manager.readFileToString(rootFolder + "/rules.yml");
				allRulesWithEnvVariable = ENV_Manager.replaceEnvVariablesInYaml(allRulesYamlSpecString, EnvVariables);

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
			// all all the environmet variables in the workspace memory
			for (String key : EnvVariables.keySet()) {
				environmentVariables.put(key, EnvVariables.get(key));
			}
			// System.out.println(gag);
		}
		return gag;
	}

	public static void setGag(GAG gag) {
		InMemoryWorkspace.gag = gag;
	}



	public static String getLocalHostIp() {
		if (localHostIP !=null) {
			return localHostIP;
		}
		String result = bind("localhost:8000");
		localHostIP = result;
		// Logger.log(result);
		return result;
	}

	public static int getReadyTaskWaitTime() {
		String val = environmentVariables.get(KEY_READY_TASK_WAIT_TIME);
		if (val != null) {
			return Integer.parseInt(val);
		}
		return VALUE_READY_TASK_WAIT_TIME;
	}

	public static int getSyncInNotificationTime() {
		String val = environmentVariables.get(KEY_SYNC_IN_NOTIFICATION_TIME);
		if (val != null) {
			return Integer.parseInt(val);
		} else {
			return VALUE_IN_NOTIFICATION_TIME;
		}
	}
	
	public static Boolean isForcedTCPOnLocalhost() {
		String val = environmentVariables.get(KEY_TCP_ON_LOCALHOST);
		if (val != null) {
			return val.equals("True");
		} else {
			return VALUE_TCP_ON_LOCALHOST==1;
		}
	}
	
	public static int getNetworkDataCompressionThreshold() {
		String val = environmentVariables.get(KEY_NETWORK_DATA_COMPRESSION_THRESHOLD);
		if (val != null) {
			return Integer.parseInt(val);
		} else {
			return VALUE_NETWORK_DATA_COMPRESSION_THRESHOLD;
		}
	}

	public static String getEnvironmentValue(String key) {
		return environmentVariables.get(key);
	}
}