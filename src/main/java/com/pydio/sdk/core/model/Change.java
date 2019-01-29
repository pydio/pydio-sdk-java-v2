package com.pydio.sdk.core.model;

import java.io.Serializable;

public class Change implements Serializable {

    public static final String TYPE_CREATE = "create";
    public static final String TYPE_PATH = "path";
    public static final String TYPE_DELETE = "delete";
    public static final String TYPE_CONTENT = "content";

    private long seq;
    private String sourceSide;
    private String targetSide;
    private String type;
    private String source;
    private String target;
    private String nodeId;
    private ChangeNode node;

    public String getSourceSide() {
        return sourceSide;
    }

    public void setSourceSide(String sourceSide) {
        this.sourceSide = sourceSide;
    }

    public String getTargetSide() {
        return targetSide;
    }

    public void setTargetSide(String targetSide) {
        this.targetSide = targetSide;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public ChangeNode getNode() {
        return node;
    }

    public void setNode(ChangeNode node) {
        this.node = node;
    }
}
