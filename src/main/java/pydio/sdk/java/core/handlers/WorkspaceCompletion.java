package pydio.sdk.java.core.handlers;

import pydio.sdk.java.core.errors.Error;
import pydio.sdk.java.core.model.WorkspaceNode;

public interface WorkspaceCompletion {
    void onComplete(WorkspaceNode w, Error error);
}
