package com.pydio.sdk.core.common.http;

import java.io.IOException;
import java.io.InputStream;

public class BufferedStream extends InputStream {

    private InputStream originalStream;
    private byte[] head;
    private int cursor;

    public BufferedStream(byte[] head, InputStream stream) {
        this.head = head;
        this.originalStream = stream;
        cursor = 0;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read;

        if (cursor >= head.length) {
            read = originalStream.read(b, off, len);
            return read;
        }

        int totalRead = 0;
        int left = len;
        int buffPosition = off;

        for (; left > 0 && cursor < head.length; ) {
            byte o = head[cursor];
            left--;
            totalRead++;
            cursor++;

            b[buffPosition] = o;
            buffPosition++;
        }

        if (left > 0) {
            for (; left > 0 && (read = originalStream.read()) != -1; ) {
                left--;
                totalRead++;
                b[buffPosition] = (byte) read;
            }
        }

        return totalRead;
    }

    @Override
    public synchronized int read() throws IOException {
        if (cursor <= head.length) {
            byte b = head[cursor];
            cursor++;
            return b;
        }
        return originalStream.read();
    }
}
