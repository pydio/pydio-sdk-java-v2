package com.pydio.sdk.sync.content;

import com.pydio.sdk.core.Client;
import com.pydio.sdk.core.common.errors.SDKException;
import com.pydio.sdk.core.model.ServerNode;
import com.pydio.sdk.core.model.Stats;
import com.pydio.sdk.sync.Error;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

public class PydioRemoteFileContent implements Content {

    private Stats stats;
    private Error error;

    private Client client;
    private String ws;
    private String path;

    public PydioRemoteFileContent(Client client, String ws, String path){
        this.client = client;
        this.ws = ws;
        this.path = path;

        try {
            stats = client.stats(ws, path, true);
        } catch (SDKException e) {
            e.printStackTrace();
            this.error = Error.notMounted("");
        }
    }

    @Override
    public Error getError() {
        return error;
    }

    @Override
    public String getMd5() {
        return stats.getHash();
    }

    @Override
    public long getSize() {
        return stats.getSize();
    }

    @Override
    public boolean exists() {
        return stats != null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        String url;
        try {
            url = client.downloadURL(ws, path);
        } catch (SDKException e) {
            e.printStackTrace();
            this.error = Error.notMounted("");
            return null;
        }

        ServerNode serverNode = client.getServerNode();
        if(serverNode.isSSLUnverified()) {
            SSLContext sslContext = serverNode.getSslContext();
            HttpsURLConnection c = (HttpsURLConnection) new URL(url).openConnection();
            c.setSSLSocketFactory(sslContext.getSocketFactory());
            c.setHostnameVerifier((host, sslSession) -> serverNode.host().equals(host));
            return c.getInputStream();
        } else {
            return new java.net.URL(url).openStream();
        }
    }
}
