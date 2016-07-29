package pydio.sdk.java.core.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by jabar on 17/02/2016.
 */
public class Log {


    public static void setLogger(Logger l){
        logger = l;
    }
    private static Logger logger;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";


    public static void e(String tag, String msg){
        if(logger != null){
            logger.e(tag, msg);
            return;
        }
        //System.out.println(ANSI_RED + msg.replace("\n", "") + ANSI_RESET);
        System.out.println(tag + "\t" + msg.replace("\n", ""));
    }
    public static void i(String tag, String msg){
        if(logger != null){
            logger.i(tag, msg);
            return;
        }
        //System.out.println(ANSI_GREEN + msg.replace("\n", "") + ANSI_RESET);
        System.out.println(tag + "\t" + msg.replace("\n", ""));
    }
    public static void d(String tag, String msg){
        if(logger != null){
            logger.d(tag, msg);
            return;
        }
        //System.out.println(ANSI_GREEN + msg.replace("\n", "") + ANSI_RESET);
        System.out.println(tag + "\t" + msg.replace("\n", ""));
    }
    public static void v(String tag, String msg){
        if(logger != null){
            logger.v(tag, msg);
            return;
        }
        //System.out.println(ANSI_GREEN + msg.replace("\n", "") + ANSI_RESET);
        System.out.println(tag + "\t" + msg.replace("\n", ""));
    }
    public static void w(String tag, String msg){
        if(logger != null){
            logger.w(tag, msg);
            return;
        }
        //System.out.println(ANSI_GREEN + msg.replace("\n", "") + ANSI_RESET);
        System.out.println(tag + "\t" + msg.replace("\n", ""));
    }
    public static String paramString(Map<String, String> params){
        Set<String> keys = params.keySet();
        String s = "";
        Iterator<String> it = keys.iterator();
        while(it.hasNext()){
            String k = it.next();
            s += "&" + k + "=" +params.get(k);
        }
        if(s.length() > 0){
            s = s.substring(1);
        }
        return s;
    }

    interface Logger {
        public void e(String tag, String text);
        public void i(String tag, String text);
        public void v(String tag, String text);
        public void d(String tag, String text);
        public void w(String tag, String text);
    }
}

