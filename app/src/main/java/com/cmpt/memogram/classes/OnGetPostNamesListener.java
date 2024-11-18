package com.cmpt.memogram.classes;

// this is for callbacks
import java.util.List;

public interface OnGetPostNamesListener {
    void onSuccess(List<String> postNames);
    void onFailure();
}
