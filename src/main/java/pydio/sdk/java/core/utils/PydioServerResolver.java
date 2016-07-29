package pydio.sdk.java.core.utils;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilderFactory;

import pydio.sdk.java.core.model.ResolutionServer;

/**
 * Created by jabar on 27/07/2016.
 */
public class PydioServerResolver implements ServerResolver {
    private int api_version = 1;
    private String databaseAddress = "https://api.pyd.io/endpoints/index.php";
    private String apikey = "zPyDirgrg48ZEFdf424e2dW";
    private String apisecret = "gsOPydo13sfDRPOKE448769";

    @Override
    public void resolve(ResolutionServer server) throws IOException {
        try{
            String uuid = UUID.randomUUID().toString();
            String seed = uuid.substring(0, 5);
            String auth = apikey + ":" + seed + ":" + md5(seed + apisecret);
            String url = databaseAddress + "?version=" + String.valueOf(api_version)
                    + "&auth=" + auth
                    + "&method=get_customer_info"
                    + "&customer_id="+ server.clientID();


            URL u = new URL(url);
            InputStream is = u.openConnection().getInputStream();
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            doc.getDocumentElement().normalize();

            if (doc.getDocumentElement().getNodeName().equals("error")) {
                return;
            }

            Node s = doc.getElementsByTagName("support").item(0);
            server.mSupport = new Properties();
            parse(s, server.mSupport);

            NodeList endpoints = doc.getElementsByTagName("endpoint");
            for(int i = 0; i < endpoints.getLength(); i++) {
                parse(endpoints.item(0), server.mEndpoints[i]);
            }

            Node v = doc.getElementsByTagName("vanity").item(0);
            server.mVanity = new Properties();
            parse(v, server.mVanity);

            InputStream splash_is = null;
            try {
                u = new URL(server.mVanity.getProperty("splash_image"));
                splash_is = u.openConnection().getInputStream();
                int read = 0;
                server.mImage = new ByteArrayOutputStream();
                byte[] buffer = new byte[16384];
                while((read = splash_is.read(buffer)) != -1){
                    server.mImage.write(buffer, 0, read);
                }
            }catch (Exception  e){} finally {
                if(splash_is != null) try{splash_is.close();} catch (IOException e){}
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void parse(Node node, Properties properties) {
        node.normalize();
        NodeList children = node.getChildNodes();

        if (node.hasAttributes()) {
            NamedNodeMap map = node.getAttributes();
            for (int i = 0; i < map.getLength(); i++) {
                Attr at = (Attr) map.item(i);
                String attrName = at.getNodeName();
                String attrValue = at.getNodeValue();
                properties.setProperty(attrName, attrValue);
            }
        }

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            if (children.getLength() > 0) {
                String name = node.getNodeName();
                for (int i = 0; i < children.getLength(); i++) {
                    Node n = children.item(i);
                    n.normalize();
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        parse(children.item(i), properties);
                    } else if (n.getNodeType() == Node.TEXT_NODE && n.getTextContent() != null && !n.getTextContent().startsWith("\n")) {
                        properties.setProperty(node.getNodeName(), n.getTextContent());
                    } else if (n.getNodeType() == Node.CDATA_SECTION_NODE) {
                        properties.setProperty(name + "_data", n.getNodeValue());
                    }
                }
            }
        }

    }

    private final String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
