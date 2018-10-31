package pydio.sdk.java.core.handlers;
import pydio.sdk.java.core.errors.Error;

public interface Completion {
    void onComplete(Error error);
}
