package pydio.sdk.java.core.thread;

import java.util.concurrent.ExecutionException;

public class Threading {

    public static void sleep(long interval){
        try{Thread.sleep(interval);} catch (Exception ignored){}
    }

}
