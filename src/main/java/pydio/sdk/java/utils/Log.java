package pydio.sdk.java.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by jabar on 17/02/2016.
 */
public class Log {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";


    public static void error(String msg){
        System.out.println(ANSI_RED + msg.replace("\n", "") + ANSI_RESET);
    }
    public static void info(String msg){
        System.out.println(ANSI_BLUE + msg.replace("\n", "") + ANSI_RESET);
    }
    public static void success(String msg){
        System.out.println(ANSI_GREEN + msg.replace("\n", "") + ANSI_RESET);
    }

    public static String paramString(Map<String, String> params){
        Set<String> keys = params.keySet();
        String s = "";
        Iterator<String> it = keys.iterator();
        while(it.hasNext()){
            String k = it.next();
            s += ", " + k + "=" +params.get(k);
        }
        return s;
    }
}
