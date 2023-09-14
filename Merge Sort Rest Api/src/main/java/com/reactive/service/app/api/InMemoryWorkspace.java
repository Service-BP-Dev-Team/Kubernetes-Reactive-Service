package com.reactive.service.app.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactive.service.model.configuration.Configuration;
import com.reactive.service.model.configuration.Data;
import com.reactive.service.model.specification.GAG;
import com.reactive.service.parser.Parser;
import com.reactive.service.parser.YAMLSpec;
import com.reactive.service.util.Context;
import com.reactive.service.util.Executor;

import static com.consulner.app.Configuration.getObjectMapper;
public class InMemoryWorkspace {

	public static final ConcurrentHashMap<String, Executor> inMemoryCalls = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<String, Pair<String,Data>> inSubscriptions = new ConcurrentHashMap<String, Pair<String,Data>>();
	public static final ConcurrentHashMap<String, Pair<String,Data>> outSubscriptions = new ConcurrentHashMap<String, Pair<String,Data>>();
	private static GAG gag;
	public static void addCall(ServiceCall sc) {
		Configuration conf = new Configuration();
		conf.setRoot(sc.getTask());
		conf.getRoot().setRemote(false);// transform the task to a local one
		conf.setId(sc.getId());
		//create and add subscriptions to undefined input
		for(Data din : sc.getTask().getInputs()) {
			if(!din.isDefined()) {
			din.setServiceCallId(sc.getId());
			inSubscriptions.put(din.getId(), new Pair<>(sc.getSender(),din));
			}
		}
		//create and add subscriptions to all output
		for(Data dout : sc.getTask().getOutputs()) {
			
			outSubscriptions.put(dout.getId(), new Pair<>(sc.getSender(),dout));
			dout.setServiceCallId(sc.getId());
		}
		//add the configuration
		Executor exec = new Executor();
		Context ctx = new Context();
		exec.setConfiguration(conf);
		exec.setGag(gag);
		exec.setContext(ctx);
		inMemoryCalls.put(sc.getId(), exec);
	}
	
	public static void processInNotification(Notification nf) {
		Pair<String,Data> p =inSubscriptions.get(nf.getData().getId());
		Data d= p.getValue();
		if(d!=null) {
			d.setValue(nf.getData().getValue());
			inSubscriptions.remove(nf.getGlobalId());
			Executor executor = inMemoryCalls.get(d.getServiceCallId());
			if(executor!=null) {
				executor.computePendingLocalComputations();
			}
		}
	}
	
	public static void processOutNotification(Notification nf) {
		Pair<String,Data> p =outSubscriptions.get(nf.getData().getId());
		String host= p.getKey();
		Message m = new Message();
		m.setType(Message.NOTIFICATION_MESSAGE_TYPE);
		m.setNotification(nf);
		nf.setReceiver(host);
		nf.setSender(getHostIp());
		Message.sendMessage(m);
	}
	
	public static void processOutServiceCall(ServiceCall sc, Executor exec) {
		//configure the task  of add service call 
		inMemoryCalls.put(sc.getId(), exec);
		// set the task to remote
		sc.getTask().setRemote(true);
		// set the service call id on data
		// and the host to send the data
		String receiver = bind(sc.getTask().getService().getKubename());
		for(Data d: sc.getTask().getInputs()) {
			d.setServiceCallId(sc.getId());
			d.setHost(receiver); 
		}
		for(Data d: sc.getTask().getOutputs()) {
			d.setServiceCallId(sc.getId());
			
		}
		// send the message
		Message m= new Message();
		m.setType(Message.SERVICECALL_MESSAGE_TYPE);
		m.setServiceCall(sc);
		sc.setSender(getHostIp());
		sc.setReceiver(receiver);
	
		
	}
	
	public static String bind(String kubename) {
		String result=null;
		Message m= new Message();
		m.setType(Message.BIND_MESSAGE_TYPE);
		Bind bind=new Bind();
		bind.setReceiver(kubename);
		m.setBind(bind);
		ObjectMapper objMapper = getObjectMapper();
		try {
			BindResponse bindResponse= objMapper.readValue(Message.sendMessage(m), BindResponse.class);
			result=bindResponse.getIpAddress();
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
		String result="";
		try {
            InetAddress ipAddr = InetAddress.getLocalHost();
            result=ipAddr.getHostAddress();
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
		return result;
	}
	
	public static void main(String args[]) {
		System.out.println(getHostIp());
	}

	public static GAG getGag() {
		if(gag==null) {
			//fetch the GAG  
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
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        ArrayList<YAMLSpec> allSpecs = new ArrayList<YAMLSpec>();
	        allSpecs.addAll(myServices);
	        allSpecs.addAll(myRules);
	        Parser parser = new Parser();
	        parser.setSpecs(allSpecs);
	        gag = parser.getGAG();
		}
		return gag;
	}

	public static void setGag(GAG gag) {
		InMemoryWorkspace.gag = gag;
	}
	
	
}
