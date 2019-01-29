package com.pydio.sdk.sync;

public class Error {

    public static final int NOT_MOUNTED = 1;
    public static final int CANNOT_READ = 2;
    public static final int CANNOT_WRITE = 3;
    public static final int UNKNOWN_OPERATION = 4;
    public static final int NOT_FOUND = 5;
    public static final int OP_FAILED = 6;
    public static final int GET_CONTENT = 7;

    private int code;
    private String op;
    private String fs;
    private String loader;
    private String path;
    private String details;

    public Error(){}

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getFs() {
        return fs;
    }

    public void setFs(String fs) {
        this.fs = fs;
    }

    public String getLoader() {
        return loader;
    }

    public void setLoader(String loader) {
        this.loader = loader;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }



    public static Error notMounted(String fs){
        Error error = new Error();
        error.code = NOT_MOUNTED;
        return error;
    }

    public static Error notFound(String fs){
        Error error = new Error();
        error.code = NOT_FOUND;
        return error;
    }

    public static Error cannotRead(String fs){
        Error error = new Error();
        error.code = CANNOT_READ;
        return error;
    }

    public static Error cannotWrite(String fs, String path){
        Error error = new Error();
        error.code = CANNOT_WRITE;
        return error;
    }

    public static Error failedToGetContent(String fs, String path){
        Error error = new Error();
        error.code = GET_CONTENT;
        error.fs = fs;
        error.path = path;
        return error;
    }

    public static Error unknownOperation(String op){
        Error error = new Error();
        error.code = UNKNOWN_OPERATION;
        return error;
    }

    public static Error notFound(String fs, String path){
        Error error = new Error();
        error.code = UNKNOWN_OPERATION;
        return error;
    }

    public static Error opFailed(String op, String fs, String path){
        Error error = new Error();
        error.setCode(OP_FAILED);
        error.fs = fs;
        error.path = path;
        return error;
    }
}
