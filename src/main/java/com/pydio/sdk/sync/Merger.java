package com.pydio.sdk.sync;

import com.pydio.sdk.core.model.Change;
import com.pydio.sdk.core.utils.Log;
import com.pydio.sdk.sync.changes.ChangeStore;
import com.pydio.sdk.sync.changes.GetChangeRequest;
import com.pydio.sdk.sync.changes.GetChangesResponse;
import com.pydio.sdk.sync.changes.ProcessChangeRequest;
import com.pydio.sdk.sync.changes.ProcessChangeResponse;
import com.pydio.sdk.sync.fs.Fs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Merger {

    private ChangeStore changeStore;
    private MergeState state;
    private List<Fs> fsList;
    private Map<String, Fs> mappedFsList;

    public Merger(MergeState state, ChangeStore store, Fs... fsList) {
        this.state = state;
        this.changeStore = store;
        this.mappedFsList = new HashMap<>();
        this.fsList = new ArrayList<>();
        for (Fs fs : fsList) {
            this.fsList.add(fs);
            this.mappedFsList.put(fs.id(), fs);
        }
    }

    public Error merge(MergeActivityListener mergeActivityListener) {
        Error error = fetchChanges();
        if (error != null) {
            return error;
        }
        return processChanges(mergeActivityListener);
    }

    public int countChanges() {
        return changeStore.count();
    }

    public Error fetchChanges() {
        if (changeStore.count() == 0) {
            List<Watch> watches = state.watches(this.fsList);
            for (Watch w : watches) {
                Fs sourceFs = mappedFsList.get(w.getSourceFs());
                if (sourceFs.receivesEvents()) {
                    Fs targetFs = mappedFsList.get(w.getTargetFs());
                    if (targetFs.sendsEvents()){

                        GetChangeRequest getChangeRequest = new GetChangeRequest();
                        getChangeRequest.setPath(w.getPath());
                        getChangeRequest.setSeq(w.getSeq());
                        getChangeRequest.setSide(sourceFs.id());

                        GetChangesResponse getChangesResponse = targetFs.getChanges(getChangeRequest);
                        if (getChangesResponse.isSuccess()) {
                            List<Change> changes = getChangesResponse.getChanges();
                            if (changes != null){
                                this.changeStore.putChanges(changes);
                            }
                            w.setSeq(getChangesResponse.getLastSeq());
                            this.state.updateSeq(w);
                        } else {
                            Error error = getChangesResponse.getError();
                            Log.e("Sync", "Failed to get changes from " + targetFs.id() + ":" + error.toString());
                            return error;
                        }
                    }
                }
            }
        }
        return null;
    }

    private Error processChanges(MergeActivityListener mergeActivityListener) {
        int count = this.changeStore.count();
        while(count > 0) {
            List<Change> changes = this.changeStore.getChanges(10);
            for (Change c: changes){
                String source = c.getSourceSide();
                String target = c.getTargetSide();

                Fs sourceFs = this.mappedFsList.get(source);
                Fs targetFs = this.mappedFsList.get(target);

                ProcessChangeRequest request = new ProcessChangeRequest();
                request.setChange(c);
                request.setContentLoader(sourceFs.getContentLoader());

                ProcessChangeResponse response = targetFs.processChange(request);
                if (response.isSuccess()){
                    changeStore.deleteChange(c);
                    if (mergeActivityListener != null) {
                        mergeActivityListener.onActionCompleted(c);
                    }
                } else {
                    Error error = response.getError();
                    if (mergeActivityListener != null) {
                        mergeActivityListener.onActionFailed(error, c);
                    }
                    Log.e("Sync", "Failed to get changes from " + targetFs.id() + ":" + error.toString());
                    return error;
                }
            }
            count = this.changeStore.count();
        }
        return null;
    }
}
