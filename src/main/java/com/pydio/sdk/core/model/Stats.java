package com.pydio.sdk.core.model;

import org.json.JSONObject;

/**
 * Created by jabar on 22/07/2016.
 */
public class Stats {
    private String hash;
    private long size;
    private long mTime;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getmTime() {
        return mTime;
    }

    public void setmTime(long mTime) {
        this.mTime = mTime;
    }
}
