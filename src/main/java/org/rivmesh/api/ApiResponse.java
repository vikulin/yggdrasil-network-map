package org.rivmesh.api;

public class ApiResponse {
	
	private String status;
	
	private String error;
	
	private ApiRequest request;
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public ApiRequest getRequest() {
		return request;
	}

	public void setRequest(ApiRequest request) {
		this.request = request;
	}

}
