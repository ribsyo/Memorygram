package com.cmpt.memogram;

import com.cmpt.memogram.Post;

public interface OnGetBytesListener {
    //this is for callbacks
    void onSuccess(byte[] data);
    void onFailure();
}
