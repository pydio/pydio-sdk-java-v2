package com.pydio.sdk.core.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

    public static long write(byte[] bytes, OutputStream out) throws IOException {
        InputStream in = new ByteArrayInputStream(bytes);
        byte[] buffer = new byte[bufferSize];
        long total_read = 0;
        int read;
        for (; ; ) {
            read = in.read(buffer);
            if (read == -1) break;
            total_read += read;
            out.write(buffer, 0, read);
        }
        io.close(in);
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

    public static byte[] readFile(String path) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = new FileInputStream(path);
        byte[] buffer = new byte[bufferSize];
        long total_read = 0;
        int read;
        for (; ; ) {
            read = in.read(buffer);
            if (read == -1) break;
            total_read += read;
            out.write(buffer, 0, read);
        }
        io.close(in);
        return out.toByteArray();
    }

    public static long writeFile(byte[] bytes, String filepath) throws IOException {
        OutputStream out = new FileOutputStream(filepath);
        InputStream in = new ByteArrayInputStream(bytes);
        byte[] buffer = new byte[bufferSize];
        long total_read = 0;
        int read;
        for (; ; ) {
            read = in.read(buffer);
            if (read == -1) break;
            total_read += read;
            out.write(buffer, 0, read);
        }
        io.close(out);
        return total_read;
    }

    public static long writeFile(InputStream in, String filepath) throws IOException {
        OutputStream out = new FileOutputStream(filepath);
        byte[] buffer = new byte[bufferSize];
        long total_read = 0;
        int read;
        for (; ; ) {
            read = in.read(buffer);
            if (read == -1) break;
            total_read += read;
            out.write(buffer, 0, read);
        }
        io.close(out);
        return total_read;
    }
}
