package com.pydio.sdk.core.common.callback;

import com.pydio.sdk.core.common.errors.Error;

public interface Completion {
    void onComplete(Error error);
}
