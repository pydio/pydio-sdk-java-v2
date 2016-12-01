package pydio.sdk.java.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pydio.sdk.java.core.PydioClient;
import pydio.sdk.java.core.security.Passwords;
import pydio.sdk.java.core.utils.Log;
import pydio.sdk.java.core.utils.PasswordLoader;

/**
 * Created by jabar on 01/12/2016.
 */

public class CMDClient {

    private static final String COOKIES_FOLDER = ".";

    private static CookieManager cm;

    private static CookieManager cm(final String url){
        if(cm == null){
            cm = new CookieManager() {
                CookieStore store;
                @Override
                public CookieStore getCookieStore() {
                    if(store == null){
                        store = new CookieStore() {
                            @Override
                            public void add(URI uri, HttpCookie c) {
                                String name = url.replace("://", "#").replace("/", "$");
                                String path = COOKIES_FOLDER + File.separator + name;
                                try {
                                    OutputStream out = new FileOutputStream(path);
                                    out.write(c.toString().getBytes());
                                    out.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public List<HttpCookie> get(URI uri) {
                                String name = url.replace("://", "#").replace("/", "$");
                                String path = COOKIES_FOLDER + File.separator + name;
                                try {
                                    InputStream in = new FileInputStream(path);
                                    byte[] content = new byte[1024];
                                    in.read(content);
                                    return HttpCookie.parse(new String(content));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return new ArrayList<HttpCookie>();
                            }

                            @Override
                            public List<HttpCookie> getCookies() {
                                String name = url.replace("://", "#").replace("/", "$");
                                String path = COOKIES_FOLDER + File.separator + name;
                                try {
                                    InputStream in = new FileInputStream(path);
                                    byte[] content = new byte[1024];
                                    in.read(content);
                                    return HttpCookie.parse(new String(content));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return new ArrayList<HttpCookie>();
                            }

                            @Override
                            public List<URI> getURIs() {
                                return null;
                            }

                            @Override
                            public boolean remove(URI uri, HttpCookie httpCookie) {
                                String name = url.replace("://", "#").replace("/", "$");
                                String path = COOKIES_FOLDER + File.separator + name;
                                return new File(path).delete();
                            }

                            @Override
                            public boolean removeAll() {
                                String name = url.replace("://", "#").replace("/", "$");
                                String path = COOKIES_FOLDER + File.separator + name;
                                return new File(path).delete();
                            }
                        };
                    }
                    return store;
                }
            };
        }
        return cm;
    }

    public static void main(String[] args) {
        final Map<String, String> params = new HashMap<>();
        String optionName = null;
        for (final String a : args) {
            if (a.charAt(0) == '-') {
                if (a.length() < 2) {
                    Log.e("Error", "at argument " + a);
                    return;
                }
                optionName = a.substring(1);

            } else if (optionName != null) {
                if("u".equals(optionName)){
                    optionName = "url";
                }
                params.put(optionName, a);
                optionName = null;

            } else {
                Log.e("Arguments", "Illegal parameter usage");
                return;
            }
        }

        String action = params.remove("action");

        String url = params.remove("url");
        int index = url.lastIndexOf("@");
        String user = url.substring(0, index);
        String address = url.substring(index+1);

        PydioClient client = new PydioClient(address);
        client.setCookieManager(cm(url));
        client.setUser(user);

        Passwords.Loader = new PasswordLoader() {
            @Override
            public String loadPassword(String url, String login) {
                char[] password = System.console().readPassword();
                return String.valueOf(password);
            }
        };

        try{
            String content = client.action(action, params);
            Log.i("", content);
        }catch (Exception e){
            Log.e("ERROR", e.getMessage());
        }
    }
}
