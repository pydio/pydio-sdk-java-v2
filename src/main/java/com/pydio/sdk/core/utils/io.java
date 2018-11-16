package com.pydio.sdk.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class io {

    public static int bufferSize = 4096;

    public static boolean close(InputStream in) {
        try {
            in.close();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean close(OutputStream out) {
        try {
            out.close();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static long pipeRead(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[bufferSize];
        long total_read = 0;
        int read;
        for (; ; ) {
            read = in.read(buffer);
            if (read == -1) break;
            total_read += read;
            out.write(buffer, 0, read);
        }
        return total_read;
    }

    public static long pipeReadWithProgress(InputStream in, OutputStream out, ProgressListener listener) throws IOException {
        byte[] buffer = new byte[bufferSize];
        long total_read = 0;
        int read;
        for (; ; ) {
            read = in.read(buffer);
            if (read == -1) break;
            total_read += read;
            out.write(buffer, 0, read);
            listener.onProgress(total_read);
        }
        return total_read;
    }
}
