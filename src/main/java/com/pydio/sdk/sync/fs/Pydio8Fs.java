package com.pydio.sdk.sync.fs;

import com.pydio.sdk.core.Pydio8;
import com.pydio.sdk.core.common.errors.SDKException;
import com.pydio.sdk.core.model.ChangeNode;
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

public class Pydio8Fs implements Fs, ContentLoader {

    private Pydio8 client;
    private String workspace;
    private String id;

    public Pydio8Fs(String id, Pydio8 pydio8, String workspace) {
        this.id = id;
        this.client = pydio8;
        this.workspace = workspace;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public List<String> getWatches() {
        return new ArrayList<>();
    }

    @Override
    public void addWatch(String path) {}

    @Override
    public GetChangesResponse getChanges(GetChangeRequest request) {
        GetChangesResponse response = new GetChangesResponse();
        try {
            int reqSeq = (int) request.getSeq();
            long seq = client.changes(workspace, request.getPath(), reqSeq, reqSeq == 0, (c) -> {
                c.setSourceSide(id());
                c.setTargetSide(request.getSide());

                if(!request.getPath().equals("/")) {
                    if(!c.getSource().equals("NULL")) {
                        c.setSource(request.getPath() + c.getSource());
                    }

                    if(!c.getTarget().equals("NULL")) {
                        c.setTarget(request.getPath() + c.getTarget());
                    }

                    ChangeNode node = c.getNode();
                    if(node != null && !"NULL".equals(node.getPath())) {
                        String p = node.getPath();
                        if (p == null) {
                            node.setPath(request.getPath());
                        } else {
                            node.setPath(request.getPath() + p);
                        }
                    }
                }
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
    public Content getContent(String path) {
        return new PydioRemoteFileContent(client, workspace, path);
    }
}
