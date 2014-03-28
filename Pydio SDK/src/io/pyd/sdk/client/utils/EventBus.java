package io.pyd.sdk.client.utils;

import io.pyd.sdk.client.model.PydioEvent;

public interface EventBus {	

	public interface Subscriber{
		public <E extends PydioEvent> void  onNewEvent(E e);
		public boolean once();
	}	
	
	
	public <E extends PydioEvent> void publish(E e);
	public <E extends PydioEvent> void subscribe(Subscriber sub, String type);
	public void unsubscribe(Subscriber sub, String type);
	
}
