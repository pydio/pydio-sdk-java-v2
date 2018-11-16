package com.pydio.sdk.core.model;

import org.json.JSONObject;

/**
 * Created by jabar on 22/07/2016.
 */


/*{
        "uid":33,
        "atime":1469190128,
        "dev":2049,
        "blksize":4096,
        "nlink":4,
        "13":"directory",
        "rdev":0,
        "11":4096,
        "12":8,
        "mode":16832,
        "ino":3806660,
        "hash":"directory",
        "mtime":1469190097,
        "blocks":8,
        "ctime":1469190097,
        "size":4096,
        "3":4,
        "2":16832,
        "10":1469190097,"1":3806660,"0":2049,"7":4096,"6":0,"5":33,"4":33,"gid":33,"9":1469190097,"8":1469190128
}*/

public class Stats {
    public String dev, nlink, rdev, hash, blocks;
    public int mode;
    public long  atime, ino, blksize, mtime, ctime, size;

    public static Stats fromJSON(JSONObject json){
        Stats stats = new Stats();
        stats.hash = json.getString("hash");
        stats.blocks = json.getString("blocks");
        stats.rdev = json.getString("rdev");
        stats.nlink = json.getString("nlink");
        stats.dev = json.getString("dev");
        stats.mode = json.getInt("mode");
        stats.atime = json.getLong("atime");
        stats.ino = json.getLong("ino");
        stats.blksize = json.getLong("blksize");
        stats.mtime = json.getLong("mtime");
        stats.ctime = json.getLong("ctime");
        stats.size = json.getLong("size");
        return stats;
    }
}
