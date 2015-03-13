package pydio.sdk.java.transport;


import pydio.sdk.java.model.ServerNode;

public class TransportFactory {

	/**
	 * generate a concrete Transport object to the mode
	 * @param mode int value of the mode
	 * @return null if the mode value is unknown
	 */
	public static Transport getInstance(int mode){		
		
		if(mode == Transport.MODE_RESTFUL){
			return new RestTransport();
		}else if(mode == Transport.MODE_SESSION){
			return new SessionTransport();
		}
		return null;
	}

    public static Transport getInstance(int mode, ServerNode server){

        if(mode == Transport.MODE_RESTFUL){
            return new RestTransport();
        }else if(mode == Transport.MODE_SESSION){
            return new SessionTransport(server);
        }
        return null;
    }
}
