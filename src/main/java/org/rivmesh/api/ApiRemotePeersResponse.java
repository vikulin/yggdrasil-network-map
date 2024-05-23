package org.rivmesh.api;

import java.util.Map;

import org.riv.node.RemotePeerResponse;

public class ApiRemotePeersResponse {
	
	private Map<String, RemotePeerResponse> response;

	public Map<String, RemotePeerResponse> getResponse() {
		return response;
	}

	public void setResponse(Map<String, RemotePeerResponse> response) {
		this.response = response;
	}

}
