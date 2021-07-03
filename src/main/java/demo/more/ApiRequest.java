package demo.more;

import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;

public class ApiRequest {
	
	private Map<String, String> map = new TreeMap<String, String>();
	
	public ApiRequest getPeers(String key){
		map.put("request", "debug_remotegetpeers");
		map.put("key", key);
		return this;
	}
	
	public ApiRequest getDHT(String key){
		map.put("request", "debug_remotegetdht");
		map.put("key", key);
		return this;
	}
	
	public String serialize() {
		Gson gson = new Gson();
		return gson.toJson(map);
	}
}
