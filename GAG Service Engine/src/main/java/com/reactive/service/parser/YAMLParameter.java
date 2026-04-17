package com.reactive.service.parser;

public class YAMLParameter {
   private String name;
   private String Description;
   private boolean array; 
   private int size;
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public String getDescription() {
	return Description;
}
public void setDescription(String description) {
	Description = description;
}
public boolean isArray() {
	return array;
}
public void setArray(boolean array) {
	this.array = array;
}
public int getSize() {
	return size;
}
public void setSize(int size) {
	this.size = size;
}

   
   
}
