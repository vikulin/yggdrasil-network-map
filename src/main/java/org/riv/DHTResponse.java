package org.riv;

import java.util.Map;

import org.riv.node.NodeData;

public class DHTResponse {
	
	private Map<String, NodeData> dht;

	public Map<String, NodeData> getDht() {
		return dht;
	}

	public void setDht(Map<String, NodeData> dht) {
		this.dht = dht;
	}

}
