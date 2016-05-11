package pydio.sdk.java.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import javax.security.cert.X509Certificate;

import pydio.sdk.java.model.Node;
import pydio.sdk.java.model.NodeDiff;
import pydio.sdk.java.security.CertificateTrust;
import pydio.sdk.java.security.CertificateTrustManager;
import pydio.sdk.java.utils.HttpChunkedResponseInputStream;
import pydio.sdk.java.utils.HttpResponseParser;
import pydio.sdk.java.utils.Pydio;

/**
 * Created by jabar on 12/04/2016.
 */
public class HttpClient {

    private String boundary;
    private static final String LINE_FEED = "\r\n";
    private static final String DOUBLE_LINE_FEED = "\r\n\r\n";

    private String CHARSET_UTF8 = "utf-8";
    private String CHARSET_ASCII = "US-ASCII";
    SSLContext mSSLContext;
    boolean mSSLUnverifiedMode;
    CertificateTrust.Helper mCertificateTrustHelper;

    String mAjxpCookie = null;
    boolean rawMode = false;

    //public static final int HTTP_DATA_BUFFER_MAX_SIZE = 1048576;
    public static final int HTTP_DATA_BUFFER_MAX_SIZE = 100*1024*1024;

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
        if(rawMode){
            return rawSend(URI.create(url), params, body);
        }


        /*if(body != null){
            Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                String name = entry.getKey(), value = entry.getValue();
                url += ("&" + name + "=" + URLEncoder.encode(value, CHARSET_UTF8));
            }
        }*/

        HttpURLConnection con;
        System.out.println(url);
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

        con.setRequestProperty("User-Agent", "Pydio Android Client");

        if(body != null){



            int totalLength = 0;

            boundary = "----" + System.currentTimeMillis();
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary );
            //con.setRequestProperty("Content-type", body.getContentType());


            ByteArrayOutputStream partHeaderBuffer = new ByteArrayOutputStream();
            Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();

            partHeaderBuffer.write(("--" + boundary).getBytes());
            partHeaderBuffer.write(LINE_FEED.getBytes());

            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                String name = entry.getKey(), value = entry.getValue();
                partHeaderBuffer.write(("Content-Disposition: form-data; name=\"" + URLEncoder.encode(name, CHARSET_UTF8) + "\"").getBytes());
                partHeaderBuffer.write(LINE_FEED.getBytes());
                partHeaderBuffer.write(("Content-Type: text/plain").getBytes());
                partHeaderBuffer.write(DOUBLE_LINE_FEED.getBytes());
                partHeaderBuffer.write(value.getBytes());
                partHeaderBuffer.write(LINE_FEED.getBytes());
                partHeaderBuffer.write(("--" + boundary).getBytes());
                partHeaderBuffer.write(LINE_FEED.getBytes());
            }

            /*partHeaderBuffer.write(boundary.getBytes());
            partHeaderBuffer.write(LINE_FEED.getBytes());*/
            partHeaderBuffer.write(("Content-Disposition: form-data; name=\"userfile_0\"; filename=" + URLEncoder.encode(body.getFilename(), CHARSET_UTF8)).getBytes());
            partHeaderBuffer.write(LINE_FEED.getBytes());
            partHeaderBuffer.write(("Content-Type: " + URLConnection.guessContentTypeFromName(body.getFilename())).getBytes());
            partHeaderBuffer.write(DOUBLE_LINE_FEED.getBytes());
            byte[] partHeaderBytes = partHeaderBuffer.toByteArray();
            byte[] lastBoundaryBytes = (LINE_FEED + "--" + boundary + "--" + LINE_FEED).getBytes();

            int rest = partHeaderBytes.length + lastBoundaryBytes.length;
            int contentSupposedBodyLength = (int) (body.maxChunkSize() - rest);
            int contentBodyActualLength = (int) Math.min(contentSupposedBodyLength, body.available());
            System.out.println("BODY CONTENT LENGTH : " + (contentBodyActualLength + rest) );

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
                    mCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                }
            }

            int code = response.code();
            if(code == 200 && !body.allChunksWritten()){
                NodeDiff diff = NodeDiff.create(HttpResponseParser.getXML(response));
                if(diff.added != null){
                    Node node = diff.added.get(0);
                    String label = node.label();
                    if(!label.equals(body.getFilename())){
                        body.setFilename(label);
                    }
                }
                params.put(Pydio.PARAM_APPEND_TO_URLENCODED_PART, body.getFilename());
                send(url, params, body);
            }
            return response;

        } else  {
            StringBuilder postData = new StringBuilder();
            Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
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
                    mCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                }
            }
            return  new HttpResponse(con);
        }
    }

    public HttpResponse rawSend(URI uri, Map<String, String> params, ContentBody body) throws IOException {
        Socket socket;
        String scheme = uri.getScheme();
        //String host = uri.getHost();
        int port = uri.getPort();
        String asciiUri = uri.toASCIIString();
        String host = uri.getHost();
        String path = uri.getPath();
        String resourcePath = asciiUri.substring(asciiUri.indexOf(path));

        if(port == -1){
            port = 80;
        }

        boolean https = "https".equals(scheme);
        if(!https) {
            socket = SocketFactory.getDefault().createSocket(host, port);
        } else {
            if(mSSLUnverifiedMode){
                socket = sslContext().getSocketFactory().createSocket(host, 443);
            } else {
                socket = SSLSocketFactory.getDefault().createSocket(host, 443);
            }

            SSLSocket sslSocket = (SSLSocket)socket;
            SNIHostName serverName = new SNIHostName(host);
            List<SNIServerName> serverNames = new ArrayList<>(1);
            serverNames.add(serverName);
            SSLParameters p = sslSocket.getSSLParameters();
            p.setServerNames(serverNames);
            sslSocket.setSSLParameters(p);
            sslSocket.startHandshake();
            //verify the certificate chains
            X509Certificate[] certChain = sslSocket.getSession().getPeerCertificateChain();
        }

        ByteArrayOutputStream headersOutputBuffer = new ByteArrayOutputStream();
        PrintWriter headersWriter = new PrintWriter(new OutputStreamWriter(headersOutputBuffer, "US-ASCII"), true);

        String method = (params == null || params.size() == 0) && body == null ? "GET " : "POST ";
        headersWriter.append(method + resourcePath + " HTTP/1.1" + LINE_FEED);
        headersWriter.append("Host: " + host + LINE_FEED);
        headersWriter.append("User-Agent: Pydio Http Client" + LINE_FEED);
        headersWriter.append("Accept: */*" + LINE_FEED);

        if(mAjxpCookie != null){
            headersWriter.append("Cookie: " + mAjxpCookie + LINE_FEED);
        }

        if(body != null){
            int dataLineFeedCount = 0;
            boundary = "----" + System.currentTimeMillis();
            headersWriter.append("Content-Type: multipart/form-data; boundary=" + boundary + LINE_FEED);
            headersWriter.flush();

            ByteArrayOutputStream partsOutputBuffer = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(partsOutputBuffer, CHARSET_UTF8), true);

            Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
            writer.append("--" + boundary).append(LINE_FEED);
            dataLineFeedCount++;

            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                String name = entry.getKey(), value = entry.getValue();
                writer.append("Content-Disposition: form-data; name=\"" + URLEncoder.encode(name, CHARSET_UTF8) + "\"").append(LINE_FEED);
                writer.append("Content-Type: text/plain").append(DOUBLE_LINE_FEED);
                writer.append(value).append(LINE_FEED);
                writer.append("--" + boundary).append(LINE_FEED);
                dataLineFeedCount += 5;
            }


            writer.append("Content-Disposition: form-data; name=\"userfile_0\"; filename=" + URLEncoder.encode(body.getFilename(), CHARSET_UTF8)).append(LINE_FEED);
            writer.append("Content-Type: " + body.getContentType()).append(DOUBLE_LINE_FEED).flush();
            //Calculating the content length
            byte[] partsBytes = partsOutputBuffer.toByteArray();
            byte[] lastLineBytes = (LINE_FEED + "--" + boundary + "--" + LINE_FEED).getBytes();
            dataLineFeedCount += 5;

            long rest = partsBytes.length + lastLineBytes.length;
            long contentBodyLength = Math.min((int) (body.maxChunkSize() - rest), body.available());

            //Last header
            headersWriter.append("Content-Length: " + String.valueOf( rest + contentBodyLength) + DOUBLE_LINE_FEED).flush();
            byte[] headersBytes = headersOutputBuffer.toByteArray();

            OutputStream socketOutput = socket.getOutputStream();
            socketOutput.write(headersBytes);
            socketOutput.write(partsBytes);
            //no buffering for upload content
            body.writeTo(socketOutput, contentBodyLength);
            socketOutput.write(lastLineBytes);


            //System.out.println(new String(headersBytes));
            //System.out.println(new String(partsBytes));
            //System.out.println("CONTENT\n");
            //System.out.println(new String(lastLineBytes));

            socket.shutdownOutput();
            HttpResponse response = parseResponse(socket.getInputStream());
            List<String> cookiesHeader = response.getHeaders("Set-Cookie");
            if (cookiesHeader != null) {
                mAjxpCookie = cookiesHeader.get(0);
            }

            int code = response.code();

            if(code == 200 && !body.allChunksWritten()){
                NodeDiff diff = NodeDiff.create(HttpResponseParser.getXML(response));
                if(diff.added != null){
                    Node node = diff.added.get(0);
                    String label = node.label();
                    if(!label.equals(body.getFilename())){
                        body.setFilename(label);
                    }
                }
                params.put(Pydio.PARAM_APPEND_TO_URLENCODED_PART, body.getFilename());
                rawSend(uri, params, body);
            }
            return response;
        }

        if(params != null && params.size() > 0) {
            StringBuilder postData = new StringBuilder();
            Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(entry.getKey(), CHARSET_UTF8));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(entry.getValue()), CHARSET_UTF8));
            }
            byte[] postDataBytes = postData.toString().getBytes(CHARSET_UTF8);
            int contentLength = postDataBytes.length;
            headersWriter.append("Content-Type: application/x-www-form-urlencoded; charset=" + CHARSET_UTF8 + LINE_FEED);
            headersWriter.append("Content-Length: " + String.valueOf(contentLength) + DOUBLE_LINE_FEED).flush();

            byte[] headersBytes = headersOutputBuffer.toByteArray();
            OutputStream socketOutput = socket.getOutputStream();
            socketOutput.write(headersBytes);
            socketOutput.write(postDataBytes);

            HttpResponse response = parseResponse(socket.getInputStream());
            List<String> cookiesHeader = response.getHeaders("Set-Cookie");
            if (cookiesHeader != null) {
                mAjxpCookie = cookiesHeader.get(0);
            }
            return response;
        }

        headersWriter.append(LINE_FEED).flush();
        OutputStream socketOutput = socket.getOutputStream();


        byte[] headersBytes = headersOutputBuffer.toByteArray();
        socketOutput.write(headersBytes);
        //System.out.println(new String(headersBytes, CHARSET_ASCII));


        socket.shutdownOutput();
        HttpResponse response = parseResponse(socket.getInputStream());
        List<String> cookiesHeader = response.getHeaders("Set-Cookie");
        if (cookiesHeader != null) {
            mAjxpCookie = cookiesHeader.get(0);
        }

        return response;
    }

    public HttpResponse parseResponse(InputStream in) throws IOException {
        try {
            byte charRead;
            StringBuffer sb = new StringBuffer();

            while (true) {
                sb.append((char) (charRead = (byte) in.read()));
                if (charRead == -1) break;
                if ((char) charRead == '\r') {
                    sb.append((char) in.read());
                    charRead = (byte) in.read();
                    if (charRead == '\r') {
                        sb.append((char) in.read());
                        break;
                    } else {
                        sb.append((char) charRead);
                    }
                }
            }

            boolean chunked = false;
            String[] headersArray = sb.toString().split("\r\n");
            //System.out.println("Response headers");
            //System.out.println(headersArray[0]);
            Map<String, List<String>> headers = new HashMap<>();
            for (int i = 1; i < headersArray.length - 1; i++) {
                String[] splits = headersArray[i].split(": ");
                chunked |= "Transfer-Encoding".equals(splits[0]);
                List<String> value = Arrays.asList(splits[1].split(";"));
                headers.put(splits[0], value);
                //System.out.println(headersArray[i]);
            }
            //System.out.println("\n");

            int code = Integer.parseInt(headersArray[0].split(" ")[1]);
            if(chunked){
                return new HttpResponse(code, headers, new HttpChunkedResponseInputStream(in));
            } else {
                return new HttpResponse(code, headers, in);
            }
        }catch (Exception e){
            throw new IOException("Failed to parse the response", e);
        }
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
