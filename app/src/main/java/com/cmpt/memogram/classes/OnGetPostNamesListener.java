package com.cmpt.memogram.classes;

import java.util.List;

public interface OnGetPostNamesListener {
    void onSuccess(List<String> postNames);
    void onFailure();
}