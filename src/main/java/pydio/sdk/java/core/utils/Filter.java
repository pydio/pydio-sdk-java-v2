package pydio.sdk.java.core.utils;

/**
 * Created by jabar on 16/11/2016.
 */

public interface Filter<T> {
    boolean isExcluded(T t);
}
