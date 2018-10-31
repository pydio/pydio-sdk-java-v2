package pydio.sdk.java.core.handlers;

import pydio.sdk.java.core.errors.Error;
import pydio.sdk.java.core.model.Message;

public interface MessageCompletion {
    void onComplete(Message msg, Error error);
}
