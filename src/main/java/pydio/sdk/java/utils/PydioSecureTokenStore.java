package pydio.sdk.java.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pydio on 26/05/2015.
 */
public class PydioSecureTokenStore {

    private static PydioSecureTokenStore store;
    Map<String, String> secure_tokens;

    public static PydioSecureTokenStore getInstance(){
        if(store == null){
            store = new PydioSecureTokenStore();
        }
        return store;
    }

    private PydioSecureTokenStore(){
        secure_tokens = new HashMap<String, String>();
    }

    public void add(String key, String value){
        secure_tokens.put(key, value);
    }

    public String get(String key){
        String token = secure_tokens.get(key);
        if(token == null) return "";
        return token;
    }
}
