package pydio.sdk.java.core.handlers;

import pydio.sdk.java.core.errors.Error;

import java.io.InputStream;

public interface InputStreamCompletion {
    void onComplete(InputStream in, Error error);
}
