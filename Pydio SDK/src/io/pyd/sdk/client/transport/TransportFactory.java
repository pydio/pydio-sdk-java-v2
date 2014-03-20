package io.pyd.sdk.client.transport;

public class TransportFactory {

	public static Transport getInstance(int mode){		
		if(mode == Transport.MODE_RESTFUL){
			return new RestTransport();
		}else if(mode == Transport.MODE_SESSION){
			return new SessionTransport();
		}else if(mode == Transport.MODE_MOCK){
			return new MockTransport();
		}
		return null;
	}
}
