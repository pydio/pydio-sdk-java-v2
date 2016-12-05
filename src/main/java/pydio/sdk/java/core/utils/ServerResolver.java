package pydio.sdk.java.core.utils;


import java.io.IOException;

/**
 * Interface describing class to resolve server adress
 * @author pydio
 *
 */
public interface ServerResolver {
	String resolve(String id) throws IOException;
}
