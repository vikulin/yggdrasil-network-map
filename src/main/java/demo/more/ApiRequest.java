package demo.more;

import java.util.Map;
import java.util.TreeMap;

import com.google.gson.Gson;

public class ApiRequest {
	
	private Map<String, String> map = new TreeMap<String, String>();
	
	public ApiRequest getDHT(){
		map.put("request", "getDHT");
		return this;
	}
	
	public ApiRequest dhtPing(String box_pub_key, String coords){
		map.put("request", "dhtPing");
		map.put("box_pub_key", box_pub_key);
		map.put("coords", coords);
		return this;
	}
	
	public ApiRequest dhtPing(NodeData nodeData){
		map.put("request", "dhtPing");
		map.put("box_pub_key", nodeData.getBox_pub_key());
		map.put("coords", nodeData.getCoords());
		return this;
	}
	
	public String serialize() {
		Gson gson = new Gson();
		return gson.toJson(map);
	}
}
