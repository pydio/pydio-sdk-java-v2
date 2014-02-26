package io.pyd.sdk.client.transport;

import java.io.InputStream;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.json.JSONObject;
import org.w3c.dom.Document;

public class RestTransport implements Transport{

	public HttpResponse getResponse(String action, Map<String, String> params) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getStringContent(String action, Map<String, String> params) {
		// TODO Auto-generated method stub
		return null;
	}

	public Document getXmlContent(String action, Map<String, String> params) {
		// TODO Auto-generated method stub
		return null;
	}

	public JSONObject getJsonContent(String action, Map<String, String> params) {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream getResponseStream(String action, Map<String, String> params) {
		// TODO Auto-generated method stub
		return null;
	}

}
