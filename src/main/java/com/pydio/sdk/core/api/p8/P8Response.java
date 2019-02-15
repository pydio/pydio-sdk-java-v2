package com.pydio.sdk.core.api.p8;

import com.pydio.sdk.core.common.callback.StringHandler;
import com.pydio.sdk.core.common.callback.TransferProgressListener;
import com.pydio.sdk.core.common.errors.Code;
import com.pydio.sdk.core.utils.io;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class P8Response {
    private int code;
    private InputStream concatStream;
    private HttpURLConnection con;
    private ByteArrayOutputStream buffered;
    private boolean concatDone;

    public static P8Response error(int code) {
        P8Response rsp = new P8Response();
        rsp.code = code;
        return rsp;
    }

    private P8Response() {
    }

    public P8Response(HttpURLConnection con) {
        concatDone = false;
        this.con = con;
        try {
            parse();
        } catch (IOException e) {
            code = Code.con_failed;
        }
    }

    private void parse() throws IOException {
        this.code = Code.fromHttpStatus(this.con.getResponseCode());
        if (this.code != Code.ok) {
            return;
        }

        buffered = new ByteArrayOutputStream();
        InputStream in = this.con.getInputStream();
        int read, left = 32768;
        byte[] buffer = new byte[left];

        try {
            for (; left > 0; ) {
                read = in.read(buffer);
                if (read == -1) {
                    break;
                }
                left -= read;
                if (read > 0) {
                    buffered.write(buffer, 0, read);
                }
            }

            buffer = buffered.toByteArray();
            String xmlString = new String(Arrays.copyOfRange(buffer, 0, buffer.length), "utf-8");
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            DefaultHandler dh = new DefaultHandler() {

                boolean tag_auth = false;
                boolean tag_msg = false;

                boolean tag_repo = false;
                boolean xpathUser = false;
                boolean repoHasContent = false;

                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if (tag_repo) {
                        repoHasContent = true;
                        return;
                    }

                    if (tag_auth) {
                        if (qName.equals("message")) {
                            P8Response.this.code = Code.authentication_required;
                            throw new SAXException("auth");
                        }
                        return;
                    }

                    if (tag_msg) {
                        return;
                    }

                    boolean registryPart = qName.equals("ajxp_registry_part") && attributes.getValue("xPath") != null;
                    if (registryPart) {
                        //<?xml version="1.0" encoding="UTF-8"?><ajxp_registry_part xPath="user/repositories" ></ajxp_registry_part>
                        tag_repo = true;
                        String attr = attributes.getValue("xPath");
                        if (attr != null) {
                            String[] splits = attr.split("/");
                            xpathUser = "user".equals(splits[0]);
                        }
                    }
                    tag_auth = qName.equals("require_auth");
                    tag_msg = qName.equals("message");
                }

                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if (tag_repo && xpathUser && !repoHasContent) {
                        P8Response.this.code = Code.authentication_required;//P8Client.this.auth_step = "LOG-USER";
                        throw new SAXException("auth");
                    }

                    if (tag_auth && qName.equals("require_auth")) {
                        P8Response.this.code = Code.authentication_required;//P8Client.this.auth_step = "LOG-USER";
                        throw new SAXException("auth");
                    }
                }

                public void characters(char ch[], int start, int length) throws SAXException {
                    String str = new String(ch);
                    if (tag_msg) {
                        if (str.toLowerCase().contains("you are not allowed to access")) {
                            P8Response.this.code = Code.authentication_required; //P8Client.this.auth_step = "RENEW-TOKEN";
                            throw new SAXException("token");
                        }
                    }
                }

                public void endDocument() {
                }
            };
            parser.parse(new InputSource(new StringReader(xmlString)), dh);
        } catch (IOException | ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            String m = e.getMessage();
            if ("auth".equalsIgnoreCase(m)) {
                this.code = Code.authentication_required;
            } else if ("token".equalsIgnoreCase(m)) {
                this.code = Code.authentication_with_captcha_required;
            } else if (m.startsWith("redirect=")) {
                this.code = Code.redirect;
            }
        } catch (Exception ignored) {
        }
    }

    private InputStream getInputStream() {
        if (concatStream == null) {
            try {
                concatStream = new InputStream() {
                    InputStream buffered = new ByteArrayInputStream(P8Response.this.buffered.toByteArray());
                    InputStream in = P8Response.this.con.getInputStream();

                    @Override
                    public int read() throws IOException {
                        int read = buffered.read();
                        if (read == -1) {
                            read = in.read();
                        }
                        return read;
                    }
                };
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return concatStream;
    }

    public InputStream getContent() {
        return getInputStream();
    }

    public int code() {
        return code;
    }

    public List<String> getHeaders(String key) {
        try {
            return this.con.getHeaderFields().get(key);
        } catch (Exception e) {
            return null;
        }
    }

    public JSONObject toJSON() throws ParseException {
        return new JSONObject(toString());
    }

    public Document toXMLDocument() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputStream in = getInputStream();
            return db.parse(in);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = getInputStream();
        try {
            io.pipeRead(in, out);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        byte[] buffer = out.toByteArray();
        String charset = "UTF-8";
        try {
            return new String(buffer, charset);
        } catch (UnsupportedEncodingException e) {
            return new String(buffer);
        }
    }

    public int lineByLine(String charset, String delimiter, StringHandler h) {
        InputStream in = getInputStream();
        Scanner sc = new Scanner(in, charset);
        sc.useDelimiter(delimiter);

        int lineCount = 0;
        while(true) {
            String line;
            try {
                line = sc.nextLine();
            } catch (NoSuchElementException  e){
                return lineCount;
            }

            if (line == null || "".equals(line)) {
                return lineCount;
            }

            lineCount++;
            h.onString(line);
        }
    }

    public int saxParse(DefaultHandler handler) {
        String content = toString();
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

    public long write(OutputStream out, TransferProgressListener progressListener) throws IOException {
        return io.pipeReadWithProgress(getContent(), out, (progress) -> {
            if (progressListener != null) {
                progressListener.onProgress(progress);
            }
        });
    }
}
