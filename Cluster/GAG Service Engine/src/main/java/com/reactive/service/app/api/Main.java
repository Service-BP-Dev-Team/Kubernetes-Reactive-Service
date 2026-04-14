package com.reactive.service.app.api;

import java.io.IOException;

import com.consulner.app.Configuration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactive.service.model.configuration.Data;

public class Main {

	
	public static void main (String args[]) {
		Data d = new Data();
		Data d3 = new Data();
		d3.setValue("hello d3" );
		//d.setValue(new int[]{3,4});
		d.setValue(d3);
		ObjectMapper objmapper=Configuration.getObjectMapper();
		try {
			String stringJson = objmapper.writeValueAsString(d);
			System.out.println(stringJson);
			Data d1 = objmapper.readValue(stringJson, Data.class);
			System.out.println(d1.getValue().getClass());
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
