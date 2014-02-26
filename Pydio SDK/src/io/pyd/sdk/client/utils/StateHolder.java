package io.pyd.sdk.client.utils;

import io.pyd.sdk.client.model.Node;

public interface StateHolder {
	
	public Node getRepository();
	public Node getDirectory();
	public Node getServer();
	
	public void setServer(Node server);
	public void setDirectory(Node directory);
	public void setRepository(Node repository);
	
}
