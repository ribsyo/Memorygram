package com.cmpt.memogram.classes;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map<String, Object> userDoc = new HashMap<>();

    public UserManager() {
        if (getID() != null) {
            this.getUserDoc();
        }
    }

    public boolean loginStatus() {
        return mAuth.getCurrentUser() != null;
    }

    public String getID() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        return null;
    }

    public String getName() {
        if (userDoc.get("name") != null) {
            return userDoc.get("name").toString();
        }
        return null;
    }

    public String getGroupID() {
        if (userDoc.get("groupID") != null) {
            return userDoc.get("groupID").toString();
        }
        return null;
    }

    public void login(String username, String password, LoginCallback callback) {
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(login -> {
                    if (login.isSuccessful()) {
                        Log.d("login", "loginWithEmail:success");
                        getUserDoc();
                        callback.onLoginResult(true);
                    } else {
                        Log.w("login", "loginUserWithEmail:failure", login.getException());
                        callback.onLoginResult(false);
                    }
                });
    }

    public boolean register(String username, String password, String name) {
        mAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(register -> {
                    if (register.isSuccessful()) {
                        Log.d("register", "createUserWithEmail:success");
                        Map<String, String> newUser = new HashMap<>();
                        newUser.put("groupID", "");
                        newUser.put("name", name);

                        db.collection("Users").document(getID())
                                .set(newUser, SetOptions.merge());
                    } else {
                        Log.w("register", "createUserWithEmail:failure", register.getException());
                    }
                });
        return loginStatus();
    }

    private void getUserDoc() {
        DocumentReference docRef = db.collection("Users").document(getID());
        docRef.get().addOnCompleteListener(getUser -> {
            if (getUser.isSuccessful()) {
                DocumentSnapshot document = getUser.getResult();
                if (document.exists()) {
                    userDoc.put("name", document.getData().get("name"));
                    userDoc.put("groupID", document.getData().get("groupID"));
                } else {
                    Log.d("getUser", "No such document");
                }
            } else {
                Log.d("getUser", "get failed with ", getUser.getException());
            }
        });
    }

    public void joinGroup(String groupJoinID) {
        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("groupID", groupJoinID);
        db.collection("Users").document(getID())
                .set(userUpdate, SetOptions.merge());
        Map<String, Object> groupUpdate = new HashMap<>();
        groupUpdate.put("name", getName());
        db.collection("FamilyGroups")
                .document(groupJoinID).collection("Members").document(getID())
                .set(groupUpdate, SetOptions.merge());
    }

    public void leaveGroup() {
        db.collection("FamilyGroups")
                .document(getGroupID()).collection("Members").document(getID())
                .delete();
        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("groupID", "");
        db.collection("Users").document(getID())
                .set(userUpdate, SetOptions.merge());
    }

    public void createGroup(String name) {
        Map<String, String> data = new HashMap<>();
        data.put("name", name);
        db.collection("FamilyGroups").add(data)
                .addOnSuccessListener(documentReference -> {
                    String createdGroupID = documentReference.getId();
                    Map<String, String> admin = new HashMap<>();
                    admin.put("admin", getID());
                    db.collection("FamilyGroups")
                            .document(createdGroupID)
                            .set(admin, SetOptions.merge());
                    joinGroup(createdGroupID);
                })
                .addOnFailureListener(fail -> Log.w("Create Group", "Error adding document"));
    }

    public interface LoginCallback {
        void onLoginResult(boolean success);
    }
}