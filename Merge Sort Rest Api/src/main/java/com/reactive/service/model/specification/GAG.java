package com.reactive.service.model.specification;

import java.util.ArrayList;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
@XmlRootElement(name="GAG")
public class GAG {

	private String name;
	private ArrayList<Service> services;
	
	public GAG() {
		services = new ArrayList<Service>();
	}
	@XmlAttribute
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlElement(name="service")
	public ArrayList<Service> getServices() {
		return services;
	}
	public void setServices(ArrayList<Service> services) {
		this.services = services;
	}
	
	public Service findByName(String serviceName) {
		Service result=null;
		for(Service s: services) {
			if(s.getName().equals(serviceName)) {
				return s;
			}
		}
		return result;
	}
	
	
}
