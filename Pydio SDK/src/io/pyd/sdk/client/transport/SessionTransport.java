
package io.pyd.sdk.client.transport;

import io.pyd.sdk.client.auth.CredentialsProvider;
import io.pyd.sdk.client.http.HttpResponseParser;
import io.pyd.sdk.client.http.Requester;
import io.pyd.sdk.client.http.XMLDocEntity;
import io.pyd.sdk.client.model.Message;
import io.pyd.sdk.client.transport.Transport;
import io.pyd.sdk.client.utils.PydioProtocol;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;


public class SessionTransport implements Transport{	
	
	public String server_url = "http://pydio/test/";
	public String index = "index.php?";
	public String secure_token = "";
	public String auth_step = "";
	public String repositoryId = "";	
	String user = "root";
	String password ="pydiotest";	
	boolean loginStateChanged = false;	
	boolean skipAuth = false;	
	CredentialsProvider provider;
	
	

	private URI getActionURI(String action, boolean add){
		
		String url = server_url+index+"get_action="+action+"&";
		if(add && !"".equals(repositoryId)){
			url += "&tmp_repository_id="+repositoryId;
		}
		
		URI uri = null;
		try{
			uri = new URI(url);
		}catch(Exception e){}
		return uri;
	}
		
	
	/***
	 * 
	 * @throws Message
	 */
	public void refreshToken() throws Message{
		Requester req = new Requester();
		
		if("".equals(user) || "".equals(password)){
			throw Message.create(1, 1, "require credentials");
		}
		
		Map<String, String> loginPass = new HashMap<String, String>();
		
		if(!skipAuth && !"".equals(user) && !"".equals(password)){
			String seed = getSeed();
			if(seed != null) seed = seed.trim();		
			
			if(seed.indexOf("captcha") > -1){
				throw Message.create(1,1, "CAPTCHA");
			}				
			
			if(!seed.trim().equals("-1")){
				password = md5(password) + seed;
				password = md5(password);
			}
			
			loginPass.put("login_seed", seed);
			loginPass.put("Ajxp-Force-Login", "true");
			loginPass.put("userid", user);
			loginPass.put("password", password);
		}
		
		HttpResponse resp = req.issueRequest(this.getActionURI(PydioProtocol.AUTH_GET_TOKEN, false), loginPass);
		JSONObject jObject;
		try {
			jObject = new JSONObject(HttpResponseParser.getString(resp));
			loginStateChanged = true;
			secure_token = jObject.getString("SECURE_TOKEN");
		} catch (JSONException e) {
			throw Message.create(1, 1, "FAILED TO REFRESH TOKEN");
		}
	}
	
	
	/***
	 * 
	 * 
	 * 
	 * @throws Message
	 */
	private String getSeed() throws Message{
		Requester req = new Requester();
		HttpResponse resp = req.issueRequest(this.getActionURI(PydioProtocol.AUTH_GET_SEED, false), null);
		return HttpResponseParser.getString(resp);
	}
	
	
	
	/*** 
	 * @throws Message
	 */
	
	private void authenticate() throws Message{
		
		Requester req = new Requester();		
		String seed = getSeed();			
		
		if(seed != null) seed = seed.trim();		
		if(seed.indexOf("captcha") > -1) throw Message.create(1,1, "CAPTCHA");
		
		
		if("".equals(user) && !"".equals(password)){
			if(provider == null){
				throw Message.create(1, 1, "NO CREDENTIALS PROVIDER SET");
			}
			
			Entry<String, String> pair = provider.requestForLoginPassword();
			user = pair.getKey();
			password = pair.getValue();
		}
		
		
		
		if(!seed.trim().equals("-1")){
			password = md5(password) + seed;
			password = md5(password);
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
				throw Message.create(1, 1, "LOGIN FAILED");
			}
		}
	}
	
	
	/***
	 * 
	 * 
	 * @param response
	 * @return
	 */
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
			
		}catch (Exception e) {
			//error(e);
			/*
			 * sendMessageToHandler(MessageListener.MESSAGE_WHAT_ERROR,
			 * e.getMessage());
			 * e.printStackTrace();
			 */
		}
		return false;
	}
	

	/***
	 * 
	 * 
	 * 
	 */
	public HttpResponse getResponse(String action, Map<String, String> params) {
		// TODO Auto-generated method stub
		URI uri = getActionURI(action, true);
		try {
			return request(getActionURI(action, true), params);
		} catch (Message e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/***
	 * 
	 * 
	 * 
	 * 
	 */
	public String getStringContent(String action, Map<String, String> params) {
		try {
			HttpResponse response  = this.request(this.getActionURI(action, true), params);
			return HttpResponseParser.getString(response);
		} catch (Message e) {
			e.printStackTrace();
		}
		return null;
	}

	/***
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	public Document getXmlContent(String action, Map<String, String> params){
		try {
			HttpResponse response  = this.request(this.getActionURI(action, true), params);
			return HttpResponseParser.getXML(response);
		} catch (Message e) {
			e.printStackTrace();
		}
		return null;
	}

	
	/***
	 * 
	 * 
	 * 
	 */
	public JSONObject getJsonContent(String action, Map<String, String> params) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/***
	 * 
	 * 
	 * 
	 */

	public InputStream getResponseStream(String action,	Map<String, String> params) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	/*** 
	 * @param uri
	 * @param params
	 * @return
	 * @throws Message
	 */
	private HttpResponse request(URI uri, Map<String, String> params) throws Message{
		
		Requester req = new Requester();
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
					}
				}
			}else{
				break;
			}
		}
		return response;
	}

	@SuppressWarnings("resource")
	public static void main(String[] args){
		
		SessionTransport session = new SessionTransport();		
		for(;;){			
			Map<String, String> params = new HashMap<String, String>();
			System.out.println("\n\n*******************************************");
			System.out.print("action :  ");
			String action = new Scanner(System.in).nextLine();
			
			for(int i = 1;;i++){
				
				System.out.print("parameter "+i+" name :  ");
				String par_name = new Scanner(System.in).nextLine();
				if(".quit".equals(par_name)) break;
				System.out.print("parameter "+i+" value :  ");
				String par_value = new Scanner(System.in).nextLine();
				params.put(par_name, par_value);
			}
			
			System.out.print("output type :  ");
			String out = new Scanner(System.in).nextLine();			
			action(session, action, params, out);
			//action(session, action, params, out);			
		}
	}
	
	/***
	 * @param session
	 * @param action
	 * @param params
	 * @param out
	 */
	public static void action(SessionTransport session, String action, Map<String, String> params, String out){
		
		if(!"xml".equals(out)){
			System.out.println(session.getStringContent(action, params));
			return;
		}
		
		Document resp = session.getXmlContent(action, params);
		
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = null;
		
		try {
			transformer = tf.newTransformer();
		} catch (TransformerConfigurationException e1) {
			e1.printStackTrace();
		}
		
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		StringWriter writer = new StringWriter();
		
		try {
			transformer.transform(new DOMSource(resp), new StreamResult(writer));
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		
		String output = writer.getBuffer().toString();
		System.out.println(output);
		
		try {
			System.out.print("result description  :");
			@SuppressWarnings("resource")
			String description =  new Scanner(System.in).nextLine();		
			FileOutputStream fos = new FileOutputStream("C:\\Users\\pydio\\Desktop\\SDK_test_results\\responses.xml", true);
			fos.write(("<!-- "+description+" -->\n").getBytes());
			fos.write(output.getBytes());
			fos.write(("\n<!-- "+description+" -->\n\n").getBytes());
			
			fos.close();
		} catch (Exception e) {
			System.out.println("can't store result");
		}
	}
	
	
	public static final String md5(final String s) {
	    try {
	        // Create MD5 Hash
	        MessageDigest digest = java.security.MessageDigest
	                .getInstance("MD5");
	        digest.update(s.getBytes());
	        byte messageDigest[] = digest.digest();
	 
	        // Create Hex String
	        StringBuffer hexString = new StringBuffer();
	        for (int i = 0; i < messageDigest.length; i++) {
	            String h = Integer.toHexString(0xFF & messageDigest[i]);
	            while (h.length() < 2)
	                h = "0" + h;
	            hexString.append(h);
	        }
	        return hexString.toString();
	 
	    } catch (NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
	    return "";
	}
}
