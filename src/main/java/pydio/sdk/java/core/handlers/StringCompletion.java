package pydio.sdk.java.core.handlers;

import pydio.sdk.java.core.errors.Error;

public interface StringCompletion {
    void onComplete(String str, Error error);
}
