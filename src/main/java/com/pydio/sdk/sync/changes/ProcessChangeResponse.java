package com.pydio.sdk.sync.changes;

import com.pydio.sdk.sync.Error;

public class ProcessChangeResponse {

    private boolean success;
    private long time;
    private Error error;

    public ProcessChangeResponse() {
        this.success = true;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.success = error == null;
        this.error = error;
    }
}
