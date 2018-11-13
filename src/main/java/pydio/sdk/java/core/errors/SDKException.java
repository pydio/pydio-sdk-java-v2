package pydio.sdk.java.core.errors;

import io.swagger.client.ApiException;
import pydio.sdk.java.core.utils.Pydio;

public class SDKException extends Throwable {
    public int code;
    public String message;
    public Exception cause;

    public SDKException(int code, String message, Exception cause){
        if (cause instanceof ApiException) {
            ApiException ae = (ApiException) cause;
            if (ae.getCode() == 401) {
                this.code = Pydio.ERROR_AUTHENTICATION;
            } else {
                this.code = Pydio.ERROR_CON_FAILED;
            }

            this.message = message;
            this.cause = cause;
        } else {
            this.code = code;
            this.message = message;
            this.cause = cause;
        }
    }

    public SDKException(ApiException e){
        this.code = e.getCode();
        this.message = e.getResponseBody();
        this.cause = e;
    }


    public static SDKException malFormURI(String url, Exception e){
        return new SDKException(Pydio.ERROR_BAD_URI, String.format("malformed: %s", url), e);
    }

    public static SDKException encoding(String encoding, String encoded, Exception e){
        return new SDKException(Pydio.ERROR_BAD_URI, String.format("failed to %s encode %s", encoding, encoded), e);
    }

    public static SDKException conFailed(Exception e){
        return new SDKException(Pydio.ERROR_CON_FAILED, "Connection failed", e);
    }

    public static SDKException conReadFailed(Exception e){
        return new SDKException(Pydio.ERROR_CON_READ, "Connection read failed", e);
    }

    public static SDKException conWriteFailed(Exception e){
        return new SDKException(Pydio.ERROR_CON_WRITE, "Connection write failed", e);
    }

    public static SDKException conClosed(Exception e){
        return new SDKException(Pydio.ERROR_CON_CLOSED, "Connection closed", e);
    }

    public static SDKException conState(Exception e){
        return new SDKException(Pydio.ERROR_CON_CLOSED, "Illegal connection state", e);
    }

    public static SDKException unexpectedContent(Exception e){
        return new SDKException(Pydio.ERROR_UNEXPECTED_RESPONSE, "Bad content", e);
    }

    public static SDKException badConfig(Exception e){
        return new SDKException(Pydio.ERROR_CON_CLOSED, "Bad configuration", e);
    }

    public static SDKException notFound(Exception e){
        return new SDKException(Pydio.ERROR_NOT_FOUND, "Bad configuration", e);
    }
}
