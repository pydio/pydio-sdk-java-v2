package com.pydio.sdk.core.api.p8;

import com.pydio.sdk.core.common.callback.StringHandler;
import com.pydio.sdk.core.common.errors.Code;
import com.pydio.sdk.core.common.http.BufferedStream;
import com.pydio.sdk.core.utils.io;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class P8Response {
    private int code;
    private ByteArrayOutputStream contentHead;
    private InputStream stream;
    private BufferedStream content;
    private Map<String, List<String>> headers;

    private P8Response() {
    }

    public P8Response(HttpURLConnection con) {
        try {
            code = con.getResponseCode();
        } catch (IOException e) {
            code = Code.con_failed;
        }
        headers = con.getHeaderFields();
        try {
            stream = con.getInputStream();
            code = underlyingCode();
        } catch (IOException ignored) {
        }
    }

    public static P8Response error(int code) {
        P8Response rsp = new P8Response();
        rsp.code = code;
        return rsp;
    }

    private int underlyingCode() {
        final int[] is_required = {Code.ok};
        contentHead = new ByteArrayOutputStream();

        try {
            int read, left = 4096;
            byte[] buffer = new byte[left];

            for (; left > 0; ) {
                read = stream.read(buffer);
                if (read == -1) {
                    break;
                }
                left -= read;
                if (read > 0) {
                    contentHead.write(buffer, 0, read);
                }
            }

            buffer = contentHead.toByteArray();
            String xmlString = new String(Arrays.copyOfRange(buffer, 0, buffer.length), "utf-8");
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            DefaultHandler dh = new DefaultHandler() {
                //<meta http-equiv="refresh" stream="5; URL=http://www.manouvelleadresse.com">
                public boolean tag_repo = false, tag_auth = false, tag_msg = false, tag_meta = false, tag_auth_message = false;
                public String content;
                String xPath;

                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if (tag_repo) {
                        if (qName.equals(xPath)) {
                            is_required[0] = Code.authentication_required;
                            throw new SAXException("auth");
                        }
                        return;
                    } else if (tag_auth) {
                        if (qName.equals("message")) {
                            is_required[0] = Code.authentication_required;
                            throw new SAXException("auth");
                        }
                        return;
                    } else if (tag_msg) {
                        return;
                    } else if (tag_meta) {
                        //TODO: redirection may be
                    }

                    boolean registryPart = qName.equals("ajxp_registry_part") && attributes.getValue("xPath") != null;
                    if (tag_repo = registryPart) {
                        String attr = attributes.getValue("xPath");
                        if (attr != null) {
                            String[] splits = attr.split("/");
                            xPath = splits[splits.length - 1];
                        }
                    }

                    tag_auth = qName.equals("require_auth");
                    tag_msg = qName.equals("message");

                    if (tag_meta = qName.equals("meta")) {
                        String equiv = attributes.getValue("http-equiv");
                        String content = attributes.getValue("xPath");
                        if ("refresh".equals(equiv) && content != null) {
                            int start = content.toLowerCase().indexOf("url=") + 4, end = content.toLowerCase().lastIndexOf("\"");
                            String newUrl = content.substring(start, end);
                            throw new SAXException("redirect=" + newUrl);
                        }
                    }
                }

                public void endElement(String uri, String localName, String qName) throws SAXException {
                    if (tag_repo && xPath != null || (tag_auth && qName.equals("require_auth"))) {
                        is_required[0] = Code.authentication_required;//P8Client.this.auth_step = "LOG-USER";
                        throw new SAXException("auth");
                    } else if (tag_meta && qName.equals("meta")) {
                    }
                }

                public void characters(char ch[], int start, int length) throws SAXException {
                    String str = new String(ch);
                    if (tag_msg) {
                        if (str.toLowerCase().contains("you are not allowed to access")) {
                            is_required[0] = Code.authentication_required; //P8Client.this.auth_step = "RENEW-TOKEN";
                            throw new SAXException("token");
                        }
                    }
                }

                public void endDocument() {
                }
            };
            parser.parse(new InputSource(new StringReader(xmlString)), dh);
            //<meta http-equiv="refresh" stream="5; URL=http://www.manouvelleadresse.com">
        } catch (IOException | ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            String m = e.getMessage();
            if ("auth".equalsIgnoreCase(m)) {
                return Code.authentication_required;
            } else if ("token".equalsIgnoreCase(m)) {
                return Code.authentication_with_captcha_required;
            } else if (m.startsWith("redirect=")) {
//                mHttpRedirectedUrl = m.substring(10);
                return Code.redirect;
            }
        } catch (Exception e) {
            //Log.w("Warning", e.getMessage());
        }
        return is_required[0];
    }

    public InputStream getContent() {
        if (content == null) {
            content = new BufferedStream(contentHead.toByteArray(), stream);
        }
        return content;
    }

    public int code() {
        return code;
    }

    public List<String> getHeaders(String key) {
        return headers.get(key);
    }

    public JSONObject toJSON() {
        return null;
    }

    public Document toXMLDocument() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputStream in = getContent();
            return db.parse(in);
        } catch (Exception e) {
            return null;
        }
    }

    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = null;
        byte[] buffer = new byte[1024];
        try {
            in = getContent();
            int read = 0;
            for (; read != -1; ) {
                read = in.read(buffer);
                if (read > 0) {
                    out.write(buffer, 0, read);
                }
            }
        } catch (IllegalStateException | IOException e) {
            return null;
        } finally {
            io.close(in);
        }

        buffer = out.toByteArray();
        String charset = "UTF-8";
        try {
            return new String(buffer, charset);
        } catch (UnsupportedEncodingException e) {
            return new String(buffer);
        }
    }

    public void lineByLine(String charset, String delimiter, StringHandler h) {
        Scanner sc = new Scanner(getContent(), charset);
        sc.useDelimiter(delimiter);
        for (; ; ) {
            String line = sc.nextLine();
            if (line == null || "".equals(line)) {
                break;
            }
            h.onString(line);
        }
    }

    public int saxParse(DefaultHandler handler) {
        InputStream in = getContent();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            parser.parse(in, handler);
            return Code.ok;
        } catch (Exception e) {
            return Code.unexpected_content;
        } finally {
            io.close(in);
        }
    }
}
