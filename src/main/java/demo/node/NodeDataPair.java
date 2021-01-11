package demo.node;

import demo.comparator.NodeDataPairSortByCoords;
import demo.more.NodeData;

public class NodeDataPair implements Comparable<NodeDataPair> {
	
	private String ip;
	
	private NodeData nodeData;
	
	public NodeDataPair(String ip, NodeData nodeData) {
		this.ip = ip;
		this.nodeData = nodeData;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public NodeData getNodeData() {
		return nodeData;
	}

	public void setNodeData(NodeData nodeData) {
		this.nodeData = nodeData;
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.nodeData.getCoords()!=null && obj instanceof NodeDataPair && this.nodeData.getCoords().equals(((NodeDataPair)obj).getNodeData().getCoords());
	}
	
	@Override
	public int hashCode() {
		return this.nodeData.getCoords().hashCode();
	}

	@Override
	public int compareTo(NodeDataPair o) {
		if(this.nodeData.getCoords()!=null && o.getNodeData().getCoords()!=null && this.nodeData.getCoords().equals(o.getNodeData().getCoords())) {
			return 0;
		}
		return new NodeDataPairSortByCoords().compare(this, o);
	}
	

}
