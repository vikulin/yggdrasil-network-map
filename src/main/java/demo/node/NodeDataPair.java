package demo.node;

public class NodeDataPair {
	
	private Long id;
	private String ip;
	private String key;
	private String icon;
	private String coords;
	private String arch;
	private String version;
	private String platform;
	
	private String name;
	
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
	
	public String getIcon() {
		return icon;
	}
	
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCoords() {
		return coords;
	}

	public void setCoords(String coords) {
		this.coords = coords;
	}

	public String getArch() {
		return arch;
	}

	public void setArch(String arch) {
		this.arch = arch;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
