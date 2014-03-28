package io.pyd.sdk.client.utils;

import io.pyd.sdk.client.model.PydioEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DefaultEventBus implements EventBus{
	
	private Map < String, ArrayList<Subscriber> > map = new HashMap< String, ArrayList<Subscriber> >();
	private static DefaultEventBus bus; 

	public static DefaultEventBus bus(){
		if(bus == null) bus = new DefaultEventBus();
		return bus;
	}

	public synchronized <E extends PydioEvent> void publish(E  e){
		ArrayList<Subscriber> list = map.get(e.type());
		if(list == null) return;
		
		Iterator<Subscriber> it = list.iterator(); 
		while(it.hasNext()){
			Subscriber sub = (Subscriber) it.next();
			sub.onNewEvent(e);
			if(sub.once()){
				it.remove();
			}
		}
	}
	
		
	public synchronized void subscribe(Subscriber sub, String etype){
		if(sub == null) return;
		ArrayList<Subscriber> list = map.get(etype);		
		if( list == null) list = new ArrayList<Subscriber>();
		list.add(sub);
	}
	
	
	public synchronized void unsubscribe(Subscriber sub, String type){		
		if(type == null){
			unsubscribe(sub, Pydio.NODE_DIFF_ADD);
			unsubscribe(sub, Pydio.NODE_DIFF_UPDATE);
			unsubscribe(sub, Pydio.NODE_DIFF_REMOVE);
		}else{
			ArrayList<Subscriber> list = map.get(type);			
			if(list == null) return;
			
			Iterator<Subscriber> it = list.iterator(); 
			while(it.hasNext()){
				Subscriber s = (Subscriber) it.next();
				if(s == sub)
					it.remove();
			}
		}
	}


}
