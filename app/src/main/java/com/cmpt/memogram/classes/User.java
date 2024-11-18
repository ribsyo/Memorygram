package com.cmpt.memogram.classes;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.util.HashMap;
import java.util.Map;

public class User {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<Boolean> loginResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> signUpResult = new MutableLiveData<>();
    String id = null;
    String name = null;
    String groupID = null;

    public boolean isLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public void login(String username, String password) {
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(login -> {
                    if (login.isSuccessful()) {
                        Log.d("login", "loginWithEmail:success");
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null) {
                            String uID = currentUser.getUid();
                            DocumentReference docRef = db.collection("Users").document(uID);
                            docRef.get().addOnCompleteListener(getUser -> {
                                if (getUser.isSuccessful()) {
                                    DocumentSnapshot document = getUser.getResult();
                                    if (document.exists()) {
                                        Log.d("getUser", "DocumentSnapshot data: " + document.getData());
                                        id = uID;
                                        name = document.getString("name");
                                        groupID = document.getString("groupID");
                                        Log.d("getUser", groupID + " " + name);
                                        loginResult.setValue(true);
                                    } else {
                                        Log.d("getUser", "No such document");
                                        loginResult.setValue(false);
                                    }
                                } else {
                                    Log.d("getUser", "get failed with ", getUser.getException());
                                    loginResult.setValue(false);
                                }
                            });
                        }
                    } else {
                        Log.w("login", "loginUserWithEmail:failure", login.getException());
                        loginResult.setValue(false);
                    }
                });
    }

    public LiveData<Boolean> getLoginResult() {
        return loginResult;
    }

    public void joinGroup(String groupJoinID) {
        Map<String, Object> data = new HashMap<>();
        data.put("groupID", groupJoinID);
        db.collection("Users").document(id)
                .set(data, SetOptions.merge());
    }

    public void signUp(String username, String password, final SignUpCallback callback) {
        db.collection("Users").whereEqualTo("username", username).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Log.d("signUp", "Username already exists");
                        callback.onComplete(false, "Username already exists");
                    } else {
                        mAuth.createUserWithEmailAndPassword(username, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            FirebaseUser currentUser = mAuth.getCurrentUser();
                                            if (currentUser != null) {
                                                String uID = currentUser.getUid();
                                                Map<String, Object> userData = new HashMap<>();
                                                userData.put("username", username);
                                                db.collection("Users").document(uID).set(userData)
                                                        .addOnCompleteListener(userTask -> {
                                                            if (userTask.isSuccessful()) {
                                                                Log.d("signUp", "User data saved successfully");
                                                                callback.onComplete(true, null);
                                                            } else {
                                                                Log.w("signUp", "Failed to save user data", userTask.getException());
                                                                callback.onComplete(false, "Failed to save user data");
                                                            }
                                                        });
                                            } else {
                                                Log.w("signUp", "Current user is null");
                                                callback.onComplete(false, "Current user is null");
                                            }
                                        } else {
                                            Log.w("signUp", "createUserWithEmail:failure", task.getException());
                                            callback.onComplete(false, task.getException().getMessage());
                                        }
                                    }
                                });
                    }
                });
    }

    public interface SignUpCallback {
        void onComplete(boolean success, String errorMessage);
    }
}