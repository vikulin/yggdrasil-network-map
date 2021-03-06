package demo.more;

public class Link {
	
	public Link(String key, String ip) {
		this.key = key;
		this.ip = ip;
	}
	
	private String key;
	
	private String ip;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.key!=null && this.ip!=null && obj instanceof Link && this.key.equals(((Link)obj).getKey()) && this.ip.equals(((Link)obj).getIp());
	}
	
	@Override
	public int hashCode() {
		String key = this.key+this.ip;
		return key.hashCode();
	}
}
