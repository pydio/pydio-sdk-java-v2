package pydio.sdk.java.core.handlers;

import pydio.sdk.java.core.errors.Error;

public interface StringListCompletion {
    void onComplete(String[] list, Error error);
}
