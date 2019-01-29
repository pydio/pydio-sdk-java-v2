package com.pydio.sdk.sync.fs;

import com.pydio.sdk.sync.changes.GetChangeRequest;
import com.pydio.sdk.sync.changes.GetChangesResponse;
import com.pydio.sdk.sync.content.ContentLoader;
import com.pydio.sdk.sync.changes.ProcessChangeRequest;
import com.pydio.sdk.sync.changes.ProcessChangeResponse;

import java.util.List;

public interface Fs {

    String id();

    List<String> getWatches();

    void addWatch(String path);

    GetChangesResponse getChanges(GetChangeRequest request);

    ProcessChangeResponse processChange(ProcessChangeRequest request);

    ContentLoader getContentLoader();

    boolean receivesEvents();

    boolean sendsEvents();
}
