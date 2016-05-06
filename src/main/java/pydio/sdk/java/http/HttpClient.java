package pydio.sdk.java.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.text.ParseException;
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
import pydio.sdk.java.utils.HttpResponseParser;
import pydio.sdk.java.utils.Log;

/**
 * Created by jabar on 12/04/2016.
 */
public class HttpClient {

    private String boundary;
    private static final String LINE_FEED = "\r\n";
    private static final String DOUBLE_LINE_FEED = "\r\n\r\n";
    private String charset = "utf-8";
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
        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("User-Agent", "Pydio Android Client");

        if(body != null){
            boundary = "*****" + System.currentTimeMillis() + "*****";
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            OutputStream out = con.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, charset), true);
            Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
            writer.append("--" + boundary).append(LINE_FEED);
            System.out.print("--" + boundary + LINE_FEED);

            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                String name = entry.getKey(), value = entry.getValue();
                writer.append("Content-Disposition: form-data; name=\"" + name + "\"").append(LINE_FEED);
                writer.append("Content-Type: text/plain; charset=").append(charset).append(DOUBLE_LINE_FEED);
                writer.append(value).append(LINE_FEED).flush();
                writer.append("--" + boundary).append(LINE_FEED);
            }
            while(body.isChunked() && !body.allChunksWritten()){
                writer.append("Content-Disposition: form-data; name=\"userfile_0\"; filename=" + body.getFilename()).append(LINE_FEED);
                writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
                writer.append("Content-Type: " + body.getContentType()).append(DOUBLE_LINE_FEED).flush();

                out.flush();
                body.writeTo(out);
                out.flush();

                writer.append(LINE_FEED);
                writer.append("--" + boundary).append(LINE_FEED).flush();
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

    public String multipartRequest(String urlTo, String post, String filepath, String filefield, int maxBufferSize) throws ParseException, IOException {
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        InputStream inputStream = null;

        String twoHyphens = "--";
        String boundary =  "*****"+Long.toString(System.currentTimeMillis())+"*****";
        String lineEnd = "\r\n";

        String result = "";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        //int maxBufferSize = 1*1024*1024;

        String[] q = filepath.split("/");
        int idx = q.length - 1;

        try {
            File file = new File(filepath);
            FileInputStream fileInputStream = new FileInputStream(file);

            URL url = new URL(urlTo);
            connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);

            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + filefield + "\"; filename=\"" + q[idx] +"\"" + lineEnd);
            outputStream.writeBytes("Content-Type: image/jpeg" + lineEnd);
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while(bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);

            // Upload POST Data
            String[] posts = post.split("&");
            int max = posts.length;
            for(int i=0; i<max;i++) {
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                String[] kv = posts[i].split("=");
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + kv[0] + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: text/plain"+lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(kv[1]);
                outputStream.writeBytes(lineEnd);
            }

            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            inputStream = connection.getInputStream();
            result = this.convertStreamToString(inputStream);

            fileInputStream.close();
            inputStream.close();
            outputStream.flush();
            outputStream.close();

            return result;
        } catch(Exception e) {
            //Log.e("MultipartRequest","Multipart Form Upload Error");
            e.printStackTrace();
            return "error";
        }
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
// see http://www.androidsnippets.com/multipart-http-requests

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
