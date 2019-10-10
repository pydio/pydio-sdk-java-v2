package com.pydio.sdk.sync;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pydio.sdk.core.utils.io;
import com.pydio.sdk.sync.fs.Fs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FilePersistedMergeState implements MergeState {

    private final Integer lock = 1;
    private List<Watch> watches;
    private String filePath;

    public FilePersistedMergeState(String filePath) throws IOException {
        this.filePath = filePath;
        load();
    }
    @Override
    public void updateSeq(Watch w) {
        synchronized (lock) {
            boolean found = false;
            for(Watch item: watches) {
                if(w.equals(item)) {
                    item.setSeq(w.getSeq());
                    found = true;
                    break;
                }
            }
            if (!found) {
                watches.add(w);
            }
        }
    }
    @Override
    public synchronized List<Watch> watches(List<Fs> fsList) {
        synchronized (lock) {
            ArrayList<Watch> newWatches = new ArrayList<>();
            for (int i = 0; i < fsList.size(); i++) {
                for(int j = 0; j < fsList.size(); j++) {
                    if (i != j){
                        List<Watch> watches = this.watches(fsList.get(i), fsList.get(j));
                        if (watches != null && watches.size() > 0){
                            newWatches.addAll(watches);
                        }
                    }
                }
            }
            return newWatches;
        }
    }

    private List<Watch> watches(Fs fs1, Fs fs2){
        synchronized (lock) {
            if(fs1.id().equals(fs2.id())){
                return null;
            }

            List<String> paths = fs1.getWatches();
            List<Watch> newWatches = new ArrayList<>();

            for (String path: paths) {
                boolean found = false;
                for (Watch w: this.watches){
                    if (w.getSourceFs().equals(fs1.id()) && w.getTargetFs().equals(fs2.id()) && w.getPath().equals(path)) {
                        newWatches.add(w);
                        found = true;
                        break;
                    }
                }

                if (!found){
                    Watch w = new Watch();
                    w.setSourceFs(fs1.id());
                    w.setTargetFs(fs2.id());
                    w.setPath(path);
                    w.setSeq(0);
                    newWatches.add(w);
                }
            }

            this.watches = newWatches;
            return newWatches;
        }
    }

    public synchronized void save() throws IOException {
        synchronized (lock) {
            Gson gson = new Gson();
            String encoded = gson.toJson(watches);
            io.writeFile(encoded.getBytes(), this.filePath);
        }
    }

    public synchronized void load() throws IOException {
        synchronized (lock) {
            try{
                byte[] bytes = io.readFile(this.filePath);
                Gson gson = new Gson();
                Type t =  new TypeToken<List<Watch>>(){}.getType();
                String content = new String(bytes);
                watches = gson.fromJson(content, t);
            } catch (FileNotFoundException ignored){}
            if (watches == null){
                watches = new ArrayList<>();
            }
        }
    }
}
