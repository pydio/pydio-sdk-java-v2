package pydio.sdk.java.core.utils;

import pydio.sdk.java.core.model.Change;

/**
 * Created by jabar on 22/07/2016.
 */
public interface ChangeHandler {
    void onChange(Change c);
}
