package com.pydio.sdk.core.utils;

import java.io.File;
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

    public static final String ANSI_BCKGD_BLACK = "\u001B[40m";
    public static final String ANSI_BCKGD_RED = "\u001B[41m";
    public static final String ANSI_BCKGD_GREEN = "\u001B[42m";
    public static final String ANSI_BCKGD_YELLOW = "\u001B[43m";
    public static final String ANSI_BCKGD_BLUE = "\u001B[44m";
    public static final String ANSI_BCKGD_PURPLE = "\u001B[45m";
    public static final String ANSI_BCKGD_CYAN = "\u001B[46m";
    public static final String ANSI_BCKGD_WHITE = "\u001B[47m";


    public static void e(String tag, String msg){
        if(logger != null){
            logger.e(tag, msg);
            return;
        }

        if(UNIXLike()){
            msg = ANSI_GREEN + tag + "\t" + msg + ANSI_RESET;
            System.out.println(ANSI_BCKGD_RED + msg + ANSI_RESET);
        } else {
            System.out.println(tag + "\t" + msg);
        }
    }
    public static void i(String tag, String msg){
        if(logger != null){
            logger.i(tag, msg);
            return;
        }
        if(UNIXLike()){
            msg = ANSI_BLACK + tag + "\t" + msg + ANSI_RESET;
            System.out.println(ANSI_BCKGD_WHITE + msg + ANSI_RESET);
        } else {
            System.out.println(tag + "\t" + msg);
        }
    }
    public static void d(String tag, String msg){
        if(logger != null){
            logger.d(tag, msg);
            return;
        }
        if(UNIXLike()){
            System.out.println(ANSI_YELLOW + msg + ANSI_RESET);
        } else {
            System.out.println(tag + "\t" + msg);
        }
    }
    public static void v(String tag, String msg){
        if(logger != null){
            logger.v(tag, msg);
            return;
        }
        System.out.println(tag + "\t" + msg);
    }
    public static void w(String tag, String msg){
        if(logger != null){
            logger.w(tag, msg);
            return;
        }
        if(UNIXLike()){
            msg = ANSI_BLACK + tag + "\t" + msg + ANSI_RESET;
            System.out.println(ANSI_BCKGD_YELLOW + msg + ANSI_RESET);
        } else {
            System.out.println(tag + "\t" + msg);
        }
    }

    public static String paramString(Map<String, String> params){
        Set<String> keys = params.keySet();
        String s = " ";
        Iterator<String> it = keys.iterator();
        while(it.hasNext()){
            String k = it.next();
            s += " " + k + "=" +params.get(k);
        }
        if(s.length() > 0){
            s = s.substring(1);
        }
        return s;
    }

    private static boolean UNIXLike(){
        return File.separator.equals("/");
    }

    public interface Logger {
        void e(String tag, String text);

        void i(String tag, String text);

        void v(String tag, String text);

        void d(String tag, String text);

        void w(String tag, String text);
    }
}

