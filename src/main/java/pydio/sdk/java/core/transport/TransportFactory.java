package pydio.sdk.java.core.transport;


import pydio.sdk.java.core.model.ServerNode;

public class TransportFactory {

	/**
	 * generate a concrete Transport object to the mode
	 * @param mode int value of the mode
	 * @return null if the mode value is unknown
	 */
	public static pydio.sdk.java.core.transport.Transport getInstance(int mode){
		if(mode == pydio.sdk.java.core.transport.Transport.MODE_SESSION){
			return new pydio.sdk.java.core.transport.SessionTransport();
		}
		return null;
	}

    public static pydio.sdk.java.core.transport.Transport getInstance(int mode, ServerNode server){

        if(mode == pydio.sdk.java.core.transport.Transport.MODE_SESSION){
            return new pydio.sdk.java.core.transport.SessionTransport(server);
        }
        return null;
    }
}
