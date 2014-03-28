package io.pyd.sdk.client.model;

import java.util.HashMap;
import java.util.Map;

public class NodeSpecContainer {
	
	public final static String NODE_SPEC_DEFAULT = "PYDIO_SPEC_DATABASE";
	public final static String NODE_SPEC_SYNC = "PYDIO_SPEC_SYNC";
	
	private static Map<String, NodeSpec> specs = new HashMap<String, NodeSpec>();
	
	public static NodeSpec getSpecInstance(String name){
	
		NodeSpec spec = null;	
		if(name.equals(NODE_SPEC_DEFAULT)){
			// return the instance of default 
		}else if(name.equals(NODE_SPEC_SYNC)){
			// return the instance of synch
		}else{
			if(!specs.containsKey(name)) return null;
			return specs.get(name);
		}
		return spec;
	}
	
	public static boolean loadSpec(String name, NodeSpec spec){
		if(specs.containsKey(name)) return false;
		specs.put(name, spec);
		return true;
	}
}
