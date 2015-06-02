package pydio.sdk.java.transport;

import org.apache.http.HttpResponse;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import pydio.sdk.java.auth.AuthenticationHelper;
import pydio.sdk.java.model.ServerNode;
import pydio.sdk.java.utils.ProgressListener;

/**
 * Created by pydio on 13/02/2015.
 */
public interface Transport {

    public static int MODE_SESSION = 1;
    public static int MODE_RESTFUL = 2;

    public int requestStatus();

    public HttpResponse getResponse(String action, Map<String, String> params);

    public String getStringContent(String action, Map<String, String> params);

    public Document getXmlContent(String action, Map<String, String> params);

    public JSONObject getJsonContent(String action, Map<String, String> params);

    public InputStream getResponseStream(String action, Map<String, String> params) ;

    public Document putContent(String action, Map<String, String> params, File file, String filename, ProgressListener handler);

    public Document putContent(String action, Map<String, String> params, byte[] data, String filename, ProgressListener handler);

    public void setAuthenticationHelper(AuthenticationHelper helper);

    public void setServer(ServerNode server);
}
