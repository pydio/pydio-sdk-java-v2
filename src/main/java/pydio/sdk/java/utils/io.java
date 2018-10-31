package pydio.sdk.java.utils;

import java.io.InputStream;
import java.io.OutputStream;

public class io {
    public static boolean close(InputStream in) {
        try {
            in.close();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean close(OutputStream out) {
        try {
            out.close();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
