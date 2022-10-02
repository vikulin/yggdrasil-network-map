package org.rivmesh.api;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;

public class ApiRequest {
	
	private Map<String, Object> map = new TreeMap<String, Object>();
	
	public ApiRequest getPeers(String key){
		map.put("request", "debug_remotegetpeers");
		Map<String, String> arguments = new HashMap<String, String>();
		arguments.put("key", key);
		map.put("arguments", arguments);
		return this;
	}
	
	public ApiRequest getNodeInfo(String key){
		map.put("request", "getnodeinfo");
		Map<String, String> arguments = new HashMap<String, String>();
		arguments.put("key", key);
		map.put("arguments", arguments);
		return this;
	}
	
	public ApiRequest getDHT(String key){
		map.put("request", "debug_remotegetdht");
		Map<String, String> arguments = new HashMap<String, String>();
		arguments.put("key", key);
		map.put("arguments", arguments);
		return this;
	}
	
	public ApiRequest getSelf(String key){
		map.put("request", "debug_remotegetself");
		Map<String, String> arguments = new HashMap<String, String>();
		arguments.put("key", key);
		map.put("arguments", arguments);
		return this;
	}
	
	public String serialize() {
		Gson gson = new Gson();
		return gson.toJson(map);
	}
}
