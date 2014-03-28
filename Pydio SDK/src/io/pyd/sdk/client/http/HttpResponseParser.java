package io.pyd.sdk.client.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class HttpResponseParser {
	
	
	public static Document getXML(HttpResponse response){
		
		if(response == null) return null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			HttpEntity entity = response.getEntity();			
			if(entity instanceof XMLDocEntity){
				return ((XMLDocEntity) entity).getDoc();
			}
			return db.parse(entity.getContent());
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;	
	}
	
	
	public static String getString(HttpResponse response){
		if(response == null) return null;		
		HttpEntity entity = response.getEntity();			
		InputStream in;
		try {
			in = entity.getContent();
			String content = "";
			byte[] buffer = new byte[1024];
			
			for(int read = 0; (read = in.read(buffer)) != -1;){
				content += new String(Arrays.copyOfRange(buffer, 0, read));
			}
			return content;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
