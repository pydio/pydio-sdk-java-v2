package pydio.sdk.java.core.utils;

/**
 * Created by jabar on 30/08/2016.
 */
public interface BucketUploadListener {
    boolean onNext(int count, int total);
    boolean onProgress(String file, long uploaded, long total);
}
