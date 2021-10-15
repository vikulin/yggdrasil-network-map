package org.rivmesh.api;

public class ApiResponse {
	
	private String status;
	
	private ApiRequest request;
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public ApiRequest getRequest() {
		return request;
	}

	public void setRequest(ApiRequest request) {
		this.request = request;
	}

}
