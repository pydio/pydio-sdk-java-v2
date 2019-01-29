package com.pydio.sdk.sync.changes;

import com.pydio.sdk.core.model.Change;

import java.util.ArrayList;
import java.util.List;
import com.pydio.sdk.sync.Error;

public class GetChangesResponse {
    private List<Change> changes;
    private long lastSeq;
    private boolean success;
    private Error error;


    public GetChangesResponse(){
        this.success = true;
    }

    public List<Change> getChanges() {
        return changes;
    }

    public void setChanges(List<Change> changes) {
        this.changes = changes;
    }

    public void addChange(Change change) {
        if(this.changes == null){
            this.changes = new ArrayList<>();
        }
        this.changes.add(change);
    }

    public long getLastSeq() {
        return lastSeq;
    }

    public void setLastSeq(long lastSeq) {
        this.lastSeq = lastSeq;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.success = error == null;
        this.error = error;
    }
}
