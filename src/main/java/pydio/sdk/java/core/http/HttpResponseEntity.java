package pydio.sdk.java.core.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import pydio.sdk.java.core.utils.ProgressListener;
import pydio.sdk.java.core.utils.Pydio;

/**
 * Created by jabar on 06/05/2016.
 */
public class HttpResponseEntity implements HttpEntity {

    InputStream mSource;
    String mContentType;
    String mContentEncoding;
    long mLength;

    public HttpResponseEntity(HttpURLConnection con){
        mLength = con.getContentLength();
        mContentEncoding = con.getContentEncoding();
        mContentType = con.getContentType();
        try {
            mSource = con.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            mLength = -1;
        }
    }
    @Override
    public boolean isRepeatable() {
        return false;
    }
    @Override
    public boolean isChunked() {
        return false;
    }
    @Override
    public long getContentLength() {
        return mLength;
    }
    @Override
    public String getContentType() {
        return mContentType;
    }
    @Override
    public String getContentEncoding() {
        return mContentEncoding;
    }
    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
        return mSource;
    }

    @Override
    public int writeTo(OutputStream outstream, long len) throws IOException {
        return 0;
    }

    public void writeTo(OutputStream out) throws IOException {
        int bufsize = Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE, read, totalRead = 0;
        byte[] buffer = new byte[bufsize];
        while (-1 != (read = mSource.read(buffer, 0, bufsize)) ){
            out.write(buffer, 0, read);
            totalRead += read;
        }
        mSource.close();
    }

    public void writeTo(OutputStream out, ProgressListener listener) throws IOException {
        int bufsize = Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE, read, totalRead = 0;
        byte[] buffer = new byte[bufsize];
        while (-1 != (read = mSource.read(buffer, 0, bufsize)) ){
            out.write(buffer, 0, read);
            totalRead += read;
            if(listener != null){
                listener.onProgress(totalRead);
            }
        }
        mSource.close();
    }
    @Override
    public boolean isStreaming() {
        return true;
    }
}
