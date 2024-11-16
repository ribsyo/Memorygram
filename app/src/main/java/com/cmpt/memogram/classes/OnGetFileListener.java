package com.cmpt.memogram.classes;

import java.io.File;

public interface OnGetFileListener {
    //this is for callbacks
    void onSuccess(File data);
    void onFailure();
}
