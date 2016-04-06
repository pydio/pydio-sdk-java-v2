package pydio.sdk.java.model;

/**
 * Created by jabar on 09/11/2015.
 */
public class SearchNode extends FileNode {

    String mLabel, mPath;
    public SearchNode(String label){
        mLabel = label;
        mPath = "search://"+label;
    }

    @Override
    public int type() {
        return Node.TYPE_SEARCH;
    }

    @Override
    public String label() {
        return mLabel;
    }

    @Override
    public String path() {
        return mPath;
    }
}
