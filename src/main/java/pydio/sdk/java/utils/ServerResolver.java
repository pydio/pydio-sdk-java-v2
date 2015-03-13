package pydio.sdk.java.utils;


import pydio.sdk.java.model.ServerNode;

/**
 * Interface describing class to resolve server adress
 * @author pydio
 *
 */
public interface ServerResolver {
	void resolve(ServerNode server);
}
