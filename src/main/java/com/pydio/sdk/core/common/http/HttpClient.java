package com.pydio.sdk.core.common.http;

import com.pydio.sdk.core.ApplicationData;
import com.pydio.sdk.core.utils.Params;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class HttpClient {

    public static HttpResponse request(HttpRequest request) throws IOException {
        switch (request.getMethod()){
            case Method.PUT:
                return put(request);

            case Method.POST:
                return post(request);

            case Method.GET:
                return get(request);

            default:
                throw new IOException("method not supported yet");
        }
    }

    private static HttpURLConnection getConnection(HttpRequest requestData) throws IOException {
        String u = requestData.getEndpoint();

        if (!u.startsWith("http")) {
            if (requestData.getResolver() == null) {
                throw new IOException("unknown scheme");
            }
            u = requestData.getResolver().resolve(u, false);
        }

        HttpURLConnection con;
        if (requestData.isSelfSigned()) {
            HttpsURLConnection c = (HttpsURLConnection) new URL(u).openConnection();
            c.setSSLSocketFactory(requestData.getSslContext().getSocketFactory());
            c.setHostnameVerifier(requestData.getHostnameVerifier());
            con = c;
        } else {
            con = (HttpURLConnection) new URL(u).openConnection();
        }

        con.setRequestMethod(requestData.getMethod());

        if (requestData.getCookies() != null) {
            List<HttpCookie> cookies = requestData.getCookies().getCookieStore().getCookies();
            if (cookies.size() > 0) {
                StringBuilder cookieString = new StringBuilder();
                for (int i = 0; i < cookies.size(); i++) {
                    String value = ";" + cookies.get(i).toString();
                    cookieString.append(value);
                }
                con.setRequestProperty("Cookie", cookieString.substring(1));
            }
        }

        if (requestData.getUserAgent() != null) {
            con.setRequestProperty("User-Agent", requestData.getUserAgent());
        }

        Params params = requestData.getHeaders();
        if (params != null) {
            for (Map.Entry<String, String> stringStringEntry : requestData.getHeaders().get().entrySet()) {
                con.setRequestProperty(stringStringEntry.getKey(), stringStringEntry.getValue());
            }
        }

        return con;
    }

    private static HttpResponse put(HttpRequest requestData) throws IOException {
        HttpURLConnection con = getConnection(requestData);
        con.setDoOutput(true);

        con.setRequestProperty("User-Agent", "Pydio-Native-" + ApplicationData.name + " " + ApplicationData.version + "." + ApplicationData.versionCode);
        con.setRequestProperty("Content-Type", "application/octet-stream");

        OutputStream out = con.getOutputStream();
        requestData.getContentBody().writeTo(out);

        Map<String, List<String>> headerFields = con.getHeaderFields();
        List<String> cookiesHeader = headerFields.get("Set-Cookie");
        if (cookiesHeader != null) {
            if (requestData.getCookies() == null) {
                requestData.setCookies(new CookieManager());
            }

            for (String cookie : cookiesHeader) {
                List<HttpCookie> cs = HttpCookie.parse(cookie);
                for (HttpCookie hc : cs) {
                    requestData.getCookies().getCookieStore().add(null, hc);
                }
            }
        }
        return new HttpResponse(con);
    }

    private static HttpResponse get(HttpRequest requestData) throws IOException {
        HttpURLConnection con = getConnection(requestData);
        Map<String, List<String>> headerFields = con.getHeaderFields();
        List<String> cookiesHeader = headerFields.get("Set-Cookie");
        if (cookiesHeader != null) {
            if (requestData.getCookies() == null) {
                requestData.setCookies(new CookieManager());
            }

            for (String cookie : cookiesHeader) {
                List<HttpCookie> cs = HttpCookie.parse(cookie);
                for (HttpCookie hc : cs) {
                    requestData.getCookies().getCookieStore().add(null, hc);
                }
            }
        }

        con.connect();
        return new HttpResponse(con);
    }

    private static HttpResponse post(HttpRequest requestData) throws IOException {

        final String utf8 = "utf-8";
        final String LF = "\r\n";
        final String DLF = LF + LF;

        HttpURLConnection con = getConnection(requestData);
        con.setDoOutput(true);

        if (requestData.getContentBody() != null) {
            String boundary = "----" + System.currentTimeMillis();
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            ByteArrayOutputStream partHeaderBuffer = new ByteArrayOutputStream();
            Iterator<Map.Entry<String, String>> it = requestData.getParams().get().entrySet().iterator();

            partHeaderBuffer.write(("--" + boundary).getBytes());
            partHeaderBuffer.write(LF.getBytes());

            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                String name = entry.getKey(), value = entry.getValue();
                partHeaderBuffer.write(("Content-Disposition: form-data; name=\"" + URLEncoder.encode(name, utf8) + "\"").getBytes());
                partHeaderBuffer.write(LF.getBytes());
                partHeaderBuffer.write(("Content-Type: text/plain; charset=" + utf8).getBytes());
                partHeaderBuffer.write(DLF.getBytes());
                partHeaderBuffer.write(value.getBytes(utf8));
                partHeaderBuffer.write(LF.getBytes());
                partHeaderBuffer.write(("--" + boundary).getBytes());
                partHeaderBuffer.write(LF.getBytes());
            }

            partHeaderBuffer.write(("Content-Disposition: form-data; name=\"userfile_0\"; filename=" + URLEncoder.encode(requestData.getContentBody().getFilename(), utf8)).getBytes());
            partHeaderBuffer.write(LF.getBytes());
            partHeaderBuffer.write(("Content-Type: " + requestData.getContentBody().getContentType()).getBytes());

            partHeaderBuffer.write(DLF.getBytes());
            byte[] partHeaderBytes = partHeaderBuffer.toByteArray();
            byte[] lastBoundaryBytes = (LF + "--" + boundary + "--" + LF).getBytes();

            int rest = partHeaderBytes.length + lastBoundaryBytes.length;
            int contentSupposedBodyLength = (int) (requestData.getContentBody().maxChunkSize() - rest);
            int contentBodyActualLength = (int) Math.min(contentSupposedBodyLength, requestData.getContentBody().available());

            con.setFixedLengthStreamingMode(contentBodyActualLength + rest);

            OutputStream out = con.getOutputStream();
            out.write(partHeaderBytes);
            requestData.getContentBody().writeTo(out, contentBodyActualLength);
            out.write(lastBoundaryBytes);

        } else {
            StringBuilder postData = new StringBuilder();

            for (Map.Entry<String, String> entry : requestData.getParams().get().entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(entry.getKey(), utf8));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(entry.getValue()), utf8));
            }

            byte[] postDataBytes = postData.toString().getBytes(utf8);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + utf8);
            con.setRequestProperty("Content-Length", String.valueOf(postData.length()));
            OutputStream out = con.getOutputStream();
            out.write(postDataBytes);
        }

        Map<String, List<String>> headerFields = con.getHeaderFields();
        List<String> cookiesHeader = headerFields.get("Set-Cookie");
        if (cookiesHeader != null) {
            if (requestData.getCookies() == null) {
                requestData.setCookies(new CookieManager());
            }

            for (String cookie : cookiesHeader) {
                List<HttpCookie> cs = HttpCookie.parse(cookie);
                for (HttpCookie hc : cs) {
                    requestData.getCookies().getCookieStore().add(null, hc);
                }
            }
        }

        return new HttpResponse(con);
    }
}
