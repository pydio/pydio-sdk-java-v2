package pydio.sdk.java.http;


import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EncodingUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import pydio.sdk.java.model.ServerNode;
import pydio.sdk.java.utils.Pydio;
import pydio.sdk.java.utils.UploadStopNotifierProgressListener;


/**
 * 
 * @author pydio
 *
 */
public class Requester {
	private PydioHttpClient httpClient;
	private boolean trustSSL = false;
    private UploadStopNotifierProgressListener listener;
	private String authStep;
    ServerNode server;

	UsernamePasswordCredentials credentials = null;
	//MA_UPLOAD to be checked
	public Requester(ServerNode server){
        this.server = server;
    }
	/**
	 * This method perform an Http POST request. If postparameters and file are null then a Http GET request is perform.
	 * @param uri 
	 * @param postParameters post parameters of the request
	 * @return returns an HTTPResponse.
	 */
	public HttpResponse issueRequest(URI uri, Map<String, String> postParameters, UploadFileBody fileBody) throws IOException {
        if(httpClient == null) {
            httpClient = new PydioHttpClient(trustSSL, server.port());
        } else {
            httpClient.setTrustSelfSignedSSL(trustSSL);
        }

		if(credentials != null){
			httpClient.refreshCredentials(credentials);
		}

        HttpRequestBase request;

        if(postParameters != null || fileBody != null){
            request = new HttpPost();
            if(fileBody != null){
                CountingMultipartRequestEntity.ProgressListener listener = fileBody.listener();

                if(listener != null){
                    listener.partTransferred(fileBody.getCurrentIndex() , fileBody.getTotalChunks());
                }

                MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                reqEntity.addPart("userfile_0", fileBody);

                if(fileBody.getFilename() != null && !EncodingUtils.getAsciiString(EncodingUtils.getBytes(fileBody.getFilename(), "US-ASCII")).equals(fileBody.getFilename())){
                    reqEntity.addPart("urlencoded_filename", new StringBody(java.net.URLEncoder.encode(fileBody.getFilename(), "utf-8")));
                }

                if(fileBody != null && !fileBody.getFilename().equals(fileBody.getRootFilename())){
                    reqEntity.addPart("appendto_urlencoded_part", new StringBody(java.net.URLEncoder.encode(fileBody.getRootFilename(), "utf-8")));
                }

                if(postParameters != null){
                    Iterator<Map.Entry<String, String>> it = postParameters.entrySet().iterator();
                    while(it.hasNext()){
                        Map.Entry<String, String> entry = it.next();
                        reqEntity.addPart(entry.getKey(), new StringBody(new String(entry.getValue().getBytes(), "utf-8")));
                    }
                }
                if(listener != null){
                    CountingMultipartRequestEntity countingEntity = new CountingMultipartRequestEntity(reqEntity, listener);
                    ((HttpPost)request).setEntity(countingEntity);
                }else{
                    ((HttpPost)request).setEntity(reqEntity);
                }
            }else{
                request.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(postParameters.size());
                Iterator<Map.Entry<String, String>> it = postParameters.entrySet().iterator();

                while(it.hasNext()){
                    Map.Entry<String, String> entry = it.next();
                    nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                ((HttpPost)request).setEntity(new UrlEncodedFormEntity(nameValuePairs, "utf-8"));
            }
        }else{
            request = new HttpGet();
        }
        request.setURI(uri);
        HttpResponse response = httpClient.executeInContext(request);
        if(fileBody != null && fileBody.isChunked() && !fileBody.allChunksUploaded()){
            this.discardResponse(response);
            this.issueRequest(uri, postParameters, fileBody);
        }
        return response;
	}
	/**
	 * This method read the response data and consume it.
	 * @param response
	 */
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
	/**
	 * Call this method to trust or not SSL self-signed certificates.
	 * @param trust
	 */
	public void setTrustSSL(boolean trust){
		trustSSL = trust;
        if(httpClient != null){
            httpClient.setTrustSelfSignedSSL(trust);
        }
	}
    public boolean isTrustSSL(){
        return trustSSL;
    }
	/**
	 * Set username and password for Http basic authentication
	 * @param user username string
	 * @param password password string
	 */
	public void setCredentials(String user, String password){
		this.credentials = new UsernamePasswordCredentials(user, password);
	}

    public UploadFileBody newFileBody(File file){
        UploadFileBody fileBody;
        String fileName = file.getName();
        fileBody = new UploadFileBody(file, fileName);
        long maxUpload = Long.parseLong(server.getRemoteConfig(Pydio.REMOTE_CONFIG_UPLOAD_SIZE));
        if(maxUpload > 0 && maxUpload < file.length()){
            fileBody.chunkIntoPieces((int)maxUpload);
        }
        return fileBody;
    }

    public void clearCookies(){
        if(httpClient != null){
            httpClient.clearCookies();
        }
    }
}
