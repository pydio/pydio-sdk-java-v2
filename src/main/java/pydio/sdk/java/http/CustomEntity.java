package pydio.sdk.java.http;

import org.apache.http.HttpEntity;
import org.apache.http.entity.BasicHttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by pydio on 06/02/2015.
 */
public class CustomEntity extends BasicHttpEntity {

    ContentStream customStream;
    HttpEntity mEntity;

    public class ContentStream extends InputStream{

        InputStream originalStream;
        ByteBuffer buffer;
        boolean final_state = false, forget_buffer = false;

        public ContentStream(InputStream original){
            this.originalStream = original;
            buffer = ByteBuffer.allocate(1024);
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

        public synchronized int safeRead(byte[] array) throws IOException {
            if(final_state) throw new IOException("Cannot apply safeRead on the custom entity content since you've once called read()!");
            int len = originalStream.read(array);
            if(len != -1)
                buffer.put(array, 0, len);
            return len;
        }
    }

    public CustomEntity(HttpEntity entity) throws IOException {
        this.customStream = new ContentStream(entity.getContent());
        mEntity = entity;
        chunked = entity.isChunked();
    }

    public InputStream getContent(){
        return customStream;
    }
}
