package pydio.sdk.java.core.errors;

import pydio.sdk.java.core.utils.Pydio;

public class Error {

    public static Error fromException(SDKException e){

        return new Error(e.code, e.message, e.cause);
    }

    public static Error notFound(){
        return new Error(Pydio.ERROR_NOT_FOUND, "", null);
    }

    public Error(int code, String text, Exception e) {
        this.code = code;
        this.text = text;
        this.cause = e;
    }

    public int code;
    public String text;
    public Exception cause;
}

