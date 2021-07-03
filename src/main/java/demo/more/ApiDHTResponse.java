package demo.more;

import java.util.List;
import java.util.Map;

public class ApiDHTResponse {
	
	private String status;
	
	private ApiRequest request;
	
	private Map<String, Map<String, List<String>>> response;

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

	public Map<String, Map<String, List<String>>> getResponse() {
		return response;
	}

	public void setResponse(Map<String, Map<String, List<String>>> response) {
		this.response = response;
	}

}
