package pydio.sdk.java.core.model;

import java.util.Properties;

import javax.swing.text.AbstractDocument;

import pydio.sdk.java.core.utils.Pydio;

/**
 * Created by jabar on 15/07/2016.
 */
public class Session {

    public long id;
    public String user, name, serverAddress, legacyId, displayName;
    public byte[] logo;
    public Properties properties;

    public Session(){
        properties = new Properties();
    }

    public Session(ContentValues values){
        serverAddress = values.getAsString(Pydio.ADDRESS);
        id = values.getAsLong(Pydio.SESSION_ID);
        user = values.getAsString(Pydio.LOGIN);
        displayName = values.getAsString(Pydio.DISPLAYED_NAME);
        legacyId = user + "@" + serverAddress.replace("://", "+").replace("/","&");
        name = values.getAsString(Pydio.SESSION_NAME);
        logo = values.getAsByteArray(Pydio.LOGO);
        properties = new Properties();
    }

    public ContentValues values(){
        ContentValues values = new ContentValues();
        values.put(Pydio.ADDRESS, serverAddress);
        values.put(Pydio.DISPLAYED_NAME, displayName);
        values.put(Pydio.LOGIN, user);
        values.put(Pydio.LOGO, logo);
        values.put(Pydio.SESSION_NAME, name);
        return values;
    }
}
