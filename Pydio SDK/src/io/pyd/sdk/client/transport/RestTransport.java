package io.pyd.sdk.client.transport;

import io.pyd.sdk.client.http.HttpResponseParser;
import io.pyd.sdk.client.http.Requester;
import io.pyd.sdk.client.model.Message;
import io.pyd.sdk.client.transport.Transport;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;


public class RestTransport implements Transport{
	
	public String server_url = "http://pydio/test/";
	public String auth_step = "";
	public String repositoryAlias = "";	
	String user = "root";
	String password ="pydiotest";	
	boolean loginStateChanged = false;
	boolean skipAuth = false;
	
	
	//api/{repositoryAlias}/{actionName}/{parameters}
	
	private URI getActionURI(final String action){
		String url = "api/"+repositoryAlias+"/"+action+"/";
		URI uri = null;
		try{
			uri = new URI(url);
		}catch(Exception e){}
		return uri;
	}
	
	
	private HttpResponse request(URI uri, Map<String, String> params) throws Message{
		
		Requester req = new Requester();
		HttpResponse response = null;
		if(!"".equals(user) && !"".equals(password)){
			req.setCredentials(user, password);
		}
		response = req.issueRequest(uri, params);
		return response;
	}



	public HttpResponse getResponse(String action, Map<String, String> params) {
		try {
			return request(getActionURI(action), params);
		} catch (Message e) {
			e.printStackTrace();
		}
		return null;
	}



	
	public String getStringContent(String action, Map<String, String> params) {
		try {			
			HttpResponse response = request(getActionURI(action), params);
			return HttpResponseParser.getString(response);			
		} catch (Message e) {
			e.printStackTrace();
		}
		
		return null;
	}

	
	public Document getXmlContent(String action, Map<String, String> params) {
		try {
			HttpResponse response = request(getActionURI(action), params);
			return HttpResponseParser.getXML(response);
		} catch (Message e) {
			e.printStackTrace();
		}
		return null;
	}


	public JSONObject getJsonContent(String action, Map<String, String> params) {
		try {
			HttpResponse response = request(getActionURI(action), params);
			return new JSONObject(HttpResponseParser.getString(response));
		} catch (Message e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public InputStream getResponseStream(String action,	Map<String, String> params) {
		try {
			HttpResponse response = request(getActionURI(action), params);
			return response.getEntity().getContent();
		} catch (Message e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	
}
