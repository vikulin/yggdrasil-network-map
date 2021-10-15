package org.riv;

public class EdgeData {
	
	public EdgeData(String from, String to) {
		this.from = from;
		this.to = to;
	}
	
	private String from;
	
	private String to;

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}
	
}
