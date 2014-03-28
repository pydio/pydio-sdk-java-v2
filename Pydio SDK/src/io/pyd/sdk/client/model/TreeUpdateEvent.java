package io.pyd.sdk.client.model;

import java.util.ArrayList;

import io.pyd.sdk.client.utils.Pydio;

public class TreeUpdateEvent implements PydioEvent{

	
	private ArrayList<Node> nodes;
	
	public TreeUpdateEvent (ArrayList<Node> nodes){
		this.nodes = nodes;
	}
	
	public String type() {
		return Pydio.NODE_DIFF_UPDATE;
	}
	
	public ArrayList<Node> getNode(){
		return nodes;
	}

}
