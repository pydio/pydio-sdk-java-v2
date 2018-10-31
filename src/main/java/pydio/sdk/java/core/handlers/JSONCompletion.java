package pydio.sdk.java.core.handlers;

import pydio.sdk.java.core.errors.Error;

import org.json.JSONObject;

public interface JSONCompletion {
    void onComplete(JSONObject o, Error error);
}
