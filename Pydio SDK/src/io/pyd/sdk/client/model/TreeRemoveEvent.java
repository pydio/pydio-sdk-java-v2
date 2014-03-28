package io.pyd.sdk.client.model;

import java.util.ArrayList;

import io.pyd.sdk.client.utils.Pydio;

public class TreeRemoveEvent implements PydioEvent{

	ArrayList<String> pathes;
	
	public TreeRemoveEvent(ArrayList<String> list){
		this.pathes = list;
	}
	
	public String type() {		
		return Pydio.NODE_DIFF_REMOVE;
	}
	
	public ArrayList<String> pathes(){
		return pathes;
	}
} 
