package org.rivmesh.api;

import java.util.List;
import java.util.Map;

public class ApiDHTResponse extends ApiResponse {
	
	private Map<String, Map<String, List<String>>> response;

	public Map<String, Map<String, List<String>>> getResponse() {
		return response;
	}

	public void setResponse(Map<String, Map<String, List<String>>> response) {
		this.response = response;
	}

}
