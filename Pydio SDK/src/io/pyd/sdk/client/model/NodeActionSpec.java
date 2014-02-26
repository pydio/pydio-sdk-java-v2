package io.pyd.sdk.client.model;

public interface NodeActionSpec {
	public Message read(Node node);
	public Message write(Node node);
}
