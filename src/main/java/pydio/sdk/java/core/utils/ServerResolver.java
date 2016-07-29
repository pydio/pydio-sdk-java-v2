package pydio.sdk.java.core.utils;


import java.io.IOException;
import pydio.sdk.java.core.model.ResolutionServer;

/**
 * Interface describing class to resolve server adress
 * @author pydio
 *
 */
public interface ServerResolver {
	void resolve(ResolutionServer server) throws IOException;
}
