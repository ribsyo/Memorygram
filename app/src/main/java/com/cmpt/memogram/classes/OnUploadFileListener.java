// OnUploadFileListener.java
package com.cmpt.memogram.classes;

public interface OnUploadFileListener {
    void onSuccess(String downloadUrl);
    void onFailure();
}