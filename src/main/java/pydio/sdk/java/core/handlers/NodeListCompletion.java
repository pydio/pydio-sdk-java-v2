package pydio.sdk.java.core.handlers;
import pydio.sdk.java.core.errors.Error;
import java.util.List;
import pydio.sdk.java.core.model.Node;

public interface NodeListCompletion {
    void onComplete(List<Node> nodes, Error error);
}
