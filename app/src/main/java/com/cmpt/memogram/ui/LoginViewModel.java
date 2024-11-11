package com.cmpt.memogram.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LoginViewModel extends ViewModel {
    private final MutableLiveData<String> username = new MutableLiveData<>();
    private final MutableLiveData<String> password = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();

    public LiveData<String> getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username.setValue(username);
    }

    public LiveData<String> getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password.setValue(password);
    }

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public void login() {
        // Add your login logic here
        // For example, check if username and password are correct
        if ("user".equals(username.getValue()) && "pass".equals(password.getValue())) {
            loginSuccess.setValue(true);
        } else {
            loginSuccess.setValue(false);
        }
    }
}