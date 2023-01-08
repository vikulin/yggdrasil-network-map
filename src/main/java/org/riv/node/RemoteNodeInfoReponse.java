package org.riv.node;

public class RemoteNodeInfoReponse {
	
	private String buildarch;
	
	private String buildname;
	
	private String buildplatform;
	
	private String buildversion;
	
	private String name;
	
	private String icon;

	public String getBuildarch() {
		return buildarch;
	}

	public void setBuildarch(String buildarch) {
		this.buildarch = buildarch;
	}

	public String getBuildname() {
		return buildname;
	}

	public void setBuildname(String buildname) {
		this.buildname = buildname;
	}

	public String getBuildplatform() {
		return buildplatform;
	}

	public void setBuildplatform(String buildplatform) {
		this.buildplatform = buildplatform;
	}

	public String getBuildversion() {
		return buildversion;
	}

	public void setBuildversion(String buildversion) {
		this.buildversion = buildversion;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

}
