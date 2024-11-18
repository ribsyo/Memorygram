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
    {this.getUserDoc();}

    //Returns a bool dependent on if a user is logged in
    public boolean loginStatus() {
        return  mAuth.getCurrentUser() != null;
    }

    //Gets User ID if user is logged in
    public String getID() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        return null;
    }

    //Get name of User
    public String getName(){
        if (userDoc.get("name") != null) {
            return userDoc.get("name").toString();
        }
        return null;
    }

    //Get groupID of User
    public String getGroupID() {
        if (userDoc.get("groupID") != null) {
            return userDoc.get("groupID").toString();
        }
        return null;
    }

    //Logs in with provided credentials returns true on success
    public boolean login (String username, String password) {
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(login -> {
                    if (login.isSuccessful()) {
                        // Sign in success, updates app variables with user info
                        Log.d("login", "loginWithEmail:success");
                        getUserDoc();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("login", "loginUserWithEmail:failure", login.getException());
                    }
                });
        return loginStatus();
    }
    public boolean register (String username, String password, String name) {
        mAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(register -> {
                        if (register.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("register", "createUserWithEmail:success");
                            Map<String, String> newUser = new HashMap<>();
                            newUser.put("groupID", "");
                            newUser.put("name", name);

                            db.collection("Users").document(getID())
                                    .set(newUser, SetOptions.merge());
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("register", "createUserWithEmail:failure", register.getException());
                        }
                });
        return loginStatus();
    }

    //populate userMap
    private void getUserDoc() {
        DocumentReference docRef = db.collection("Users")
                .document(getID());
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

    //Joins a group by groupID
    public void joinGroup (String groupJoinID) {
        //update user
        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("groupID", groupJoinID);
        db.collection("Users").document(getID())
                .set(userUpdate, SetOptions.merge());
        //update group
        Map<String, Object> groupUpdate = new HashMap<>();
        groupUpdate.put("name", getName());
        db.collection("FamilyGroups")
                .document(groupJoinID).collection("Members").document(getID())
                .set(groupUpdate, SetOptions.merge());
    }

    //Leaves group user is currently in
    public void leaveGroup() {
        //update group
        db.collection("FamilyGroups")
                .document(getGroupID()).collection("Members").document(getID())
                .delete();

        //update user
        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("groupID", "");
        db.collection("Users").document(getID())
                .set(userUpdate, SetOptions.merge());
    }

    //Creates a group
    public void createGroup(String name) {
        Map<String, String> data = new HashMap<>();
        data.put("name", name);
        db.collection("FamilyGroups").add(data)
                .addOnSuccessListener(documentReference -> {
                    String createdGroupID = documentReference.getId();

                    //sets creator as admin and joins
                    Map<String, String> admin = new HashMap<>();
                    admin.put("admin", getID());
                    db.collection("FamilyGroups")
                            .document(createdGroupID)
                            .set(admin, SetOptions.merge());
                    joinGroup(createdGroupID);
                })
                .addOnFailureListener(fail -> Log
                        .w("Create Group", "Error adding document"));
    }

    //TODO: Output members of group
    //public String[] getGroupMembers() {
    //   return null;
    //}
    //Outputs group id for invite function,
    //TODO: add functionality for one time join code?
    public String getGroupCode() {
        return getGroupID();
    }

    //TODO: Add invite functionality.
    //
}
