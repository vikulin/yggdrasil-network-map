package demo.more;

import java.util.Map;

public class ApiNodeInfoResponse extends ApiResponse{
	
	private Map<String, Map<String, Object>> response;

	public Map<String, Map<String, Object>> getResponse() {
		return response;
	}

	public void setResponse(Map<String, Map<String, Object>> response) {
		this.response = response;
	}

}
