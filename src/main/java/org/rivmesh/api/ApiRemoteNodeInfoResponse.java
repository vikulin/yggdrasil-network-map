package org.rivmesh.api;

import java.util.Map;

import org.riv.node.RemoteNodeInfoReponse;

public class ApiRemoteNodeInfoResponse {
	
	private Map<String, Map<String, RemoteNodeInfoReponse>> response;

	public Map<String, Map<String, RemoteNodeInfoReponse>> getResponse() {
		return response;
	}

	public void setResponse(Map<String, Map<String, RemoteNodeInfoReponse>> response) {
		this.response = response;
	}

}
