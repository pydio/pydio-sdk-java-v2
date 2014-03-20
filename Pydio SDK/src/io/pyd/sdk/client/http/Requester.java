package io.pyd.sdk.client.http;





import io.pyd.sdk.client.model.Message;
import io.pyd.sdk.client.utils.ServerResolver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
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
	private AjxpHttpClient httpClient;
	private boolean trustSSL = false;
	private CountingMultipartRequestEntity.ProgressListener progressListener;	
	String authStep;
	
	UsernamePasswordCredentials credentials = null;
	
	
	//MAX_UPLOAD to be checked
	
	public HttpResponse issueRequest(URI uri, Map<String, String> postParameters) throws Message{
		
		
		
		httpClient = new AjxpHttpClient(trustSSL);
		
		if(credentials != null){
			httpClient.refreshCredentials(credentials);
		}
		
     	if(uri.toString().contains(ServerResolver.SERVER_URL_RESOLUTION)){
     		throw Message.create(1, 1, "resolution needed");
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
			
			HttpResponse response = httpClient.executeInContext(request);			
		
			
			if(fileBody != null && fileBody.isChunked() && !fileBody.allChunksUploaded()){
				this.discardResponse(response);
				this.issueRequest(uri, postParameters);
			}
			
			return response;
			
		}catch(IOException e){
			Message m = new Message();
			m.setMessage(e.getMessage());
			throw m;
		}
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
	
	
	
	public void setFile(File file){
		this.file = file;
	}
	
	public void setTrustSSL(boolean trust){
		trustSSL = trust;
	}

	public void setUploadProgressListener(CountingMultipartRequestEntity.ProgressListener uploadList){
		this.progressListener = uploadList;
	}
	
	
	public void setCredentials(String user, String password){
		this.credentials = new UsernamePasswordCredentials(user, password);
	}
}
