package com.cmpt.memogram.classes;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class User {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    String id = null;
    String name = null;
    String groupID = null;

    //logs in with provided credentials
    public void login (String username, String password) {
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(login -> {
                    if (login.isSuccessful()) {
                        // Sign in success, updates app variables with user info
                        Log.d("login", "loginWithEmail:success");
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if(currentUser != null){
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
                                    } else {
                                        Log.d("getUser", "No such document");
                                    }
                                } else {
                                    Log.d("getUser", "get failed with ", getUser.getException());
                                }
                            });
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("login", "loginUserWithEmail:failure", login.getException());
                    }
                });
    }
    //Joins a group by groupID
    public void joinGroup (String groupJoinID) {
        Map<String, Object> data = new HashMap<>();
        data.put("groupID", groupJoinID);
        db.collection("Users").document(id)
                .set(data, SetOptions.merge());
    }
}
