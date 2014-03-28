package io.pyd.sdk.client.transport;

import io.pyd.sdk.client.http.CountingMultipartRequestEntity.ProgressListener;
import io.pyd.sdk.client.http.HttpResponseParser;
import io.pyd.sdk.client.http.Requester;

import java.io.File;
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
	
	
	private HttpResponse request(URI uri, Map<String, String> params){
		
		Requester req = new Requester();
		HttpResponse response = null;
		if(!"".equals(user) && !"".equals(password)){
			req.setCredentials(user, password);
		}
		response = req.issueRequest(uri, params);
		return response;
	}



	public HttpResponse getResponse(String action, Map<String, String> params) {
			return request(getActionURI(action), params);
	}



	
	public String getStringContent(String action, Map<String, String> params) {
			HttpResponse response = request(getActionURI(action), params);
			return HttpResponseParser.getString(response);
	}

	
	public Document getXmlContent(String action, Map<String, String> params) {
			HttpResponse response = request(getActionURI(action), params);
			return HttpResponseParser.getXML(response);
	}


	public JSONObject getJsonContent(String action, Map<String, String> params) {
		
			HttpResponse response = request(getActionURI(action), params);
			try {
				return new JSONObject(HttpResponseParser.getString(response));
			} catch (JSONException e) {
			}
			return null;
	}

	
	public InputStream getResponseStream(String action,	Map<String, String> params) {
			HttpResponse response = request(getActionURI(action), params);
			try {
				return response.getEntity().getContent();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
	}


	public Document putContent(String action, Map<String, String> params, File file, String filename, ProgressListener handler) {
		return null;
	}


	public Document putContent(String action, Map<String, String> params, byte[] data, String filename, ProgressListener handler) {
		// TODO Auto-generated method stub
		return null;
	}



	
	
	
	
}
