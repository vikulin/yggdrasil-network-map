package demo.more;

import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;

public class ApiRequest {
	
	private Map<String, Object> map = new TreeMap<String, Object>();
	
	public ApiRequest getPeers(String key){
		map.put("request", "debug_remotegetpeers");
		map.put("key", key);
		map.put("keepalive", true);
		return this;
	}
	
	public ApiRequest getNodeInfo(String key){
		map.put("request", "getnodeinfo");
		map.put("key", key);
		map.put("keepalive", true);
		return this;
	}
	
	public ApiRequest getDHT(String key){
		map.put("request", "debug_remotegetdht");
		map.put("key", key);
		map.put("keepalive", true);
		return this;
	}
	
	public ApiRequest getSelf(String key){
		map.put("request", "debug_remotegetself");
		map.put("key", key);
		map.put("keepalive", true);
		return this;
	}
	
	public String serialize() {
		Gson gson = new Gson();
		return gson.toJson(map);
	}
}
