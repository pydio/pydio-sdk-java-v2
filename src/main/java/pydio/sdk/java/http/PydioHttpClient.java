/**
 *  Copyright 2012 Charles du Jeu
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  This file is part of the AjaXplorer Java Client
 *  More info on http://ajaxplorer.info/
 */
package pydio.sdk.java.http;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EncodingUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import pydio.sdk.java.security.HostNameVerifier;
import pydio.sdk.java.security.SSLSocketFactory;
import pydio.sdk.java.utils.Pydio;

public class PydioHttpClient extends DefaultHttpClient {

	public HttpContext localContext = new BasicHttpContext();
	public CookieStore cookieStore = new BasicCookieStore();
    ClientConnectionManager mConnectionManager;
	int port;

	public PydioHttpClient() {
		super();
		this.port = port <= 0 ? 80 : port;
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
	}

    public void turnSecure(){
        SchemeRegistry registry;
        if(mConnectionManager != null){
            registry = mConnectionManager.getSchemeRegistry();
			registry.unregister("https");
			registry.unregister("http");
		} else {
			registry = new SchemeRegistry();
			mConnectionManager = new ThreadSafeClientConnManager(getParams(), registry);
		}
		registry.register(new Scheme("https", new SSLSocketFactory(), 443));
    }

	@Override
	protected ClientConnectionManager createClientConnectionManager() {
        if(mConnectionManager != null) return mConnectionManager;
		SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		org.apache.http.conn.ssl.SSLSocketFactory factory = org.apache.http.conn.ssl.SSLSocketFactory.getSocketFactory();
		//factory.setHostnameVerifier(new HostNameVerifier());
        registry.register(new Scheme("https", factory, 443));
        mConnectionManager = new ThreadSafeClientConnManager(getParams(), registry);
        return mConnectionManager;
	}

	public void clearCookies() {
		if(cookieStore != null) cookieStore.clear();
	}

	public HttpResponse executeInContext(HttpRequestBase request) throws IOException {
		HttpConnectionParams.setConnectionTimeout(getParams(), 60000);
		return execute(request, localContext);
	}

	public List<Cookie> getCookies(URI uri) {
		// Duplicate and prune
		List<Cookie> originalCookies = cookieStore.getCookies();
		List<Cookie> cookies = new ArrayList<Cookie>(originalCookies);
		for (int i = 0; i < originalCookies.size(); i++) {
			if (!originalCookies.get(i).getDomain().equals(uri.getHost())) {
				cookies.remove(originalCookies.get(i));
			}
		}
		return cookies;
	}
	
	public void destroy() {
		// TODO Auto-generated method stub
	}

	public HttpResponse execute(URI uri, Map<String, String> postParameters, HttpContentBody httpBody) throws IOException {

		HttpRequestBase request;

		if(postParameters != null || httpBody != null){
			request = new HttpPost();
			if(httpBody != null){
				CountingMultipartRequestEntity.ProgressListener listener = httpBody.listener();

				if(listener != null){
					listener.partTransferred(httpBody.getCurrentIndex() , httpBody.getTotalChunks());
				}

				MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				reqEntity.addPart("userfile_0", httpBody);

				if(httpBody.getFilename() != null && !EncodingUtils.getAsciiString(EncodingUtils.getBytes(httpBody.getFilename(), "US-ASCII")).equals(httpBody.getFilename())){
					reqEntity.addPart(Pydio.PARAM_URL_ENCODED_FILENAME, new StringBody(java.net.URLEncoder.encode(httpBody.getFilename(), "utf-8")));
				}

				if(httpBody != null && !httpBody.getFilename().equals(httpBody.getRootFilename())){
					reqEntity.addPart(Pydio.PARAM_APPEND_TO_URLENCODED_PART, new StringBody(java.net.URLEncoder.encode(httpBody.getRootFilename(), "utf-8")));
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
		HttpResponse response = executeInContext(request);
		if(httpBody != null && httpBody.isChunked() && !httpBody.allChunksUploaded()){
			this.discardResponse(response);
			execute(uri, postParameters, httpBody);
		}
		return response;
	}

	public void discardResponse(HttpResponse response) {
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

}
