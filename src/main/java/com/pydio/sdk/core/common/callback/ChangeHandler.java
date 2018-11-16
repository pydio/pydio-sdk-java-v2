package com.pydio.sdk.core.common.callback;

import com.pydio.sdk.core.model.Change;

/**
 * Created by jabar on 22/07/2016.
 */
public interface ChangeHandler {
    void onChange(Change c);
}
