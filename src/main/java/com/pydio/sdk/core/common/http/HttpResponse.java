package com.pydio.sdk.core.common.http;

import com.pydio.sdk.core.common.callback.StringHandler;
import com.pydio.sdk.core.common.errors.Code;
import com.pydio.sdk.core.utils.ProgressListener;
import com.pydio.sdk.core.utils.io;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class HttpResponse {

    private HttpURLConnection con;

    public HttpResponse(HttpURLConnection con) {
        this.con = con;
    }

    public HttpURLConnection getConnection(){
        return con;
    }

    public InputStream getContent() throws IOException {
        return con.getInputStream();
    }

    public int code() throws IOException {
        return con.getResponseCode();
    }

    public List<String> getHeaders(String key) {
        try {
            return this.con.getHeaderFields().get(key);
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject toJSON() throws ParseException, IOException {
        return new JSONObject(this.getString());
    }

    public Document toXMLDocument() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputStream in = getContent();
            return db.parse(in);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getString() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = getContent();
        io.pipeRead(in, out);

        byte[] buffer = out.toByteArray();
        String charset = "UTF-8";
        try {
            return new String(buffer, charset);
        } catch (UnsupportedEncodingException e) {
            return new String(buffer);
        }
    }

    public int lineByLine(String charset, String delimiter, StringHandler h) throws IOException {
        InputStream in = getContent();
        Scanner sc = new Scanner(in, charset);
        sc.useDelimiter(delimiter);

        int lineCount = 0;
        while(true) {
            String line;
            try {
                line = sc.nextLine();
            } catch (NoSuchElementException e){
                return lineCount;
            }

            if (line == null || "".equals(line)) {
                return lineCount;
            }

            lineCount++;
            h.onString(line);
        }
    }

    public int saxParse(DefaultHandler handler) throws IOException {
        String content = getString();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(new InputSource(new StringReader(content)), handler);
            return Code.ok;
        } catch (Exception e) {
            return Code.unexpected_content;
        }
    }

    public long write(OutputStream out) throws IOException {
        return io.pipeRead(getContent(), out);
    }

    public long write(OutputStream out, ProgressListener progressListener) throws IOException {
        return io.pipeReadWithProgress(getContent(), out, progressListener);
    }
}
