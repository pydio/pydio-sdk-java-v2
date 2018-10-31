package pydio.sdk.java.core.handlers;

import java.util.List;

import pydio.sdk.java.core.errors.Error;
import pydio.sdk.java.core.model.WorkspaceNode;

public interface WorkspaceListCompletion {
    void onComplete(List<WorkspaceNode> nodes, Error error);
}
