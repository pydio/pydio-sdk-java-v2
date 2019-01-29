package com.pydio.sdk.sync.fs;

import com.pydio.sdk.core.PydioCells;
import com.pydio.sdk.core.common.errors.SDKException;
import com.pydio.sdk.sync.Error;
import com.pydio.sdk.sync.changes.GetChangeRequest;
import com.pydio.sdk.sync.changes.GetChangesResponse;
import com.pydio.sdk.sync.content.Content;
import com.pydio.sdk.sync.content.ContentLoader;
import com.pydio.sdk.sync.changes.ProcessChangeRequest;
import com.pydio.sdk.sync.changes.ProcessChangeResponse;
import com.pydio.sdk.sync.content.PydioRemoteFileContent;

import java.util.ArrayList;
import java.util.List;

public class CellsFs implements Fs, ContentLoader {

    private PydioCells cells;
    private String workspace;
    private String id;

    public CellsFs(String id, PydioCells cells, String workspace) {
        this.id = id;
        this.cells = cells;
        this.workspace = workspace;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public List<String> getWatches() {
        return new ArrayList<>();
    }

    @Override
    public void addWatch(String path) {
    }

    @Override
    public GetChangesResponse getChanges(GetChangeRequest request) {
        GetChangesResponse response = new GetChangesResponse();
        try {
            int reqSeq = (int) request.getSeq();
            long seq = cells.changes(workspace, request.getPath(), reqSeq, reqSeq == 0, (c) -> {
                c.setSource(id());
                c.setTargetSide(request.getSide());
                response.addChange(c);
            });
            response.setLastSeq(seq);
        } catch (SDKException e) {
            e.printStackTrace();
            response.setError(Error.notMounted(""));
        }
        return response;
    }

    @Override
    public ProcessChangeResponse processChange(ProcessChangeRequest request) {
        return null;
    }

    @Override
    public ContentLoader getContentLoader() {
        return this;
    }

    @Override
    public boolean receivesEvents() {
        return false;
    }

    @Override
    public boolean sendsEvents() {
        return true;
    }

    @Override
    public Content getContent(String nodeId) {
        return new PydioRemoteFileContent(cells, workspace, nodeId);
    }
}
