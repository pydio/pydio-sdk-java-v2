package com.pydio.sdk.core.common.errors;

public class Error {

    public int code;

    public Error(int code) {
        this.code = code;
    }

    public static Error fromException(SDKException e) {
        return new Error(e.code);
    }
}

