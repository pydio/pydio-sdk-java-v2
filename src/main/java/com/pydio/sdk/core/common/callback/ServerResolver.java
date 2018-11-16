package com.pydio.sdk.core.common.callback;


import java.io.IOException;

/**
 * Interface describing class to resolve server adress
 * @author pydio
 *
 */
public interface ServerResolver {
	String resolve(String id, boolean refresh) throws IOException;
}
