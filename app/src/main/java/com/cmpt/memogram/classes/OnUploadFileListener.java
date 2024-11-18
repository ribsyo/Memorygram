package com.cmpt.memogram.classes;

// this is for callbacks
public interface OnUploadFileListener {
    void onSuccess(String downloadUrl);
    void onFailure();
}