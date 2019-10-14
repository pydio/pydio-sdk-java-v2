package com.pydio.sdk.sync.changes;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pydio.sdk.core.utils.io;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileWatchStore {
    private String filePath;
    private List<String> watches;

    private final Type classType = new TypeToken<List<String>>(){}.getType();

    public FileWatchStore(String filePath) throws IOException {
        this.filePath = filePath;
        load();
    }

    public synchronized void addWatches(List<String> list) {
        for(String w: list){
            if(watches.contains(w)){
                return;
            }

            for (String watch: watches) {
                if (watch.startsWith(w + "/")){
                    return;
                }
            }
            watches.add(w);
        }
        try {save();} catch (IOException ignored) {}
    }

    public synchronized void deleteWatch(String path) {
        Iterator it = watches.iterator();
        while(it.hasNext()) {
            String value = (String) it.next();
            if (value.equals(path)) {
                it.remove();
                break;
            }
        }
        try {save();} catch (IOException ignored) {}
    }

    public synchronized void addWatch(String w) {
        if(isWatched(w)){
            return;
        }
        watches.add(w);
        try {save();} catch (IOException ignored) {}
    }

    public synchronized boolean isWatched(String path) {
        return watches.contains(path);
    }

    public synchronized boolean isUnderWatched(String path) {
        if(watches.contains(path)){
            return false;
        }

        for (String watch: watches) {
            if (path.startsWith(watch)){
                return true;
            }
        }
        return false;
    }

    public synchronized List<String> getWatches() {
        return new ArrayList<>(watches);
    }

    private void save() throws IOException{
        Gson gson = new Gson();
        String encoded = gson.toJson(watches);
        io.writeFile(encoded.getBytes(), this.filePath);
    }

    private void load() throws IOException {
        try {
            byte[] bytes = io.readFile(this.filePath);
            Gson gson = new Gson();
            watches = gson.fromJson(new String(bytes), classType);
        } catch (Exception ignored){}
        if(watches == null){
            watches = new ArrayList<>();
        }
    }
}
