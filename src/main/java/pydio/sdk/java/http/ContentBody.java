package pydio.sdk.java.http;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import pydio.sdk.java.utils.Pydio;

/**
 * Created by jabar on 29/04/2016.
 */
public class ContentBody implements HttpEntity{

    long mLength;
    String mFilename;
    File mFile;
    InputStream mInStream;

    private int mChunkIndex;
    private int mChunkCount;

    private long mChunkSize;
    private long mLastChunkSize;

    private final String MIME = "application/octet-stream";


    public ContentBody(String filename, long length, long maxPartSize){
        mFilename = filename;
        mLength = length;
        if(maxPartSize >= length){
            mChunkCount = 0;
        } else {
            mChunkSize = maxPartSize;
            mChunkCount = (int) Math.ceil((float) mLength / maxPartSize);
            if ((mLength % (float) this.mChunkSize) == 0) {
                mLastChunkSize = mChunkSize;
            } else {
                mLastChunkSize = (int) (mLength - (mChunkSize * (mChunkCount - 1)));
            }
        }
    }
    public ContentBody(File file, String filename, long maxPartSize) throws FileNotFoundException {
        this(filename, file.length(), maxPartSize);
        mFile = file;
    }
    public ContentBody(byte[] bytes, String filename, long maxPartSize){
        this(new ByteArrayInputStream(bytes), filename, bytes.length, maxPartSize);
    }
    public ContentBody(InputStream in, String filename, long length, long maxPartSize){
        this(filename, length, maxPartSize);
        mInStream = in;
    }

    public String getFilename() {
        return mFilename;
    }

    public String getCharset() {
        return "UTF-8";
    }

    public void writeTo(OutputStream out) throws IOException {

        if(mChunkCount > 0){
            long limit = mChunkIndex;
            long bufsize = Math.min(Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE, mChunkSize);

            if(mFile != null) {
                long start = mChunkIndex * mChunkSize;
                byte[] buffer = new byte[(int) Math.min(Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE, mChunkSize)];

                if (mChunkIndex == (mChunkCount - 1)) {
                    limit = mLastChunkSize;
                }

                RandomAccessFile raf = new RandomAccessFile(mFile, "r");
                raf.seek(start);
                int read;

                while (limit > 0) {
                    read = raf.read(buffer, 0, (int) Math.min(bufsize, limit));
                    out.write(buffer, 0, read);
                    limit -= read;
                }
                raf.close();

            } else {
                byte[] buffer = new byte[(int) Math.min(Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE, mChunkSize)];
                if (mChunkIndex == (mChunkCount - 1)) {
                    limit = mLastChunkSize;
                }

                int read;
                while (limit > 0) {
                    read = mInStream.read(buffer, 0, (int) Math.min(bufsize, limit));
                    out.write(buffer, 0, read);
                    limit -= read;
                }
            }
            mChunkIndex++;
        } else {
            if(mFile != null){
                mInStream = new FileInputStream(mFile);
            }
            byte[] buf = new byte[(int) Math.min(Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE, mLength)];
            int len;
            while ((len = mInStream.read(buf)) > 0){
                out.write(buf, 0, len);
            }
            mInStream.close();
        }
    }
    @Override
    public boolean isStreaming() {
        return mInStream != null;
    }

    public long getContentLength() {
        return mLength;
    }
    @Override
    public String getContentType() {
        return MIME;
    }
    @Override
    public String getContentEncoding() {
        return "binary";
    }
    @Override
    public InputStream getContent() throws IOException, IllegalStateException {
        return mInStream;
    }
    @Override
    public boolean isRepeatable() {
        return false;
    }

    public boolean isChunked(){
        return mChunkCount > 0;
    }

    public boolean allChunksWritten(){
        return mChunkIndex >= mChunkCount;
    }

    public interface ProgressListener {
        void transferred(long num) throws IOException;
        void partTransferred(int part, int total) throws IOException;
    }


    private ProgressListener progressListener;
    public ProgressListener listener(){
        return progressListener;
    }
    public void setListener(ProgressListener listener){
        progressListener = listener;
    }
}
