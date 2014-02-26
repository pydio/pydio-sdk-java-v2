package io.pyd.sdk.client.http;





import io.pyd.sdk.client.ServerResolver;
import io.pyd.sdk.client.model.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EncodingUtils;


@SuppressWarnings("deprecation")
public class Requester {
	
	private AjxpFileBody fileBody;
	private File file;
	private String fileName;
	private String httpUser;
	private String httpPassword;
	private AjxpHttpClient httpClient;
	private boolean trustSSL = false;	
	private CountingMultipartRequestEntity.ProgressListener progressListener;
	

	
	//MAX_UPLOAD to be checked	
	
	HttpEntity issueRequest(URI uri, Map<String, String> postParameters) throws Message{
		
     	if(uri.toString().contains(ServerResolver.SERVER_URL_RESOLUTION)){
     		Message m = new Message();
     		m.setMessage("Must resolve server");
     		throw m;
     	}
		
		try{
			//HttpResponse response = null;
			HttpRequestBase request;
	
			if(postParameters != null || file != null){
				request = new HttpPost();
				
				if(file != null){
					if(fileBody == null){
						if(fileName == null) fileName = file.getName();
						fileBody = new AjxpFileBody(file, fileName);
						long maxUpload = /*getMaxUploadSize();*/ 60*1024*1024;
						if(maxUpload > 0 && maxUpload < file.length()){
							fileBody.chunkIntoPieces((int)maxUpload);
							if(progressListener != null){
								progressListener.partTransferred(fileBody.getCurrentIndex(), fileBody.getTotalChunks());
							}
						}
					}else{
						if(progressListener != null){
							progressListener.partTransferred(fileBody.getCurrentIndex() , fileBody.getTotalChunks());
						}
					}
					
					MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
					reqEntity.addPart("userfile_0", fileBody);
					
					if(fileName != null && !EncodingUtils.getAsciiString(EncodingUtils.getBytes(fileName, "US-ASCII")).equals(fileName)){
						reqEntity.addPart("urlencoded_filename", new StringBody(java.net.URLEncoder.encode(fileName, "UTF-8")));
					}
					
					if(fileBody != null && !fileBody.getFilename().equals(fileBody.getRootFilename())){
						reqEntity.addPart("appendto_urlencoded_part", new StringBody(java.net.URLEncoder.encode(fileBody.getRootFilename(), "UTF-8")));
					}
					
					if(postParameters != null){						
						Iterator<Map.Entry<String, String>> it = postParameters.entrySet().iterator();
						while(it.hasNext()){
							Map.Entry<String, String> entry = it.next();
							reqEntity.addPart(entry.getKey(), new StringBody(entry.getValue()));
						}					
					}
					
					if(progressListener != null){
						CountingMultipartRequestEntity countingEntity = new CountingMultipartRequestEntity(reqEntity, progressListener);
						((HttpPost)request).setEntity(countingEntity);
					}else{
						((HttpPost)request).setEntity(reqEntity);
					}
				}else{
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(postParameters.size());
					Iterator<Map.Entry<String, String>> it = postParameters.entrySet().iterator();
					
					while(it.hasNext()){
						Map.Entry<String, String> entry = it.next();
						nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
					}
					((HttpPost)request).setEntity(new UrlEncodedFormEntity(nameValuePairs));
				}
			}else{
				request = new HttpGet();
			}
			
			request.setURI(uri);
			
			if(this.httpUser.length()> 0 && this.httpPassword.length()> 0 ){
				request.addHeader("Ajxp-Force-Login", "true");
			}
			
			HttpResponse response = httpClient.executeInContext(request);
			
			if(isAuthenticationRequested(response)){
				Message authMessage = new Message();
				//add auth message
				authMessage.setMessage("");				
				throw authMessage;
			}
			
			if(fileBody != null && fileBody.isChunked() && !fileBody.allChunksUploaded()){
				this.discardResponse(response);
				this.issueRequest(uri, postParameters);
			}
			
			return response.getEntity();
			
		}catch(IOException e){
			Message m = new Message();
			m.setMessage(e.getMessage());
			throw m;
		}
	}

	// TO-DO
	private boolean isAuthenticationRequested(HttpResponse response){
		return true;
	}
	
	private void discardResponse(HttpResponse response) {
		try {
			BufferedReader in = null;
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();
		} catch (IllegalStateException e){
			// Silent, was already consumed
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setHttpUser(String user){
		httpUser = user;
	}
	
	public void setHttpPassword(String password){
		httpPassword = password;
	}
	
	public void setFile(File file){
		this.file = file;
	}
	
	public void setTrustSSL(boolean trust){
		trustSSL = trust;
	}

	public void setUploadProgressListener(CountingMultipartRequestEntity.ProgressListener uploadList){
		this.progressListener = uploadList;
	}
}
