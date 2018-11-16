package com.pydio.sdk.core.model;

import com.pydio.sdk.core.Pydio;

/**
 * Created by jabar on 21/07/2016.
 */
public class Change {

    public String source, target, path, ws, md5, type;
    public long seq, mtime, task, id, size;


    public static Change parse(ContentValues values){
        Change event = new Change();
        event.source = values.getAsString(Pydio.CHANGE_SOURCE);
        event.target = values.getAsString(Pydio.CHANGE_TARGET);
        event.path = values.getAsString(Pydio.CHANGE_NODE_PATH);
        event.ws = values.getAsString(Pydio.WORKSPACE_ID);
        event.md5 = values.getAsString(Pydio.CHANGE_NODE_MD5);
        event.type = values.getAsString(Pydio.CHANGE_TYPE);

        event.seq = values.getAsLong(Pydio.CHANGE_SEQ);
        event.task = values.getAsLong(Pydio.TASK_ID);
        event.mtime = values.getAsLong(Pydio.CHANGE_NODE_MTIME);
        event.id = values.getAsLong(Pydio.CHANGE_NODE_ID);
        event.size = values.getAsLong(Pydio.CHANGE_NODE_BYTESIZE);
        return event;
    }

    public ContentValues values(){
        ContentValues values = new ContentValues();
        values.put(Pydio.CHANGE_SOURCE, source);
        values.put(Pydio.CHANGE_TARGET, target);
        values.put(Pydio.WORKSPACE_ID, ws);
        values.put(Pydio.CHANGE_NODE_PATH, path);
        values.put(Pydio.CHANGE_NODE_MD5, md5);
        values.put(Pydio.CHANGE_SEQ, seq);
        values.put(Pydio.CHANGE_NODE_MTIME, mtime);
        values.put(Pydio.TASK_ID, task);
        values.put(Pydio.CHANGE_NODE_BYTESIZE, size);
        values.put(Pydio.CHANGE_NODE_ID, id);
        return values;
    }
}
