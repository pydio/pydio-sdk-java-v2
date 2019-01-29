package com.pydio.sdk.sync;

import com.pydio.sdk.sync.fs.Fs;

import java.util.List;

public interface MergeState {
    void updateSeq(Watch w);
    List<Watch> watches(List<Fs> fsList);
}
