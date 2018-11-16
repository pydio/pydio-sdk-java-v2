package com.pydio.sdk.core.common.callback;

import com.pydio.sdk.core.model.Message;

/**
 * Created by pydio on 10/02/2015.
 */
public interface MessageHandler {
    void onMessage(Message m);
}
