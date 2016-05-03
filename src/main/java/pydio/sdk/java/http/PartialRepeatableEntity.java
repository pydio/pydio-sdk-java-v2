package pydio.sdk.java.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Created by pydio on 06/02/2015.
 */
public class PartialRepeatableEntity implements HttpEntity {

    ContentStream customStream;
    HttpEntity mEntity;
    boolean mConsumed = false;

    public class ContentStream extends InputStream{

        InputStream originalStream;
        ByteBuffer buffer;
        int length = 0;
        boolean final_state = false, forget_buffer = false;

        public ContentStream(InputStream original){
            this(original, 1024);
        }

        public ContentStream(InputStream original, int bufferLength){
            this.originalStream = original;
            this.length = bufferLength;
            buffer = ByteBuffer.allocate(bufferLength);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int read = 0;
            if(!final_state){
                final_state = true;
                buffer.flip();
            }

            if(forget_buffer){
                read = originalStream.read(b, off, len);
                return read;
            }

            int remaining = buffer.remaining();

            if(len > remaining){
                buffer.get(b, off, remaining);
                forget_buffer = true;
                read = originalStream.read(b, off+remaining, len-remaining);
                return remaining + (read == -1 ? 0 : read);
            }

            buffer.get(b, off, len);
            read = remaining - buffer.remaining();
            forget_buffer = buffer.remaining() == 0;
            return read;
        }


        @Override
        public synchronized int read() throws IOException {
            if(!final_state){
                final_state = true;
                buffer.flip();
            }

            if(buffer.hasRemaining()) {
                byte b = buffer.get();
                return b;
            }

            return originalStream.read();
        }

        public synchronized int partialRead(byte[] array) throws IOException {
            if(final_state) throw new IOException("Cannot apply partialRead on the custom entity content since you've once called read()!");
            int buffer_cursor = 0;
            while(true){
                int read = originalStream.read();
                if(read == -1){
                    break;
                } else {
                    array[buffer_cursor] = (byte) read;
                    buffer_cursor++;
                    if(buffer_cursor == length){
                        break;
                    }
                }
            }
            buffer.put(array, 0, buffer_cursor);
            return buffer_cursor;
        }
    }

    public PartialRepeatableEntity(HttpEntity entity) throws IOException {
        this.customStream = new ContentStream(entity.getContent());
        mEntity = entity;
    }

    public PartialRepeatableEntity(HttpEntity entity, int length) throws IOException {
        this.customStream = new ContentStream(entity.getContent(), length);
        mEntity = entity;
    }
    @Override
    public boolean isRepeatable() {
        return false;
    }
    @Override
    public boolean isChunked() {
        return mEntity.isChunked();
    }
    @Override
    public long getContentLength() {
        return mEntity.getContentLength();
    }
    @Override
    public String getContentType() {
        return mEntity.getContentType();
    }
    @Override
    public String getContentEncoding() {
        return mEntity.getContentEncoding();
    }

    public InputStream getContent(){
        return customStream;
    }
    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        if(mConsumed) throw  new IOException("Entity consumed");

        byte[] buffer = new byte[4096];
        int read;

        InputStream in = getContent();
        while((read = in.read(buffer)) != -1){
            outstream.write(buffer, 0, read);
        }
        mConsumed = true;
    }
    @Override
    public boolean isStreaming() {
        return true;
    }
}
