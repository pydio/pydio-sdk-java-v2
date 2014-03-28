package io.pyd.sdk.client.model;

import java.util.ArrayList;

import io.pyd.sdk.client.utils.Pydio;

public class TreeAddEvent implements PydioEvent{	

	
	private ArrayList<Node> nodes;
	
	public TreeAddEvent(ArrayList<Node> nodes){
		this.nodes = nodes;
	}
	
	public String type() {
		return Pydio.NODE_DIFF_ADD;
	}
	
	public ArrayList<Node> getNode(){
		return nodes;
	}

}
