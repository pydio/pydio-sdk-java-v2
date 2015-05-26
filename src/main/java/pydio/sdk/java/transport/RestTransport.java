package pydio.sdk.java.transport;


import org.apache.http.HttpResponse;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.ParseException;
import java.util.Map;

import pydio.sdk.java.auth.AuthenticationHelper;
import pydio.sdk.java.http.HttpResponseParser;
import pydio.sdk.java.model.ServerNode;
import pydio.sdk.java.utils.ProgressListener;

/**
 * Class that generates rest requests
 * @author pydio
 *
 */
public class RestTransport implements Transport{
	
	public String server_url = "";
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
        return null;
	}
    @Override
    public int requestStatus() {
        return 0;
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
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e){
                e.printStackTrace();
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
    @Override
    public void setServer(ServerNode server) {

    }
    @Override
    public String secureToken() {
        return "";
    }
    @Override
    public void setAuthenticationHelper(AuthenticationHelper helper) {

    }
}
