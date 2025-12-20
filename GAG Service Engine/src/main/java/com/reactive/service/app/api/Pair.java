package com.reactive.service.app.api;

public class Pair<K,V> {
  private K key;
  private V value;
  
  public Pair() {
	  
  }
  public Pair(K key, V value){
	  this.key=key;
	  this.value=value;
  }
public K getKey() {
	return key;
}
public void setKey(K key) {
	this.key = key;
}
public V getValue() {
	return value;
}
public void setValue(V value) {
	this.value = value;
}

public K getFirst() {
	return key;
}
public void setFirst(K key) {
	this.key = key;
}
public V getSecond() {
	return value;
}
public void setSecond(V value) {
	this.value = value;
}
  
}
