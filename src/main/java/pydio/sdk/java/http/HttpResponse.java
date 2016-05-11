package pydio.sdk.java.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jabar on 29/04/2016.
 */
public class HttpResponse {

    HttpEntity mEntity;
    int mCode;
    Map<String, List<String>> mHeaders;

    public HttpResponse(int code, Map<String, List<String>> headers, final InputStream in){
        mHeaders = headers;
        mEntity = new PartialRepeatableEntity(in, 4096);
        mCode = code;
    }

    public HttpResponse(HttpURLConnection   con) throws IOException {
        mCode = con.getResponseCode();
        mHeaders = con.getHeaderFields();
        mEntity = new PartialRepeatableEntity(new HttpResponseEntity(con), 4096);
    }

    public HttpEntity getEntity() throws IOException {
        return mEntity;
    }

    public void setEntity(HttpEntity entity){
        mEntity = entity;
    }

    public int code() throws IOException {
        return mCode;
    }

    public List<String> getHeaders(String key){
        return mHeaders.get(key);
    }
}
