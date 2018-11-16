package com.pydio.sdk.core.common.callback;

import com.pydio.sdk.core.common.errors.Error;

public interface StringCompletion {
    void onComplete(String str, Error error);
}
