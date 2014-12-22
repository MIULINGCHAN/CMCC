package com.ggsn.client;

import java.util.ArrayList;

public class OneUserGGSNClient{
	
	public static final void main(String args[]) throws Exception {
		
		ArrayList<UserSimulation> users = new ArrayList<UserSimulation>();
		users.add(new UserSimulation("8613430321124", "广州", 2049));
		users.add(new UserSimulation("8613430321123", "深圳", 1026));
//		for(int i = 0;i<5;i++){
//			users.add(new UserSimulation("86134303"+i+"1125", "珠海", 1000));
//		}
		
		ArrayList<Thread> trds = new ArrayList<Thread>();
		for(int i = 0 ; i < users.size(); i++){
			Thread t = new Thread(users.get(i));
			trds.add(t);
		}
		
		for(int i = 0 ; i < trds.size(); i++){
			trds.get(i).start();
		}
		
	}
	
}