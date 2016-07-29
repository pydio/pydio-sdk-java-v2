package pydio.sdk.java.core.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jabar on 10/05/2016.
 */
public class HttpChunkedResponseInputStream extends InputStream {

    int mChunkCount;
    long mChunkLength, mChunkRead;
    InputStream mWrappedStream;
    boolean EOS;

    public HttpChunkedResponseInputStream(InputStream in){
        mChunkCount = 0;
        mChunkLength = -1;
        mChunkRead = 0;
        mWrappedStream = in;
        EOS = false;
    }
    @Override
    public int read() throws IOException {
        if(EOS) return -1;

        if(mChunkLength == -1){
            String stringChunkLen = "";

            while(true){

                int c =  mWrappedStream.read();
                if(EOS = (c == -1)){
                    return -1;
                }
                byte read = (byte) c;

                if(read == '\r'){
                    char second_read = (char) mWrappedStream.read();
                    if(EOS = "".equals(stringChunkLen)){
                        return -1;
                    }
                    mChunkLength = Long.parseLong(stringChunkLen, 16);
                    if(mChunkLength == 0){
                        return -1;
                    }
                    mChunkRead = 1;
                    mChunkCount++;
                    return mWrappedStream.read();
                }

                stringChunkLen += ((char) read);
            }
        }


        int c =  mWrappedStream.read();
        if(EOS = (c == -1)){
            return -1;
        }

        mChunkRead++;
        if(mChunkRead == mChunkLength){
            mChunkLength = -1;
            mWrappedStream.read();
            mWrappedStream.read();
        }
        return c;
    }
}
