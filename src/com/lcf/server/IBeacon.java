package com.lcf.server;

import java.net.Socket;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class IBeacon {

	public static Map<String,Map<String,Socket>> majorMap;

	public static void newIBeacon(){
		majorMap = new HashMap<String,Map<String,Socket>>();
	}
	
	public static Boolean addIBeacon(String major, String minor, Socket sock){
		if(!majorMap.containsKey(major)){
			Map<String,Socket> minorMap = new HashMap<String,Socket>();
			majorMap.put(major, minorMap);
		}
		
		Map<String,Socket> minorMap = majorMap.get(major);
		if(minorMap.containsKey(minor)){
			minorMap.remove(minor);
		}
		
		minorMap.put(minor, sock);

		return true;
	}
	
	public static Socket getSock(String major, String minor){	
		return majorMap.get(major).get(minor);
	}
	
}
