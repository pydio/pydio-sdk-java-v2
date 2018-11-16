package com.pydio.sdk.core.common.callback;

import com.pydio.sdk.core.common.errors.Error;

public interface StringListCompletion {
    void onComplete(String[] list, Error error);
}
