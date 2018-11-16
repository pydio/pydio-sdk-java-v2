package com.pydio.sdk.core.common.callback;

import com.pydio.sdk.core.common.errors.Error;

import org.json.JSONObject;

public interface JSONCompletion {
    void onComplete(JSONObject o, Error error);
}
