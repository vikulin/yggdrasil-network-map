package demo.node;

import demo.more.NodeData;

public class NodeDataPair {
	
	private Long id;
	
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
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		return this.nodeData.getBox_pub_key()!=null && obj instanceof NodeDataPair && this.nodeData.getBox_pub_key().equals(((NodeDataPair)obj).getNodeData().getBox_pub_key());
	}
	
	@Override
	public int hashCode() {
		String pubKey = this.nodeData.getBox_pub_key();
		if(pubKey==null) {
			return -1;
		}
		return pubKey.hashCode();
	}

}
