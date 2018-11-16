package com.pydio.sdk.core.common.callback;

import com.pydio.sdk.core.common.errors.Error;
import com.pydio.sdk.core.model.Message;

public interface MessageCompletion {
    void onComplete(Message msg, Error error);
}
