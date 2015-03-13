package pydio.sdk.java.model;

import java.util.ArrayList;

import pydio.sdk.java.utils.Pydio;


/**
 * Event class that describe events that are fired when a remote node has been removed
 * @author pydio
 *
 */
public class TreeRemoveEvent implements PydioEvent{

	ArrayList<String> pathes;
	
	/**
	 * genreate a remove event
	 * @param list
	 */
	public TreeRemoveEvent(ArrayList<String> list){
		this.pathes = list;
	}
	
	public String type() {		
		return Pydio.NODE_DIFF_REMOVE;
	}
	/** 
	 * @return An arrayList of remote Node pathes that have been removed
	 */
	public ArrayList<String> pathes(){
		return pathes;
	}
} 
