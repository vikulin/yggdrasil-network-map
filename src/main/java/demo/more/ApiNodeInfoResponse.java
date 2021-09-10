package demo.more;

import java.util.Map;

public class ApiNodeInfoResponse extends ApiResponse{
	
	private Map<String, Map<String, String>> response;

	public Map<String, Map<String, String>> getResponse() {
		return response;
	}

	public void setResponse(Map<String, Map<String, String>> response) {
		this.response = response;
	}

}
