package pydio.sdk.java.core.utils;
import pydio.sdk.java.core.model.PydioMessage;

/**
 * Created by pydio on 10/02/2015.
 */
public interface MessageHandler {
    public void onMessage(PydioMessage m);
}
