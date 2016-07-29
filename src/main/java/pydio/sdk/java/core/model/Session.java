package pydio.sdk.java.core.model;

import pydio.sdk.java.core.utils.Pydio;

/**
 * Created by jabar on 15/07/2016.
 */
public class Session {

    public long id;
    public String user, name, serverAddress, legacyId, displayName;
    public byte[] logo;

    public static Session parse(ContentValues values){
        Session s = new Session();
        s.serverAddress = values.getAsString(Pydio.ADDRESS);
        s.id = values.getAsLong(Pydio.SESSION_ID);
        s.user = values.getAsString(Pydio.LOGIN);
        s.displayName = values.getAsString(Pydio.DISPLAYED_NAME);
        s.legacyId = s.user + "@" + s.serverAddress.replace("://", "+").replace("/","&");
        s.name = values.getAsString(Pydio.SESSION_NAME);
        s.logo = values.getAsByteArray(Pydio.LOGO);
        return s;
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
