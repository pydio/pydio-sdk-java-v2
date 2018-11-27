package com.pydio.sdk.core.common.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BufferedStream extends InputStream {

    private InputStream originalStream;
    private ByteArrayInputStream bytes;

    public BufferedStream(byte[] head, InputStream stream) {
        this.bytes = new ByteArrayInputStream(head);
        this.originalStream = stream;
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        int left = len;
        int count = 0;

        for (; left > 0; ) {
            int read = this.read();
            if (read == -1) {
                if (count > 0) {
                    return count;
                }
                return read;
            }
            buf[off + count] = (byte) read;
            count++;
            left--;
        }

        return count;
    }

    @Override
    public synchronized int read() throws IOException {
        int read = this.bytes.read();
        if (read == -1) {
            read = originalStream.read();
        }
        return read;
    }
}
