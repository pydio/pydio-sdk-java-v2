package pydio.sdk.java.core.utils;

import pydio.sdk.java.core.model.NodeDiff;

/**
 * Created by jabar on 09/02/2017.
 */

public interface NodeDiffHandler {
    void onNodeDiff(NodeDiff diff);
}
