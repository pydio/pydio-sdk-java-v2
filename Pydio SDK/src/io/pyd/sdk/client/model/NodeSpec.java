package io.pyd.sdk.client.model;

public interface NodeSpec {
	public Message read(Node node);
	public Message write(Node node);
}
