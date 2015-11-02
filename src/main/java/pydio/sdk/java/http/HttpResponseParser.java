package pydio.sdk.java.http;



import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import pydio.sdk.java.utils.Pydio;

/**
 * 
 * @author pydio
 *
 */
public class HttpResponseParser {
	
	/**
	 * This method parse the response content 
	 * @param response an HttpResposne object
	 * @return a XML document object
	 */
	public static Document getXML(HttpResponse response){
		
		if(response == null) return null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			HttpEntity entity = response.getEntity();			
			if(entity instanceof XMLDocEntity){
				return ((XMLDocEntity) entity).getDoc();
			}
			InputStream in = entity.getContent();
			return db.parse(in);
			
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
	
	/**
	 * This method parse the response content
	 * @param response an HtrtpResponse object
	 * @return a String object
	 */
	public static String getString(HttpResponse response){
		if(response == null) return null;		
		HttpEntity entity = response.getEntity();			
		InputStream in;
		StringBuilder sb = new StringBuilder();
		int bufsize = Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE;
		try {
			in = entity.getContent();
			byte[] buffer = new byte[bufsize];
			
			for(int read = 0; (read = in.read(buffer)) != -1;){
				sb.append(new String(Arrays.copyOfRange(buffer, 0, read)));
			}
			return sb.toString();
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
