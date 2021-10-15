package org.riv;

import java.util.Map;

public class DHTResponse {
	
	private Map<String, NodeData> dht;

	public Map<String, NodeData> getDht() {
		return dht;
	}

	public void setDht(Map<String, NodeData> dht) {
		this.dht = dht;
	}

}
