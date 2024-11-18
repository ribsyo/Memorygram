package com.cmpt.memogram.classes;

// this is for callbacks
public interface OnGetFileListener {
    //this is for callbacks
    void onSuccess(String downloadLink);
    void onFailure();
}
