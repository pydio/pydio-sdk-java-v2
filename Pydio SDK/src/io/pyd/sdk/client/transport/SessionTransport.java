
package io.pyd.sdk.client.transport;

import io.pyd.sdk.client.auth.AuthenticationUtils;
import io.pyd.sdk.client.auth.CredentialsProvider;
import io.pyd.sdk.client.http.CountingMultipartRequestEntity;
import io.pyd.sdk.client.http.HttpResponseParser;
import io.pyd.sdk.client.http.Requester;
import io.pyd.sdk.client.http.XMLDocEntity;
import io.pyd.sdk.client.model.Message;
import io.pyd.sdk.client.model.ServerNode;
import io.pyd.sdk.client.utils.Pydio;
import io.pyd.sdk.client.utils.StateHolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * 
 * @author pydio
 *
 */
public class SessionTransport implements Transport{
	
	
	public String index = "index.php?";
	public String secure_token = "";
	public String auth_step = "";
	public String repositoryId = "";	
	
	ServerNode server;
	String user = "";
	String password ="";
	private CredentialsProvider provider;
			
	boolean loginStateChanged = false;
	boolean skipAuth = false;
	
	
	public SessionTransport(){
		server = StateHolder.getInstance().getServer();
	}
	
	
	public SessionTransport(CredentialsProvider p){
		server = StateHolder.getInstance().getServer();
		provider = p;
	}
	
	
	public SessionTransport(ServerNode node, CredentialsProvider p){
		provider = p;
		server = node;
	}
	

	private URI getActionURI(String action, boolean add){		
		String url = server.getUrl()+index+Pydio.PARAM_GET_ACTION+"="+action+"&";
		if(StateHolder.getInstance().getRepository() != null && add){
			repositoryId = StateHolder.getInstance().getRepository().getId();
		}
		url += "tmp_repository_id="+repositoryId+"&";
		try{
			return new URI(url);
		}catch(Exception e){}
		return null;
	}
	

	private String getSeed() {
		Requester req = new Requester();
		HttpResponse resp = req.issueRequest(this.getActionURI(Pydio.AUTH_GET_SEED, false), null);
		return HttpResponseParser.getString(resp);
	}
	
	
	public void refreshToken(){
		
		Requester req = new Requester();		
		if("".equals(user) || "".equals(password)){
			//throw Message.create(1, 1, "require credentials");
		}		
		Map<String, String> loginPass = new HashMap<String, String>();
		
		if(!skipAuth && !"".equals(user) && !"".equals(password)){
			String seed = getSeed();
			if(seed != null) seed = seed.trim();		
			
			if(seed.indexOf("captcha") > -1){
				//throw Message.create(1,1, "CAPTCHA");
			}		
			
			if(!seed.trim().equals("-1")){
				AuthenticationUtils.processPydioPassword(password, seed);
			}
			
			loginPass.put("login_seed", seed);
			loginPass.put("Ajxp-Force-Login", "true");
			loginPass.put("userid", user);
			loginPass.put("password", password);
		}
		
		HttpResponse resp = req.issueRequest(this.getActionURI(Pydio.AUTH_GET_TOKEN, false), loginPass);
		JSONObject jObject;
		try {
			jObject = new JSONObject(HttpResponseParser.getString(resp));
			loginStateChanged = true;
			secure_token = jObject.getString("SECURE_TOKEN");
		} catch (JSONException e) {
			//throw Message.create(1, 1, "FAILED TO REFRESH TOKEN");
		}
	}

	
	private void authenticate() {
		
		Requester req = new Requester();
		String seed = getSeed();	
		
		if(seed != null) seed = seed.trim();		
		if(seed.indexOf("captcha") > -1) {			
			//throw Message.create(1,1, "CAPTCHA");
		}		
		
		if("".equals(user) && "".equals(password)){
			if(provider == null){
				//throw Message.create(1, 1, "NO CREDENTIALS PROVIDER SET");
			}
			UsernamePasswordCredentials credentials = provider.requestForLoginPassword();
			user = credentials.getUserName();
			password = credentials.getPassword();
		}
		
		if(!seed.trim().equals("-1")){
			password = AuthenticationUtils.processPydioPassword(password, seed);
		}	
		
		Map<String, String> loginPass = new HashMap<String, String>();
		loginPass.put("userid", user);
		loginPass.put("password", password);
		loginPass.put("login_seed", seed);
		loginPass.put("Ajxp-Force-Login", "true");
				
		req = new Requester();
		Document doc = HttpResponseParser.getXML(req.issueRequest(this.getActionURI("login", false), loginPass));				
		if(doc.getElementsByTagName("logging_result").getLength() > 0){
			String result = doc.getElementsByTagName("logging_result").item(0).getAttributes().getNamedItem("value").getNodeValue();
			if(result.equals("1")){
				String newToken = doc.getElementsByTagName("logging_result").item(0).getAttributes().getNamedItem("secure_token").getNodeValue();
				loginStateChanged = true;
				secure_token = newToken;
			}else{
				//authentication failed
			}
		}
	}
	
	
	@SuppressWarnings("deprecation")
	private boolean isAuthenticationRequested(HttpResponse response){

		Header[] heads = response.getHeaders("Content-type");
		boolean xml = false;	
		
		for(int i=0;i<heads.length;i++){
			if(heads[i].getValue().contains("text/xml")) xml = true;
		}
		
		if(!xml || loginStateChanged) return false;
		
		try{
			HttpEntity ent = response.getEntity();
			Document doc;
			if(ent.getClass() == XMLDocEntity.class){
				doc = ((XMLDocEntity)ent).getDoc();
				((XMLDocEntity)ent).toLogger();
			}else{
				XMLDocEntity docEntity = new XMLDocEntity(ent);
				doc = docEntity.getDoc();
				ent.consumeContent();// Make sure to clear resources
				response.setEntity(docEntity);
				docEntity.toLogger();
			}

			if(doc.getElementsByTagName("ajxp_registry_part").getLength() > 0
					&& doc.getDocumentElement().getAttribute("xPath").equals("user/repositories")
					&& doc.getElementsByTagName("repositories").getLength() == 0){
				//Log.d("RestRequest Authentication", "EMPTY REGISTRY : AUTH IS REQUIRED");
				this.auth_step = "LOG-USER";
				return true;
			}
			
			if(doc.getElementsByTagName("message").getLength() > 0){
				if(doc.getElementsByTagName("message").item(0).getFirstChild().getNodeValue().trim().contains("You are not allowed to access this resource.")){
					//Log.d("RestRequest Authentication", "REQUIRE_AUTH TAG : TOKEN IS REQUIRED");
					this.auth_step = "RENEW-TOKEN";
					return true;
				}
			}
			
			if(doc.getElementsByTagName("require_auth").getLength() > 0){
				//Log.d("RestRequest Authentication", "REQUIRE_AUTH TAG : AUTH IS REQUIRED");
				if(doc.getElementsByTagName("message").getLength() > 0) {
					throw new Exception(doc.getElementsByTagName("message").item(0).getFirstChild().getNodeValue().trim());
				}
				this.auth_step = "LOG-USER";
				return true;
			}
			
		}catch (SAXException sax) {
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	

	public HttpResponse getResponse(String action, Map<String, String> params) {		
		return request(null, getActionURI(action, true), params);
	}

	
	public String getStringContent(String action, Map<String, String> params) {
		HttpResponse response  = this.request(null, this.getActionURI(action, true), params);
		return HttpResponseParser.getString(response);
	}

	
	public Document getXmlContent(String action, Map<String, String> params){
		HttpResponse response  = this.request(null, this.getActionURI(action, true), params);
		return HttpResponseParser.getXML(response);
	}

	
	public JSONObject getJsonContent(String action, Map<String, String> params) {
		return null;
	}
	
	
	private HttpResponse request(Requester req, URI uri, Map<String, String> params){
		if(req == null){
			req = new Requester();
		}
			
		HttpResponse response = null;	
		
		for(;;){			
			if(!"".equals(secure_token)){
				params.put("secure_token", secure_token);
			}
			response = req.issueRequest(uri, params);
			
			if(isAuthenticationRequested(response)){				
				if(auth_step.equals("RENEW-TOKEN")) {
					refreshToken();					
				}else if (auth_step.equals("LOG-USER")){					
					authenticate();
					if(loginStateChanged){
						loginStateChanged = false;
					}else{
						user = "";
						password ="";
					}
				}
			}else{
				break;
			}
		}
		return response;
	}

	



	public Document putContent(String action, Map<String, String> params, File file, String filename, CountingMultipartRequestEntity.ProgressListener listener) {
		Requester req = new Requester();
		req.setFile(file);
		req.setProgressListener(listener);
		req.setFilename(filename);
		if(!"".equals(secure_token)){
			params.put("secure_token", secure_token);
		}
		HttpResponse response = req.issueRequest(getActionURI(action, true), params);
		System.out.println(HttpResponseParser.getString(response));
		return HttpResponseParser.getXML(response);
	}


	public Document putContent(String action, Map<String, String> params, byte[] data, String filename, CountingMultipartRequestEntity.ProgressListener listener) {
		return null;
	}

	
}