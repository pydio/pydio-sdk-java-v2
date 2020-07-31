package com.pydio.sdk.core.encoding;

public abstract class B64 {

    private static B64 encoder = null;

    public static void set(B64 b) {
        encoder = b;
    }

    public static B64 get() {
        /*if (encoder == null) {
            return new DefaultB64Encoder();
        } */
        return encoder;
    }

    public abstract byte[] decode(byte[] data);
    public abstract byte[] encode(byte[] data);

    public abstract String decode(String s);
    public abstract String encode(String s);

    public abstract String decodeToString(byte[] data);
    public abstract String encodeToString(byte[] data);
}
