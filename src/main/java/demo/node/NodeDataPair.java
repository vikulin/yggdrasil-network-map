package demo.node;

public class NodeDataPair {
	
	private Long id;
	
	private String ip;
	
	private String key;
	
	public NodeDataPair(String ip, String key) {
		this.ip = ip;
		this.key = key;
	}
	
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		return this.key!=null && obj instanceof NodeDataPair && this.key.equals(((NodeDataPair)obj).getKey());
	}
	
	@Override
	public int hashCode() {
		String key = this.key;
		if(key==null) {
			return -1;
		}
		return key.hashCode();
	}

}
