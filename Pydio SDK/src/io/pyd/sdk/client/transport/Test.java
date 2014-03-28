package io.pyd.sdk.client.transport;

import io.pyd.sdk.client.auth.CommandlineCredentialsProvider;
import io.pyd.sdk.client.model.Message;
import io.pyd.sdk.client.model.Node;
import io.pyd.sdk.client.model.NodeFactory;
import io.pyd.sdk.client.model.ServerNode;

import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class Test {
	
	
	
	@SuppressWarnings("resource")
	public static void main(String[] args){
		
		/*		 a simple example
			*******************************************
			action :  ls&tmp_repository_id=1
			parameter 1 name :  options
			parameter 1 value :  al
			...
			parameter n name :  . (end of parameters list)
			output type :  xml for instance
			
			
			จจจจจจจจจจจจจจAuthenticationจจจจจจจจจจจจจจจจจจ
			login    :yourlogin
			password :yourpassword
			จจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจจ
		 */
		
		ServerNode server = (ServerNode) NodeFactory.createNode(Node.TYPE_SERVER);
		server.setHost("192.168.0.181");
		server.setPath("/test");
		server.setProtocol("http");
		server.setLegacy(false);		
		SessionTransport session = new SessionTransport(server, new CommandlineCredentialsProvider());
		
		
		for(;;){		
			Map<String, String> params = new HashMap<String, String>();
			System.out.println("\n\n*******************************************");
			System.out.print("action :  ");
			String action = new Scanner(System.in).nextLine();
			
			for(int i = 1;;i++){				
				System.out.print("parameter "+i+" name :  ");
				String par_name = new Scanner(System.in).nextLine();
				if(".".equals(par_name)) break;
				System.out.print("parameter "+i+" value :  ");
				String par_value = new Scanner(System.in).nextLine();
				params.put(par_name, par_value);
			}
			
			System.out.print("output type :  ");
			String out = new Scanner(System.in).nextLine();			
			action(session, action, params, out);
			//action(session, action, params, out);			
		}
	}
	
	
	
	
	/***
	 * @param session
	 * @param action
	 * @param params
	 * @param out
	 */
	public static void action(SessionTransport session, String action, Map<String, String> params, String out){
		
		if(!"xml".equals(out)){
			System.out.println(session.getStringContent(action, params));
			return;
		}
		
		Document resp = session.getXmlContent(action, params);
		
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = null;
		
		try {
			transformer = tf.newTransformer();
		} catch (TransformerConfigurationException e1) {
			e1.printStackTrace();
		}
		
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		StringWriter writer = new StringWriter();
		
		try {
			transformer.transform(new DOMSource(resp), new StreamResult(writer));
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		
		String output = writer.getBuffer().toString();
		System.out.println(output);
		
		try {
			System.out.print("result description  :");
			@SuppressWarnings("resource")
			String description =  new Scanner(System.in).nextLine();		
			FileOutputStream fos = new FileOutputStream("C:\\Users\\pydio\\Desktop\\SDK_test_results\\responses.xml", true);
			fos.write(("<!-- "+description+" -->\n").getBytes());
			fos.write(output.getBytes());
			fos.write(("\n<!-- "+description+" -->\n\n").getBytes());
			fos.close();
		} catch (Exception e) {
			System.out.println("can't store result");
		}
	}	
	
}
