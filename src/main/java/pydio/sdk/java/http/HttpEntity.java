package pydio.sdk.java.http;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import pydio.sdk.java.utils.ProgressListener;

/**
 * Created by jabar on 29/04/2016.
 */
public interface HttpEntity {

    boolean isRepeatable();

    boolean isChunked();

    long getContentLength();

    String getContentType();

    String getContentEncoding();

    InputStream getContent() throws IOException, IllegalStateException;

    void writeTo(OutputStream outstream) throws IOException;

    int writeTo(OutputStream outstream, long len) throws IOException;

    boolean isStreaming(); // don't expect an exception here
}