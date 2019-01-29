package com.pydio.sdk.core.model;

public class ChangeNode {

    private String id;
    private String md5;
    private long size;
    private long mTime;
    private String path;
    private String workspace;

    public void setId(String id) {
        this.id = id;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getId() {
        return id;
    }

    public String getMd5() {
        return md5;
    }

    public long getSize() {
        return size;
    }

    public long getmTime() {
        return mTime;
    }

    public void setmTime(long mTime) {
        this.mTime = mTime;
    }

    public String getPath() {
        return path;
    }

    public String getWorkspace() {
        return workspace;
    }
}
