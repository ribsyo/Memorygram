package com.cmpt.memogram.classes;

// this is for callbacks
public interface OnDeletePostListener {
    void onSuccess();
    void onFailure(Exception e);
}