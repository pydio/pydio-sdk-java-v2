package io.pyd.sdk.client.model;

import java.util.HashMap;
import java.util.Map;

public class NodeSpecContainer {
	
	public final static String NODE_SPEC_DEFAULT = "PYDIO_SPEC_DATABASE";
	public final static String NODE_SPEC_SYNC = "PYDIO_SPEC_SYNC";
	
	private static Map<String, NodeSpec> specs = new HashMap<String, NodeSpec>();
	
	public static NodeSpec getSpecInstance(String name) throws Message{
	
		NodeSpec spec = null;	
		if(name.equals(NODE_SPEC_DEFAULT)){
			// return the instance of default 
		}else if(name.equals(NODE_SPEC_SYNC)){
			// return the instance of synch
		}else{
			if(!specs.containsKey(name)) throw new Message();
			return specs.get(name);
		}
		return spec;
	}
	
	public static void loadSpec(String name, NodeSpec spec) throws Message{
		if(specs.containsKey(name)) throw new Message();
		specs.put(name, spec);
	}
}
