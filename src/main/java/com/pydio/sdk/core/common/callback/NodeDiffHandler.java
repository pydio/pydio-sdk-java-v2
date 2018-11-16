package com.pydio.sdk.core.common.callback;

import com.pydio.sdk.core.model.NodeDiff;

/**
 * Created by jabar on 09/02/2017.
 */

public interface NodeDiffHandler {
    void onNodeDiff(NodeDiff diff);
}
