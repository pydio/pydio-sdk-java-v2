package pydio.sdk.java.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URLConnection;

import pydio.sdk.java.utils.Pydio;

/**
 * Created by jabar on 29/04/2016.
 */
public class ContentBody implements HttpEntity{

    private long mLength;
    private long mCursor;
    private String mFilename;
    private File mFile;
    private InputStream mInStream;

    private int mChunkIndex;
    private int mChunkCount;

    private long mChunkSize;
    private long mLastChunkSize;

    private long mMaxChunckSize;

    private final String MIME = "application/octet-stream";


    public ContentBody(String filename, long length, long maxPartSize){
        mFilename = filename;
        mLength = length;

        //we remove 1 Mb just to make sure we do not exceed the client_upload_max_size
        maxPartSize = mMaxChunckSize = Math.min(maxPartSize, HttpClient.HTTP_DATA_BUFFER_MAX_SIZE) - 1024;
        if(maxPartSize >= length){
            mChunkCount = 1;
        } else {
            mChunkSize = maxPartSize;
            mChunkCount = (int) Math.ceil((float) mLength / mChunkSize);
            mLastChunkSize = mLength % this.mChunkSize;
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

    public ContentBody(InputStream in, long length){
        mLength = length;
        mInStream = in;
    }

    public String getFilename() {
        return mFilename;
    }

    public String getCharset() {
        return "utf-8";
    }


    public void setFilename(String label){
        mFilename = label;
    }

    @Override
    public boolean isStreaming() {
        return mInStream != null;
    }
    @Override
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
    public void writeTo(OutputStream out) throws IOException {

        long limit = mChunkSize;
        long bufsize = Math.min(Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE, mChunkSize);
        long start = mChunkIndex * mChunkSize, totalRead = start;

        if(mChunkCount > 1){


            if(mFile != null) {
                byte[] buffer = new byte[(int) Math.min(Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE, mChunkSize)];

                if (mChunkIndex == (mChunkCount - 1)) {
                    limit = mLastChunkSize;
                }

                RandomAccessFile raf = new RandomAccessFile(mFile, "r");
                raf.seek(start);
                int read, maximumToRead = (int) Math.min(bufsize, limit);

                while (limit > 0) {
                    read = raf.read(buffer, 0, maximumToRead);
                    out.write(buffer, 0, read);
                    totalRead += read;
                    if(progressListener != null){
                        progressListener.transferred(start + totalRead);
                    }
                    limit -= read;
                    maximumToRead = (int) Math.min(bufsize, limit);
                }
                raf.close();

            } else {

                byte[] buffer = new byte[(int) Math.min(Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE, mChunkSize)];
                if (mChunkIndex == (mChunkCount - 1)) {
                    limit = mLastChunkSize;
                }
                int maximumToRead = (int) Math.min(bufsize, limit), read;
                while (limit > 0) {
                    read = mInStream.read(buffer, 0, maximumToRead);
                    out.write(buffer, 0, read);
                    totalRead += read;
                    if(progressListener != null){
                        progressListener.transferred(start + totalRead);
                    }
                    limit -= read;
                    maximumToRead = (int) Math.min(bufsize, limit);
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
                totalRead += len;
                if(progressListener != null){
                    progressListener.transferred(totalRead);
                }
            }
            mInStream.close();
            mChunkIndex++;
        }

        System.out.println(mChunkIndex + "/" + mChunkCount + " Sent");
        System.out.println("Total sent size : " + totalRead + " bytes");
        if(progressListener != null){
            progressListener.partTransferred(mChunkIndex, mChunkCount);
        }
    }
    @Override
    public int writeTo(OutputStream out, long len) throws IOException {
        long writtenCount = 0;
        int bufsize = (int) Math.min(Math.min(Pydio.LOCAL_CONFIG_BUFFER_SIZE_DEFAULT_VALUE, len), available());
        byte[] buffer = new byte[bufsize];

        int read, maximumToRead = (int) Math.min(len, available());

        if(mFile != null) {
            RandomAccessFile raf = new RandomAccessFile(mFile, "r");
            raf.seek(mCursor);

            while (writtenCount < maximumToRead) {
                read = raf.read(buffer, 0, Math.min(bufsize, (int) (maximumToRead - writtenCount)));
                out.write(buffer, 0, read);
                writtenCount += read;
                if(progressListener != null){
                    progressListener.transferred(mCursor + writtenCount);
                }
            }
            raf.close();
        } else if(mInStream != null){

            while (writtenCount < maximumToRead) {
                read = mInStream.read(buffer, 0, Math.min(bufsize, (int) (maximumToRead - writtenCount)));
                out.write(buffer, 0, read);
                writtenCount += read;
                if(progressListener != null){
                    progressListener.transferred(mCursor + writtenCount);
                }
            }
        } else {
            throw new IOException("No source in content body");
        }

        mCursor += writtenCount;
        return maximumToRead;
    }
    @Override
    public boolean isRepeatable() {
        return false;
    }



    public boolean isChunked(){
        return mChunkCount > 1;
    }

    public boolean allChunksWritten(){
        return mChunkIndex >= mChunkCount || mCursor >= mLength;
    }

    public boolean lastChunk(){
        return isChunked() && mChunkIndex == (mChunkCount - 1);
    }

    public long maxChunkSize(){
        return mMaxChunckSize;
    }

    public long available(){
        return mLength - mCursor;
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
