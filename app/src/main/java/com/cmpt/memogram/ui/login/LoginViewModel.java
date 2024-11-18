package com.cmpt.memogram.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.cmpt.memogram.classes.UserManager;
import android.util.Pair;

public class LoginViewModel extends ViewModel {
    private final MutableLiveData<String> username = new MutableLiveData<>();
    private final MutableLiveData<String> password = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<Pair<Boolean, String>> signUpSuccess = new MutableLiveData<>();
    private final UserManager userManager = new UserManager();

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

    public LiveData<Pair<Boolean, String>> getSignUpSuccess() {
        return signUpSuccess;
    }

    public void login() {
        userManager.login(username.getValue(), password.getValue(), success -> loginSuccess.setValue(success));
    }

    public void signUp(String username, String password) {
        boolean success = userManager.register(username, password, username);
        signUpSuccess.setValue(new Pair<>(success, success ? null : "Sign-up failed"));
    }
}