package com.pydio.sdk.core.common.errors;

import com.pydio.sdk.core.api.cells.ApiException;

import java.io.IOException;

public class SDKException extends Throwable {
    public int code;
    public Exception cause;

    private SDKException() {
    }

    public SDKException(int code, Exception cause) {
        if (cause instanceof ApiException) {
            ApiException ae = (ApiException) cause;
            if (ae.getCode() == 401) {
                this.code = Code.authentication_required;
            } else {
                this.code = Code.con_failed;
            }

            this.cause = cause;
        } else {
            this.code = code;
            this.cause = cause;
        }
    }

    public SDKException(ApiException e){
        this.code = e.getCode();
        this.cause = e;
    }

    public static SDKException fromP8Code(int p8Code) {
        SDKException e = new SDKException();
        e.code = p8Code;
        return e;
    }

    public static SDKException fromHttpCode(int httpCode) {
        SDKException e = new SDKException();
        e.code = httpCode;
        return e;
    }

    public static SDKException malFormURI(Exception e) {
        return new SDKException(Code.bad_uri, e);
    }

    public static SDKException encoding(Exception e) {
        return new SDKException(Code.encoding_failed, e);
    }

    public static SDKException conFailed(IOException e) {
        return new SDKException(Code.con_failed, e);
    }

    public static SDKException conReadFailed(IOException e) {
        return new SDKException(Code.con_read_failed, e);
    }

    public static SDKException conWriteFailed(IOException e) {
        return new SDKException(Code.con_write_failed, e);
    }

    public static SDKException conClosed(IOException e) {
        return new SDKException(Code.con_closed, e);
    }

    public static SDKException unexpectedContent(Exception e){
        return new SDKException(Code.unexpected_content, e);
    }

    public static SDKException badConfig(Exception e){
        return new SDKException(Code.bad_config, e);
    }

    public static SDKException notFound(Exception e){
        return new SDKException(Code.not_found, e);
    }
}
