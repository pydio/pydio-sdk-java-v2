package pydio.sdk.java.core.utils;

/**
 * Created by jabar on 29/01/2016.
 */
public class UnexpectedResponseException extends Exception {

    String mMessage;

    public UnexpectedResponseException(String message){
        mMessage = message;
    }

    public String getMessage(){
        return mMessage;
    }
}
