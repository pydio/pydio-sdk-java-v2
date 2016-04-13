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
package pydio.sdk.java.security;

import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;


public class SSLSocketFactory implements SocketFactory, LayeredSocketFactory {
    private SSLContext sslcontext = null;

    public static String[] enabledProtocols = new String[]{"TLSv1", "TLSv1.2", "SSLv3", "SSLv2Hello"};
    public static String[] enabledCipherSuites = new String[]{"SSL_RSA_WITH_RC4_128_MD5", "TLS_RSA_WITH_AES_128_CBC_SHA"};

    public SSLSocketFactory() {}

    private static SSLContext createEasySSLContext() throws IOException {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] { new CertificateTrustManager()}, null);
            return context;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private SSLContext getSSLContext() throws IOException{
        if (this.sslcontext == null) {
            try {
                this.sslcontext = SSLContext.getInstance("TLS");
                this.sslcontext.init(null, new TrustManager[] { new CertificateTrustManager()}, null);
            } catch (Exception e) {
                throw new IOException("Failed to create SSL context");
            }
        }
        return this.sslcontext;
    }

	public Socket connectSocket(Socket sock, String host, int port, InetAddress localAddress, int localPort, HttpParams params) throws IOException{

        int connTimeout = HttpConnectionParams.getConnectionTimeout(params);
        int soTimeout = HttpConnectionParams.getSoTimeout(params);

        InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
        SSLSocket socket = (SSLSocket) ((sock != null) ? sock : createSocket());

        if ((localAddress != null) || (localPort > 0)) {
            // we need to bind explicitly
            if (localPort < 0) {
                localPort = 0; // indicates "any"
            }
            InetSocketAddress isa = new InetSocketAddress(localAddress, localPort);
            socket.bind(isa);
        }
        socket.setSoTimeout(soTimeout);
        socket.connect(remoteAddress, connTimeout);
        return socket;
	}

	public Socket createSocket() throws IOException {
		return getSSLContext().getSocketFactory().createSocket();
	}

	public boolean isSecure(Socket arg0) throws IllegalArgumentException {
		return true;
	}

	public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
			throws IOException {
		return getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
	}
}