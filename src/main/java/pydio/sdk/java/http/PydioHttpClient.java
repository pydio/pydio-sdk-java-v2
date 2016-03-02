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
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import pydio.sdk.java.utils.AjxpSSLSocketFactory;

@SuppressWarnings("deprecation")
public class PydioHttpClient extends DefaultHttpClient {

	boolean trustSelfSignedSSL;
	public HttpContext localContext = new BasicHttpContext();
	public CookieStore cookieStore = new BasicCookieStore();
	int port;

	public PydioHttpClient(boolean trustSSL, int port) {
		super();
		this.port = port <= 0 ? 80 : port;
		this.trustSelfSignedSSL = trustSSL;
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		this.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
	}
	
	public void refreshCredentials(String user, String pass){
		this.getCredentialsProvider().setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM),
                new UsernamePasswordCredentials(user, pass)
        );		
	}
	
	public void refreshCredentials(UsernamePasswordCredentials credentials){
		this.getCredentialsProvider().setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM),
                credentials
        );
	}
	@Override
	protected ClientConnectionManager createClientConnectionManager() {
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), port));
		
		if(trustSelfSignedSSL){
			AjxpSSLSocketFactory socketFactory = new AjxpSSLSocketFactory();
			registry.register(new Scheme("https", socketFactory, 443));
		}else{
			SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
			registry.register(new Scheme("https", socketFactory, 443));
		}

		return new SingleClientConnManager(getParams(), registry);		
	}

	public void clearCookies() {
		if(cookieStore != null) cookieStore.clear();
	}

	public HttpResponse executeInContext(HttpRequestBase request) throws ClientProtocolException, IOException {
		HttpConnectionParams.setConnectionTimeout(getParams(), 3000);
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
}
