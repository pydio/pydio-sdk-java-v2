package pydio.sdk.java.http;

import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.content.AbstractContentBody;

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
 * Created by jabar on 01/04/2016.
 */
public class HttpContentBody extends AbstractContentBody {

    long mLength;
    String mFilename;
    File mFile;
    InputStream mInStream;

    private int mChunkIndex;
    private int mChunkCount;

    private long mChunkSize;
    private long mLastChunkSize;


    public HttpContentBody(String filename, long length, long maxPartSize){
        super("application/octet-stream");
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

    public HttpContentBody(File file, String filename, long maxPartSize) throws FileNotFoundException {
        this(filename, file.length(), maxPartSize);
        mFile = file;
    }

    public HttpContentBody(byte[] bytes, String filename, long maxPartSize){
        this(new ByteArrayInputStream(bytes), filename, bytes.length, maxPartSize);
    }

    public HttpContentBody(InputStream in, String filename, long length, long maxPartSize){
        this(filename, length, maxPartSize);
        mInStream = in;
    }


    @Override
    public String getFilename() {
        return mFilename;
    }
    @Override
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
    public String getCharset() {
        return "UTF-8";
    }
    @Override
    public String getTransferEncoding() {
        return MIME.ENC_BINARY;
    }
    @Override
    public long getContentLength() {
        return mLength;
    }


    public int getCurrentIndex(){
        return mChunkIndex;
    }
    public int getTotalChunks(){
        return mChunkCount;
    }
    public boolean isChunked(){
        return mChunkCount > 0;
    }
    public boolean allChunksUploaded(){
        return mChunkIndex >= mChunkCount;
    }
    public String getRootFilename(){
        return mFilename;
    }


    private CountingMultipartRequestEntity.ProgressListener progressListener;

    public CountingMultipartRequestEntity.ProgressListener listener(){
        return progressListener;
    }

    public void setListener(CountingMultipartRequestEntity.ProgressListener listener){
        progressListener = listener;
    }
}
