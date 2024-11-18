package com.cmpt.memogram.classes;

public interface OnGetBytesListener {
    //this is for callbacks
    void onSuccess(byte[] data);
    void onFailure();
}
