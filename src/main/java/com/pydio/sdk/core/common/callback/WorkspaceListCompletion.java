package com.pydio.sdk.core.common.callback;

import com.pydio.sdk.core.common.errors.Error;
import com.pydio.sdk.core.model.WorkspaceNode;

import java.util.List;

public interface WorkspaceListCompletion {
    void onComplete(List<WorkspaceNode> nodes, Error error);
}
