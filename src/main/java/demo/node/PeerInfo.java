package demo.node;

public class PeerInfo {
	
	public PeerInfo(boolean up, String address, String box_pub_key, long last_seen) {
		this.up = up;
		this.address = address;
		this.box_pub_key = box_pub_key;
		this.last_seen = last_seen;
	}

	private boolean up;
	private String address;
	private String box_pub_key;
	private long last_seen;
	
	public boolean isUp() {
		return up;
	}
	public void setUp(boolean up) {
		this.up = up;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getBox_pub_key() {
		return box_pub_key;
	}
	public void setBox_pub_key(String box_pub_key) {
		this.box_pub_key = box_pub_key;
	}
	public long getLast_seen() {
		return last_seen;
	}
	public void setLast_seen(long last_seen) {
		this.last_seen = last_seen;
	}

}
