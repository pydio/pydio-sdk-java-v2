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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import pydio.sdk.java.model.ServerNode;
import pydio.sdk.java.utils.ProgressListener;
import pydio.sdk.java.utils.Pydio;


/**
 * 
 * @author pydio
 *
 */
public class Requester {
    private AjxpFileBody fileBody;
	private File file;
	private String fileName;
	private AjxpHttpClient httpClient;
	private boolean trustSSL = false;
	private CountingMultipartRequestEntity.ProgressListener progressListener;
    private ProgressListener listener;
	private String authStep;
    ServerNode server;
    boolean fresh = true;

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
	public HttpResponse issueRequest(URI uri, Map<String, String> postParameters) throws IOException {
		
		httpClient = new AjxpHttpClient(server.isSSLselfSigned(), server.port());

        try {
            CookieStore cstore = httpClient.getCookieStore();
            List<Cookie> cookies = cstore.getCookies();
            for (int i = 0; i < cookies.size(); i++) {
                Cookie c = cookies.get(i);
                System.out.println(c.toString());
            }

        }catch(Exception e){
            System.out.println(e.getMessage());
        }

		if(credentials != null){
			httpClient.refreshCredentials(credentials);
		}

        HttpRequestBase request;
        if(postParameters != null || file != null){
            request = new HttpPost();

            if(file != null){
                if(fileBody == null){
                    if(fileName == null) fileName = file.getName();
                    fileBody = new AjxpFileBody(file, fileName);
                    long maxUpload = Long.parseLong(server.getRemoteConfig(Pydio.REMOTE_CONFIG_UPLOAD_SIZE));
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
                    reqEntity.addPart("urlencoded_filename", new StringBody(java.net.URLEncoder.encode(fileName, StandardCharsets.UTF_8.name())));
                }

                if(fileBody != null && !fileBody.getFilename().equals(fileBody.getRootFilename())){
                    reqEntity.addPart("appendto_urlencoded_part", new StringBody(java.net.URLEncoder.encode(fileBody.getRootFilename(), StandardCharsets.UTF_8.name())));
                }

                if(postParameters != null){
                    Iterator<Map.Entry<String, String>> it = postParameters.entrySet().iterator();
                    while(it.hasNext()){
                        Map.Entry<String, String> entry = it.next();
                        reqEntity.addPart(entry.getKey(), new StringBody(new String(entry.getValue().getBytes(), StandardCharsets.UTF_8.name())));
                    }
                }
                if(progressListener != null){
                    CountingMultipartRequestEntity countingEntity = new CountingMultipartRequestEntity(reqEntity, progressListener);
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
                ((HttpPost)request).setEntity(new UrlEncodedFormEntity(nameValuePairs, StandardCharsets.UTF_8.name()));
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
	 * This method is used to pass a file to upload and  must be called before issueRequest(). 
	 * @param file Object File to be uploaded
	 */
	public void setFile(File file){
		this.file = file;
	}
	/**
	 * Call this method to trust or not SSL self-signed certificates.
	 * @param trust
	 */
	public void setTrustSSL(boolean trust){
		trustSSL = trust;
	}
	/**
	 * Set username and password for Http basic authentication
	 * @param user username string
	 * @param password password string
	 */
	public void setCredentials(String user, String password){
		this.credentials = new UsernamePasswordCredentials(user, password);
	}
	/**
	 * Set a listener to follow upload progress.
	 * @param listener
	 */
	public void setProgressListener(final ProgressListener listener){
		this.listener = listener;
        progressListener = new CountingMultipartRequestEntity.ProgressListener() {
            @Override
            public void transferred(long num) throws IOException {
                listener.onProgress(num);
            }

            @Override
            public void partTransferred(int part, int total) throws IOException {
                listener.onProgress(part*100 / total);
            }
        };
	}
	/**
	 * This method is used to set the final name of the file to be uploaded and must be called before issueRequest();
	 * @param fname
	 */
	public void setFilename(String fname){
		fileName = fname;
	}
}
