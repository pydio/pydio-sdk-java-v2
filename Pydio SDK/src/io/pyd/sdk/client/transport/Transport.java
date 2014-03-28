package io.pyd.sdk.client.transport;
import io.pyd.sdk.client.http.CountingMultipartRequestEntity;
import io.pyd.sdk.client.model.Message;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.json.JSONObject;
import org.w3c.dom.Document;

public interface Transport{
	
	public static int MODE_SESSION = 1;
	public static int MODE_RESTFUL = 2;
	public static int MODE_MOCK = 3;	
		
	public HttpResponse getResponse(String action, Map<String, String> params);
	public String getStringContent(String action, Map<String, String> params);
	public Document getXmlContent(String action, Map<String, String> params);
	public JSONObject getJsonContent(String action, Map<String, String> params);
	public Document putContent(String action, Map<String, String> params, File file, String filename, CountingMultipartRequestEntity.ProgressListener handler);
	public Document putContent(String action, Map<String, String> params, byte[] data, String filename, CountingMultipartRequestEntity.ProgressListener handler);
	
}