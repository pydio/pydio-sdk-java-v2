package com.pydio.sdk.sync;

public class Watch {
    private String sourceFs;
    private String targetFs;
    private String path;
    private long seq;

    public String getSourceFs() {
        return sourceFs;
    }

    public void setSourceFs(String sourceFs) {
        this.sourceFs = sourceFs;
    }

    public String getTargetFs() {
        return targetFs;
    }

    public void setTargetFs(String targetFs) {
        this.targetFs = targetFs;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Watch)) return false;
        Watch watch = (Watch) o;
        return getSourceFs().equals(watch.getSourceFs()) &&
                getTargetFs().equals(watch.getTargetFs()) &&
                getPath().equals(watch.getPath());
    }
}
