package pydio.sdk.java.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jabar on 29/04/2016.
 */
public class HttpResponse {

    HttpURLConnection mConnection;
    HttpEntity mEntity;

    public HttpResponse(HttpURLConnection   con){
        mConnection = con;
        //HttpClient.responseHeaders(con);
    }

    public HttpEntity getEntity() throws IOException {
        if(!mConnection.getDoInput()){
            mConnection.setDoInput(true);
        }

        if(mEntity == null) {
            mEntity = new ContentBody(mConnection.getInputStream(), "", mConnection.getContentLength(), Long.MAX_VALUE);
        }

        return mEntity;
    }

    public void setEntity(HttpEntity entity){
        mEntity = entity;
    }

    public int code() throws IOException {
        return mConnection.getResponseCode();
    }

    public int getContentLength(){
        return mConnection.getContentLength();
    }

    public String getContentEncoding(){
        return mConnection.getContentEncoding();
    }

    public List<String> getHeaders(String key){
        return mConnection.getHeaderFields().get(key);
    }
}
