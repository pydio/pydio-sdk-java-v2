package pydio.sdk.java.core.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import pydio.sdk.java.core.model.Node;
import pydio.sdk.java.core.model.NodeDiff;
import pydio.sdk.java.core.security.CertificateTrust;
import pydio.sdk.java.core.utils.ApplicationData;
import pydio.sdk.java.core.utils.HttpResponseParser;
import pydio.sdk.java.core.utils.Log;
import pydio.sdk.java.core.utils.Pydio;

/**
 * Created by jabar on 12/04/2016.
 */
public class HttpClient {
    static final long HTTP_DATA_BUFFER_MAX_SIZE = 10*1024*1024;
    private static Map<String, String> redirectedAddresses = new HashMap<String, String>();
    private static final String LINE_FEED = "\r\n";
    private static final String DOUBLE_LINE_FEED = "\r\n\r\n";

    private SSLContext mSSLContext;
    private boolean mSSLUnverifiedMode;
    private CertificateTrust.Helper mCertificateTrustHelper;
    public CookieManager mCookieManager;

    public HttpClient(boolean b) {
        mSSLUnverifiedMode = b;
        mCookieManager = new CookieManager();
    }

    public HttpClient(boolean b, CookieManager cm) {
        mSSLUnverifiedMode = b;
        if(cm == null) {
            mCookieManager = new CookieManager();
        }else {
            mCookieManager = cm;
        }
    }

    public void enableUnverifiedMode(CertificateTrust.Helper helper){
        mSSLUnverifiedMode = true;
        mCertificateTrustHelper = helper;
    }

    private SSLContext sslContext() throws IOException {
        if(mSSLContext != null) return mSSLContext;

        try {
            mSSLContext = SSLContext.getInstance("TLS");
            mSSLContext.init(null, new TrustManager[]{new pydio.sdk.java.core.security.CertificateTrustManager(mCertificateTrustHelper)}, null);
            return mSSLContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            Log.e("SSL", "Failed to create ssl context");
        }
        throw new IOException("NO TLS");
    }

    public HttpResponse send(String url, Map<String, String> params, ContentBody body) throws IOException {

        URL urlObject = new URL(url);
        //URI uri = URI.create(urlObject.getProtocol() + "://" + urlObject.getHost() + urlObject.getPath());
        String address = urlObject.getProtocol() + "://" + urlObject.getHost() + urlObject.getPath();
        if(redirectedAddresses.containsKey(address)){
            url = url.replace(address, redirectedAddresses.get(address));
        }

        HttpURLConnection con;
        if(mSSLUnverifiedMode){
            HttpsURLConnection c = (HttpsURLConnection) new URL(url).openConnection();
            c.setSSLSocketFactory(sslContext().getSocketFactory());
            c.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
            con = c;
        } else {
            con = (HttpURLConnection) new URL(url).openConnection();
        }

        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setDoInput(true);

        List<HttpCookie> cookies = mCookieManager.getCookieStore().getCookies();
        if(cookies.size() > 0) {
            String cookieString = "";
            for (int i = 0; i < cookies.size(); i++) {
                String value = ";" + cookies.get(i).toString();
                cookieString += value;
            }
            con.setRequestProperty("Cookie", cookieString.substring(1));
        }
        con.setRequestProperty("User-Agent", "Pydio-Native-" + ApplicationData.name + " " + ApplicationData.version + "." + ApplicationData.versionCode);

        String CHARSET_UTF8 = "utf-8";
        if(body != null){
            String boundary = "----" + System.currentTimeMillis();
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            ByteArrayOutputStream partHeaderBuffer = new ByteArrayOutputStream();
            Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();

            partHeaderBuffer.write(("--" + boundary).getBytes());
            partHeaderBuffer.write(LINE_FEED.getBytes());

            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                String name = entry.getKey(), value = entry.getValue();
                partHeaderBuffer.write(("Content-Disposition: form-data; name=\"" + URLEncoder.encode(name, CHARSET_UTF8) + "\"").getBytes());
                partHeaderBuffer.write(LINE_FEED.getBytes());
                partHeaderBuffer.write(("Content-Type: text/plain; charset="+ CHARSET_UTF8).getBytes());
                partHeaderBuffer.write(DOUBLE_LINE_FEED.getBytes());
                partHeaderBuffer.write(value.getBytes(CHARSET_UTF8));
                partHeaderBuffer.write(LINE_FEED.getBytes());
                partHeaderBuffer.write(("--" + boundary).getBytes());
                partHeaderBuffer.write(LINE_FEED.getBytes());
            }

            partHeaderBuffer.write(("Content-Disposition: form-data; name=\"userfile_0\"; filename=" + URLEncoder.encode(body.getFilename(), CHARSET_UTF8)).getBytes());
            partHeaderBuffer.write(LINE_FEED.getBytes());
            partHeaderBuffer.write(("Content-Type: " + body.getContentType()).getBytes());

            partHeaderBuffer.write(DOUBLE_LINE_FEED.getBytes());
            byte[] partHeaderBytes = partHeaderBuffer.toByteArray();
            byte[] lastBoundaryBytes = (LINE_FEED + "--" + boundary + "--" + LINE_FEED).getBytes();

            int rest = partHeaderBytes.length + lastBoundaryBytes.length;
            int contentSupposedBodyLength = (int) (body.maxChunkSize() - rest);
            int contentBodyActualLength = (int) Math.min(contentSupposedBodyLength, body.available());

            con.setFixedLengthStreamingMode(contentBodyActualLength + rest);

            OutputStream out = con.getOutputStream();
            out.write(partHeaderBytes);
            body.writeTo(out, contentBodyActualLength);
            out.write(lastBoundaryBytes);

            HttpResponse response = new HttpResponse(con);

            Map<String, List<String>> headerFields = con.getHeaderFields();
            List<String> cookiesHeader = headerFields.get("Set-Cookie");
            if(cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    List<HttpCookie> cs = HttpCookie.parse(cookie);
                    for (HttpCookie hc: cs) {
                        mCookieManager.getCookieStore().add(null, hc);
                    }
                }
            }

            int code = response.code();
            if(code == 303 || code == 307 || code == 308){
                String location = response.getHeaders("Location").get(0);
                url = url.replace(address, location);
                redirectedAddresses.put(address, location);
                return send(url, params, body);
            }

            if(code == 200 && !body.allChunksWritten()){
                try {
                    NodeDiff diff = NodeDiff.create(HttpResponseParser.getXML(response));
                    if (diff.added != null) {
                        Node node = diff.added.get(0);
                        String label = node.label();
                        if (!label.equals(body.getFilename())) {
                            body.setFilename(label);
                        }
                    }
                }catch (NullPointerException ignored){}

                params.put(Pydio.PARAM_APPEND_TO_URLENCODED_PART, body.getFilename());
                send(url, params, body);
            }
            return response;

        } else  {
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(entry.getKey(), CHARSET_UTF8));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(entry.getValue()), CHARSET_UTF8));
            }

            byte[] postDataBytes = postData.toString().getBytes(CHARSET_UTF8);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + CHARSET_UTF8);
            con.setRequestProperty("Content-Length", String.valueOf(postData.length()));
            OutputStream out = con.getOutputStream();
            out.write(postDataBytes);

            Map<String, List<String>> headerFields = con.getHeaderFields();
            List<String> cookiesHeader = headerFields.get("Set-Cookie");
            if(cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    List<HttpCookie> cs = HttpCookie.parse(cookie);
                    for (HttpCookie hc: cs) {
                        mCookieManager.getCookieStore().add(null, hc);
                    }
                }
            }

            HttpResponse response = new HttpResponse(con);
            int code = response.code();
            if(code == 303 || code == 307 || code == 308){
                String location = response.getHeaders("Location").get(0);
                url = url.replace(address, location);
                redirectedAddresses.put(address, location);
                return send(url, params, null);
            }

            return response;
        }
    }

}
