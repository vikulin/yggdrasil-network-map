package org.riv.node;

public class NodeData {
	
	public NodeData() {
		
	}
	
	public NodeData(String box_pub_key, String coords) {
		this.box_pub_key = box_pub_key;
		this.coords = coords;
	}
	
	private String box_pub_key;
	
	private String coords;
	
	public String getBox_pub_key() {
		return box_pub_key;
	}

	public String getCoords() {
		return coords;
	}

	public void setBox_pub_key(String box_pub_key) {
		this.box_pub_key = box_pub_key;
	}

	public void setCoords(String coords) {
		this.coords = coords;
	}

}
