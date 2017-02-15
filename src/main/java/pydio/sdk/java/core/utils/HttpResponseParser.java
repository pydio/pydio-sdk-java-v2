package pydio.sdk.java.core.utils;

import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import pydio.sdk.java.core.http.HttpEntity;
import pydio.sdk.java.core.http.HttpResponse;

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
	public static Document getXML(HttpResponse response) throws IOException {
		if(response == null) return null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			HttpEntity entity = response.getEntity();
			InputStream in = entity.getContent();
			return db.parse(in);
		}catch (Exception e){
			throw new IOException(e.getMessage(), e);
		}
	}
	
	/**
	 * This method parse the response content
	 * @param response an HtrtpResponse object
	 * @return a String object
	 */
	public static String getString(HttpResponse response) throws IOException {
		if(response == null) return null;		
		HttpEntity entity = response.getEntity();			
		InputStream in = null;
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
		} finally {
			try {in.close();} catch (IOException e) {}
		}
	}

}
