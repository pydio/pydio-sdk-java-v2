package com.pydio.sdk.core.common.callback;

import com.pydio.sdk.core.common.errors.Error;
import com.pydio.sdk.core.model.Node;

import java.util.List;

public interface NodeListCompletion {
    void onComplete(List<Node> nodes, Error error);
}
