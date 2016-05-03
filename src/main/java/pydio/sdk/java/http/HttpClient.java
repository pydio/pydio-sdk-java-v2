package pydio.sdk.java.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import pydio.sdk.java.security.CertificateTrust;
import pydio.sdk.java.security.CertificateTrustManager;

/**
 * Created by jabar on 12/04/2016.
 */
public class HttpClient {

    private String boundary;
    private static final String LINE_FEED = "\r\n";
    private String charset = "UTF-8";
    SSLContext mSSLContext;
    boolean mSSLUnverifiedMode;
    CertificateTrust.Helper mCertificateTrustHelper;

    public HttpClient(CertificateTrust.Helper helper){
    }

    public HttpClient(boolean b) {
        mSSLUnverifiedMode = b;
    }

    public void enableUnverifiedMode(CertificateTrust.Helper helper){
        mSSLUnverifiedMode = true;
        mCertificateTrustHelper = helper;
    }

    public void disableUnverifiedMode(){
        mSSLUnverifiedMode = false;
    }

    public SSLContext sslContext() throws IOException {
        if(mSSLContext != null) return mSSLContext;

        try {
            mSSLContext = SSLContext.getInstance("TLS");
            mSSLContext.init(null, new TrustManager[]{new CertificateTrustManager(mCertificateTrustHelper)}, null);
            return mSSLContext;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        throw new IOException("NO TLS");
    }

    CookieManager mCookieManager = new CookieManager();

    public HttpResponse send(String url, Map<String, String> params, ContentBody body) throws IOException {

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

        List<HttpCookie> cookies = mCookieManager.getCookieStore().getCookies();
        if(cookies.size() > 0) {
            String cookieString = "";
            for (int i = 0; i < cookies.size(); i++) {
                String value = ";" + cookies.get(i).toString();
                cookieString += value;
            }
            con.setRequestProperty("Cookie", cookieString.substring(1));
        }
        con.setRequestMethod("POST");

        if(body != null){
            boundary = "===" + System.currentTimeMillis() + "===";
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.setDoOutput(true);
            displayHeaders(con);
            OutputStream out = con.getOutputStream();

            Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                new PrintWriter(new OutputStreamWriter(out, charset),true)
                .append(boundary).append(LINE_FEED)
                .append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"")
                .append(LINE_FEED)
                .append("Content-Type: text/plain; charset=" + charset)
                .append(LINE_FEED).append(LINE_FEED)
                .append(entry.getValue()).append(LINE_FEED).flush();
            }

            while(!body.allChunksWritten()){
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, charset),true);
                writer.append(boundary)
                .append(LINE_FEED)
                .append("Content-Disposition: form-data; name=\"userfile_0\"; filename="+ body.getFilename())
                .append(LINE_FEED)
                .append("Content-Type: " + URLConnection.guessContentTypeFromName(body.getFilename()) + "; charset=" + charset)
                .append(LINE_FEED).append(LINE_FEED).flush();
                body.writeTo(out);
                writer.append(LINE_FEED).flush();
            }

        } else  {
            StringBuilder postData = new StringBuilder();
            Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(entry.getKey(), charset));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(entry.getValue()), charset));
            }
            byte[] postDataBytes = postData.toString().getBytes(charset);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + charset);
            con.setRequestProperty("Content-Length", String.valueOf(postData.length()));

            con.setDoOutput(true);
            //displayHeaders(con);
            OutputStream out = con.getOutputStream();
            out.write(postDataBytes);
        }

        Map<String, List<String>> headerFields = con.getHeaderFields();
        List<String> cookiesHeader = headerFields.get("Set-Cookie");
        if(cookiesHeader != null) {
            for (String cookie : cookiesHeader) {
                mCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
            }
        }
        return  new HttpResponse(con);
    }

    public static void displayHeaders(HttpURLConnection conn){
        System.out.println("---------------REQUEST HEADERS--------------------");
        for (String header : conn.getRequestProperties().keySet()) {
            if (header != null) {
                for (String value : conn.getRequestProperties().get(header)) {
                    System.out.println(header + ":" + value);
                }
            }
        }
        System.out.println("---------------------******-------------------------\n\n");
    }

    public static void responseHeaders(HttpURLConnection con){
        System.out.println("---------------RESPONSE HEADERS--------------------");
        Map<String, List<String>> headers = con.getHeaderFields();
        Iterator it= headers.keySet().iterator();
        System.out.println("HTTP RESPONSE HEADERS");
        while(it.hasNext()){
            String key = (String) it.next();
            System.out.println(key + ": " + headers.get(key).get(0));
        }
        System.out.println("---------------------******-------------------------\n\n");
    }

}
