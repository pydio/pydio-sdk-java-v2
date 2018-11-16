package com.pydio.sdk.core.utils;

import java.util.HashMap;
import java.util.Map;

public class Params {
    private Map<String, String> params;

    private Params(){
        params = new HashMap<>();
    }

    public Params set(String key, String value){
        params.put(key, value);
        return this;
    }

    public Params del(String key){
        params.remove(key);
        return this;
    }

    public Map<String, String> get(){
        return params;
    }

    public static Params create(String key, String value){
        Params p = new Params();
        p.set(key, value);
        return p;
    }
}
