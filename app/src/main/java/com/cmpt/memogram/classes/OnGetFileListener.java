package com.cmpt.memogram.classes;

public interface OnGetFileListener {
    //this is for callbacks
    void onSuccess(String downloadLink);
    void onFailure();
}
