package com.pydio.sdk.core.api.p8;

import com.pydio.sdk.core.ApplicationData;
import com.pydio.sdk.core.api.p8.consts.Param;
import com.pydio.sdk.core.common.errors.Code;
import com.pydio.sdk.core.utils.Params;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.rmi.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * This class handle a session with a pydio server
 *
 * @author pydio
 */
public class P8Client {

    private CookieManager cookies;

    private Configuration config;

    public P8Client(Configuration config) {
        this.cookies = new CookieManager();
        this.config = config;
    }

    public P8Response execute(P8Request request) {
        if (request.method != null) {
            switch (request.method) {
                case Method.get:
                    return get(request);

                case Method.post:
                    return post(request);

                case Method.put:
                    return put(request);
            }
            return null;
        }

        if (request.body == null && request.params == null) {
            return get(request);
        }

        if (request.params == null) {
            return put(request);
        }

        return post(request);
    }

    public P8Response execute(P8Request request, RetryCallback retry, int code) {
        P8Response response = execute(request);
        final int c = response.code();
        if (c == code) {
            P8Request retryRequest = retry.update(request);
            if (retryRequest != null) {
                response = execute(retryRequest);
            }
        }
        return response;
    }

    public String getURL(P8Request request) throws ProtocolException, UnsupportedEncodingException, UnknownHostException {
        String u = this.config.endpoint;
        if (!u.endsWith("/")) {
            u += "/";
        }
        StringBuilder url = new StringBuilder(u);
        if (!url.toString().startsWith("http")) {
            if (this.config.resolver == null) {
                throw new ProtocolException(url.toString());
            }
            try {
                url = new StringBuilder(this.config.resolver.resolve(url.toString(), false));
            } catch (IOException e) {
                throw new UnknownHostException(url.toString());
            }
        }

        url = url.append("index.php?").append(Param.getAction + "=").append(request.action);
        Iterator<Map.Entry<String, String>> it = request.params.get().entrySet().iterator();
        url.append("&");
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            String name = entry.getKey(), value = entry.getValue();
            url.append(name).append("=").append(URLEncoder.encode(value, "utf-8"));
            url.append("&");
        }

        List<HttpCookie> cookies = this.cookies.getCookieStore().getCookies();
        for (HttpCookie c : cookies) {
            url.append("ajxp_sessid=").append(URLEncoder.encode(c.getValue(), "utf-8"));
        }
        return url.toString();
    }

    //**********************************************************************************************
    //              HTTP
    //**********************************************************************************************
    private HttpURLConnection getConnection(String u, String method, boolean ignoreCookies) {
        if (!u.startsWith("http")) {
            if (this.config.resolver == null) {
                return null;
            }
            try {
                u = this.config.resolver.resolve(u, false);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        HttpURLConnection con;

        if (this.config.selfSigned) {
            HttpsURLConnection c;
            try {
                c = (HttpsURLConnection) new URL(u).openConnection();
            } catch (IOException e) {
                e.printStackTrace();
                //todo : return detailed
                return null;
            }
            c.setSSLSocketFactory(config.sslContext.getSocketFactory());
            c.setHostnameVerifier(config.hostnameVerifier);
            con = c;
        } else {

            try {
                con = (HttpURLConnection) new URL(u).openConnection();
            } catch (IOException e) {
                e.printStackTrace();
                //todo : return detailed
                return null;
            }
        }

        try {
            con.setRequestMethod(method);
        } catch (ProtocolException e) {
            e.printStackTrace();
            //todo : return detailed
            return null;
        }
        if (!ignoreCookies) {
            List<HttpCookie> cookies = this.cookies.getCookieStore().getCookies();
            if (cookies.size() > 0) {
                StringBuilder cookieString = new StringBuilder();
                for (int i = 0; i < cookies.size(); i++) {
                    String value = ";" + cookies.get(i).toString();
                    cookieString.append(value);
                }
                con.setRequestProperty("Cookie", cookieString.substring(1));
            }
        }

        if (this.config.userAgent != null) {
            con.setRequestProperty("User-Agent", this.config.userAgent);
        } else {
            con.setRequestProperty("User-Agent", "Pydio-Native-" + ApplicationData.name + " " + ApplicationData.version + "." + ApplicationData.versionCode);
        }
        con.setDoInput(true);
        return con;
    }

    private P8Response get(P8Request request) {
        String url = config.endpoint;
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        url = url + "?" + Param.getAction + "=" + request.action;

        HttpURLConnection con = getConnection(url, Method.get, request.ignoreCookies);
        if (con == null) {
            return P8Response.error(Code.con_failed);
        }

        P8Response response = new P8Response(con);

        Map<String, List<String>> headerFields = con.getHeaderFields();
        List<String> cookiesHeader = headerFields.get("Set-Cookie");
        if (cookiesHeader != null) {
            for (String cookie : cookiesHeader) {
                List<HttpCookie> cs = HttpCookie.parse(cookie);
                for (HttpCookie hc : cs) {
                    cookies.getCookieStore().add(null, hc);
                }
            }
        }
        return response;
    }

    private P8Response post(P8Request request) {
        String url = config.endpoint;
        if (!url.endsWith("/")) {
            url = url + "/";
        }

        HttpURLConnection con = getConnection(url, Method.post, request.ignoreCookies);
        if (con == null) {
            return P8Response.error(Code.con_failed);
        }

        if (request.params == null) {
            request.params = Params.create(Param.getAction, request.action);
        } else {
            request.params.set(Param.getAction, request.action);
        }

        con.setDoOutput(true);

        List<HttpCookie> cookies = this.cookies.getCookieStore().getCookies();
        if (cookies.size() > 0) {
            StringBuilder cookieString = new StringBuilder();
            for (int i = 0; i < cookies.size(); i++) {
                String value = ";" + cookies.get(i).toString();
                cookieString.append(value);
            }
            con.setRequestProperty("Cookie", cookieString.substring(1));
        }
        con.setRequestProperty("User-Agent", "Pydio-Native-" + ApplicationData.name + " " + ApplicationData.version + "." + ApplicationData.versionCode);

        String utf8 = "utf-8";
        String LF = "\r\n";
        String DLF = LF + LF;

        try {
            if (request.body != null) {
                String boundary = "----" + System.currentTimeMillis();
                con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                ByteArrayOutputStream partHeaderBuffer = new ByteArrayOutputStream();
                Iterator<Map.Entry<String, String>> it = request.params.get().entrySet().iterator();

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

                partHeaderBuffer.write(("Content-Disposition: form-data; name=\"userfile_0\"; filename=" + URLEncoder.encode(request.body.getFilename(), utf8)).getBytes());
                partHeaderBuffer.write(LF.getBytes());
                partHeaderBuffer.write(("Content-Type: " + request.body.getContentType()).getBytes());

                partHeaderBuffer.write(DLF.getBytes());
                byte[] partHeaderBytes = partHeaderBuffer.toByteArray();
                byte[] lastBoundaryBytes = (LF + "--" + boundary + "--" + LF).getBytes();

                int rest = partHeaderBytes.length + lastBoundaryBytes.length;
                int contentSupposedBodyLength = (int) (request.body.maxChunkSize() - rest);
                int contentBodyActualLength = (int) Math.min(contentSupposedBodyLength, request.body.available());

                con.setFixedLengthStreamingMode(contentBodyActualLength + rest);

                OutputStream out = con.getOutputStream();
                out.write(partHeaderBytes);
                request.body.writeTo(out, contentBodyActualLength);
                out.write(lastBoundaryBytes);

            } else {
                StringBuilder postData = new StringBuilder();

                for (Map.Entry<String, String> entry : request.params.get().entrySet()) {
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

            P8Response response = new P8Response(con);
            Map<String, List<String>> headerFields = con.getHeaderFields();
            List<String> cookiesHeader = headerFields.get("Set-Cookie");
            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    List<HttpCookie> cs = HttpCookie.parse(cookie);
                    for (HttpCookie hc : cs) {
                        this.cookies.getCookieStore().add(null, hc);
                    }
                }
            }
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return P8Response.error(Code.con_failed);
        }
    }

    private P8Response put(P8Request request) {
        String url;
        try {
            url = getURL(request);
        } catch (IOException e) {
            e.printStackTrace();
            return P8Response.error(Code.con_failed);
        }

        HttpURLConnection con = getConnection(url, Method.put, request.ignoreCookies);
        if (con == null) {
            return P8Response.error(Code.con_failed);
        }
        con.setDoOutput(true);

        con.setRequestProperty("User-Agent", "Pydio-Native-" + ApplicationData.name + " " + ApplicationData.version + "." + ApplicationData.versionCode);
        con.setRequestProperty("Content-Type", "application/octet-stream");
        try {
            OutputStream out = con.getOutputStream();
            request.body.writeTo(out);
            P8Response response = new P8Response(con);
            Map<String, List<String>> headerFields = con.getHeaderFields();
            List<String> cookiesHeader = headerFields.get("Set-Cookie");
            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    List<HttpCookie> cs = HttpCookie.parse(cookie);
                    for (HttpCookie hc : cs) {
                        cookies.getCookieStore().add(null, hc);
                    }
                }
            }
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return P8Response.error(Code.con_failed);
        }
    }
}