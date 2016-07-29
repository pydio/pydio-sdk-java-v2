package pydio.sdk.java.core.utils;

/**
 * Created by jabar on 24/06/2016.
 */
public class ApplicationData {

    public static String name = "";
    public static String version = "";
    public static int versionCode = 1;

    private static PersistentDataManager dataManager;

    public static PersistentDataManager manager(){
        return dataManager;
    }
    public static void setManager(PersistentDataManager m){
        dataManager = m;
    }
}
