package com.example.facedoor.db;

public class User {
	public int id;
	public String name;
	public String groupId;
	
	public User(){
		
	}
	
	public User(int id, String name, String groupId){
		this.id = id;
		this.name = name;
		this.groupId = groupId;
	}
}
