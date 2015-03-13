package pydio.sdk.java.model;

import java.util.ArrayList;

import pydio.sdk.java.utils.Pydio;


/**
 * Event class that describe events that are fired when a remote node has been added
 * @author pydio
 *
 */
public class TreeAddEvent implements PydioEvent{	

	
	private ArrayList<Node> nodes;
	
	public TreeAddEvent(ArrayList<Node> nodes){
		this.nodes = nodes;
	}
	
	public String type() {
		return Pydio.NODE_DIFF_ADD;
	}
	/**
	 * @return An arrayList of remote Node that have been added
	 */
	public ArrayList<Node> getNode(){
		return nodes;
	}

}
