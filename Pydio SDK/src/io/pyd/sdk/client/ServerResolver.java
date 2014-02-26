package io.pyd.sdk.client;

import io.pyd.sdk.client.model.ServerNode;

public interface ServerResolver {
	public final static String SERVER_URL_RESOLUTION = "RequestResolution";
	void resolveServer(ServerNode server);
}
