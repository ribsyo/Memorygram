package com.cmpt.memogram.classes;

import android.util.Log;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class UserManager {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference storageRef = storage.getReference();

    Map<String, Object> userDoc = new HashMap<>();

    public UserManager() {
        if (getID() != null) {
            this.getUserDoc(new onGetUserDocListener() {
                @Override
                public void onSuccess() {

                }
                @Override
                public void onFailure(String message) {
                    Log.d("userMan initialize", message);
                }
            });
        }
    }

    // Returns a bool dependent on if a user is logged in
    public boolean loginStatus() {
        return mAuth.getCurrentUser() != null;
    }

    // Gets User ID if user is logged in
    public String getID() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        return null;
    }

    // Get name of User
    public String getName() {
        if (userDoc.get("name") != null) {
            return userDoc.get("name").toString();
        }
        return null;
    }

    // Get groupID of User
    public String getGroupID() {
        if (userDoc.get("groupID") != null) {
            return userDoc.get("groupID").toString();
        }
        return null;
    }
    public String getRole() {
        if (userDoc.get("role") != null) {
            return userDoc.get("role").toString();
        }
        return null;
    }

    public String getEmail() {
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getEmail();
        }
        return null;
    }

    public String getImagePath() {
        if (userDoc.get("imagePath") != null) {
            return userDoc.get("imagePath").toString();
        }
        return "";
    }


    public interface onGetProfilePictureListener {
        void onSuccess(String downloadLink);
        void onFailure();
    }
    public void getProfilePicture(onGetProfilePictureListener listener) {
        if (userDoc.get("imagePath") != null) {
            getMedia(userDoc.get("imagePath").toString(), new OnGetFileListener() {
                @Override
                public void onSuccess(String downloadLink) {
                    listener.onSuccess(downloadLink);
                }

                @Override
                public void onFailure() {
                    listener.onSuccess("https://firebasestorage.googleapis.com/v0/b/memorygram-1b8ca.appspot.com/o/profilePictures%2FUntitled.jpg?alt=media&token=e97c16d5-86bd-4a86-8498-89148ad8ee0b");
                }
            });

        }

    }

    // Logs in with provided credentials returns true on success
    public interface onLoginListener {
        void onSuccess();
        void onFailure();
    }
    public void login(String username, String password, onLoginListener listener) {
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(login -> {
                    if (login.isSuccessful()) {
                        // Sign in success, updates app variables with user info
                        Log.d("login", "loginWithEmail:success");
                        getUserDoc(new onGetUserDocListener() {
                            @Override
                            public void onSuccess() {
                                listener.onSuccess();
                            }

                            @Override
                            public void onFailure(String message) {
                                Log.d("login", message);
                            }
                        });
                        listener.onSuccess();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("login", "loginUserWithEmail:failure", login.getException());
                        listener.onFailure();
                    }
                });
        loginStatus();
    }

    public void logout() {
        mAuth.signOut();
    }

    // Logs in with provided credentials returns true on success
    public interface onRegisterListener {
        void onSuccess();
        void onFailure(String message);
    }
    public void register(String username, String password, String name, String role, onRegisterListener listener) {
        mAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(register -> {
                    if (register.isSuccessful()) {
                        Log.d("register", "createUserWithEmail:success");
                        Map<String, String> newUser = new HashMap<>();
                        newUser.put("groupID", "");
                        newUser.put("role", role); //this is what i changed to accept role and name
                        newUser.put("name", name);
                        newUser.put("imagePath", "profilePictures/Untitled.jpg");

                        db.collection("Users").document(getID())
                                .set(newUser, SetOptions.merge());
                        listener.onSuccess();
                    } else {
                        Log.w("register", "createUserWithEmail:failure ", register.getException());
                        listener.onFailure(register.getException().getMessage());
                    }
                });
    }

    // Populate userMap
    public interface onGetUserDocListener {
        void onSuccess();
        void onFailure(String message);
    }
    public void getUserDoc(onGetUserDocListener listener) {
        DocumentReference docRef = db.collection("Users")
                .document(getID());
        docRef.get().addOnCompleteListener(getUser -> {
            if (getUser.isSuccessful()) {
                DocumentSnapshot document = getUser.getResult();
                if (document.exists()) {
                    userDoc.put("name", document.getData().get("name"));
                    userDoc.put("groupID", document.getData().get("groupID"));
                    userDoc.put("role", document.getData().get("role"));
                    userDoc.put("imagePath", document.getData().get("imagePath"));
                    listener.onSuccess();
                } else {
                    Log.d("getUser", "No such document");
                    listener.onFailure("No such document");
                }
            } else {
                Log.d("getUser", "get failed with ", getUser.getException());
                listener.onFailure(getUser.getException().getMessage());
            }
        });
    }

    // Joins a group by invite code string
    public interface onJoinGroupListener {
        void onSuccess();
        void onFailure();
    }
    public void joinGroup(String groupJoinCode, String role, onJoinGroupListener listener) {
        DocumentReference docRef = db.collection("Invites")
                .document(groupJoinCode.toUpperCase());
        docRef.get().addOnCompleteListener(getGroup -> {
            if (getGroup.isSuccessful()) {
                DocumentSnapshot document = getGroup.getResult();
                if (document.exists()) {
                    String groupJoinID = document.getData().get("groupID").toString();

                    // Update user
                    Map<String, Object> userUpdate = new HashMap<>();
                    userUpdate.put("groupID", groupJoinID);
                    userUpdate.put("role", role);
                    db.collection("Users").document(getID())
                            .set(userUpdate, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                // Update group
                                Map<String, Object> groupUpdate = new HashMap<>();
                                groupUpdate.put("name", getName());
                                groupUpdate.put("role", role);
                                groupUpdate.put("imagePath", userDoc.get("imagePath").toString());
                                db.collection("FamilyGroups")
                                        .document(groupJoinID).collection("Members")
                                        .document(getID())
                                        .set(groupUpdate, SetOptions.merge())
                                        .addOnSuccessListener(aVoid1 -> {
                                            db.collection("Invites").document(groupJoinCode.toUpperCase()).delete();
                                            listener.onSuccess();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d("joinGroup", "Failed to update group members");
                                            listener.onFailure();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.d("joinGroup", "Failed to update user");
                                listener.onFailure();
                            });
                } else {
                    Log.d("joinGroup", "No such invite document");
                    listener.onFailure();
                }
            } else {
                Log.d("joinGroup", "Get failed with ", getGroup.getException());
                listener.onFailure();
            }
        });
    }

    // Leaves group user is currently in
    public interface onLeaveGroupListener {
        void onSuccess();
        void onFailure();
    }
    // UserManager.java
    public void leaveGroup(onLeaveGroupListener listener) {
        // Update group
        db.collection("FamilyGroups")
                .document(getGroupID()).collection("Members").document(getID())
                .delete().addOnCompleteListener(delete -> {
                    if (!delete.isSuccessful()) {
                        listener.onFailure();
                        return;
                    }

                    // Update user
                    Map<String, Object> userUpdate = new HashMap<>();
                    userUpdate.put("groupID", "");
                    userUpdate.put("role", "");
                    db.collection("Users").document(getID())
                            .set(userUpdate, SetOptions.merge()).addOnCompleteListener(update -> {
                                if (!update.isSuccessful()) {
                                    listener.onFailure();
                                    return;
                                }
                                listener.onSuccess();
                            });
                });
    }

    // Creates a group
    public interface onCreateGroupListener {
        void onSuccess();
        void onFailure();
    }
    public void createGroup(String name, onCreateGroupListener listener) {
        Map<String, String> data = new HashMap<>();
        data.put("name", name);
        db.collection("FamilyGroups").add(data)
                .addOnSuccessListener(documentReference -> {
                    String createdGroupID = documentReference.getId();

                    // Sets creator as admin and joins
                    Map<String, Object> group = new HashMap<>();
                    group.put("admin", getID());
                    group.put("name", name);
                    group.put("tags", Arrays.asList("none"));
                    db.collection("FamilyGroups")
                            .document(createdGroupID)
                            .set(group, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                // Create an invite code for the new group
                                createInviteCode(createdGroupID, new onGetInviteCodeListener() {
                                    @Override
                                    public void onSuccess(String inviteCode) {
                                        joinGroup(inviteCode, "admin", new onJoinGroupListener() {
                                            @Override
                                            public void onSuccess() {
                                                Log.d("createGroup", "Created and joined");
                                                listener.onSuccess();
                                            }

                                            @Override
                                            public void onFailure() {
                                                Log.d("createGroup", "Failed to join group");
                                                listener.onFailure();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure() {
                                        Log.d("createGroup", "Failed to create invite code");
                                        listener.onFailure();
                                    }
                                });
                            })
                            .addOnFailureListener(e -> {
                                Log.d("createGroup", "Failed to set group data");
                                listener.onFailure();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.w("Create Group", "Error adding document", e);
                    listener.onFailure();
                });
    }

    private void createInviteCode(String groupID, onGetInviteCodeListener listener) {
        String inviteCode = randomCode();
        Map<String, String> inviteData = new HashMap<>();
        inviteData.put("groupID", groupID);
        db.collection("Invites").document(inviteCode)
                .set(inviteData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> listener.onSuccess(inviteCode))
                .addOnFailureListener(e -> listener.onFailure());
    }

    // Updates group role
    public interface onUpdateListener {
        void onSuccess();
        void onFailure();
    }
    public void update(String name, String email, String role, String pw, String imagePath, onUpdateListener listener) {
        AuthCredential credential = EmailAuthProvider
                .getCredential(getEmail(), pw);
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(reAuth -> {
                    if(!reAuth.isSuccessful()) {
                        listener.onFailure();
                    }
                    Log.d("reAUTH", "User re-authenticated.");

                    // Update group
                    Map<String, Object> groupUpdate = new HashMap<>();
                    groupUpdate.put("name", name);
                    groupUpdate.put("role", role);
                    groupUpdate.put("imagePath", imagePath);
                    db.collection("FamilyGroups")
                            .document(getGroupID()).collection("Members").document(getID())
                            .set(groupUpdate, SetOptions.merge()).addOnCompleteListener(update -> {
                                if(!update.isSuccessful()) {
                                    listener.onFailure();
                                }
                            });

                    // Update user
                    Map<String, Object> userUpdate = new HashMap<>();
                    userUpdate.put("name", name);
                    userUpdate.put("role", role);
                    userUpdate.put("imagePath", imagePath);
                    mAuth.getCurrentUser()
                            .updateEmail(email)
                            .addOnCompleteListener(update -> {
                                if (update.isSuccessful()) {
                                    Log.d("emailUpdate", "User email address updated.");
                                } else {
                                    Log.d("emailUpdate", "User email address update failed. " + update.getException().getMessage());
                                }
                            });

                    db.collection("Users").document(getID())
                            .set(userUpdate, SetOptions.merge()).addOnCompleteListener(update -> {
                                if(!update.isSuccessful()) {
                                    listener.onFailure();
                                }
                            });
                    listener.onSuccess();
                });

    }

    // Creates invite code
    public interface onGetInviteCodeListener {
        void onSuccess(String groupCode);
        void onFailure();
    }
    public void getInviteCode(final onGetInviteCodeListener listener) {
        if (getGroupID() == null || Objects.equals(getGroupID(), "")) {
            Log.d("getInviteCode", "groupID null or blank");
            listener.onFailure();
        }

        AtomicReference<String> groupCode = new AtomicReference<>(randomCode());
        DocumentReference docRef = db.collection("Invites")
                .document(randomCode());
        docRef.get().addOnCompleteListener(checkCode -> {
            if (checkCode.isSuccessful()) {
                DocumentSnapshot document = checkCode.getResult();
                if (document.exists()) {
                    getInviteCode(listener);
                } else {
                    Map<String, String> newInvite= new HashMap<>();
                    newInvite.put("groupID", getGroupID());
                    db.collection("Invites").document(groupCode.get())
                            .set(newInvite, SetOptions.merge());
                    listener.onSuccess(groupCode.get());
                }
            } else {
                Log.d("getInviteCode", "get failed with ", checkCode.getException());
                listener.onFailure();
            }
        });
    }


    //generates random string for invite code
    //helper function for getInviteCode
    String randomCode() {
        StringBuilder code = new StringBuilder();
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int codeLength = 4;

        for (int i = 0; i < codeLength; i++) {
            code.append(alpha.charAt((int) Math.floor(Math.random() * 26)));
        }
        Log.d("randomCode", code.toString());
        return code.toString();
    }
    public interface onGetGroupMembersListener {
        void onSuccess(List<User> users);
        void onFailure();
    }
    public void getGroupMembers(onGetGroupMembersListener listener) {
        CollectionReference membersCollection = db.collection("FamilyGroups")
                .document(getGroupID())
                .collection("Members");

        membersCollection.get().addOnCompleteListener(getMembers -> {
            if (getMembers.isSuccessful()) {
                List<User> members = new ArrayList<>();
                for (QueryDocumentSnapshot document : getMembers.getResult()) {
                    User member = new User();
                    member.ID = document.getId();
                    member.name = document.getData().get("name") != null ?
                            document.getData().get("name").toString() : "NAME BLANK";
                    member.imagePath = document.getData().get("imagePath").toString();
                    getMedia(member.imagePath, new OnGetFileListener() {
                        @Override
                        public void onSuccess(String downloadLink) {
                            member.imageDownloadLink = downloadLink;
                            member.role = document.getData().get("role") != null ?
                                    document.getData().get("role").toString() : "";
                            members.add(member);
                            listener.onSuccess(members);

                        }
                        @Override
                        public void onFailure() {
                            // Handle the failure
                            member.imageDownloadLink = "https://firebasestorage.googleapis.com/v0/b/memorygram-1b8ca.appspot.com/o/alexGroup%2F6dee7bd8-db42-4e34-970b-305a34b06e17.jpeg?alt=media&token=1a4db593-0523-4ba8-b547-11b62c48f4ca";
                            member.role = document.getData().get("role") != null ?
                                    document.getData().get("role").toString() : "";
                            members.add(member);
                            listener.onSuccess(members);
                        }
                    });
                }

            } else {
                Log.d("getGroupMembers", "Error getting documents: ", getMembers.getException());
                listener.onFailure();
            }
        });
    }

    public void getMedia(String path, final OnGetFileListener listener) {
        StorageReference fileRef = storageRef.child(path);

        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            // File download URL retrieved successfully
            listener.onSuccess(uri.toString());
        }).addOnFailureListener(exception -> {
            // Handle any errors
            listener.onFailure();
        });
    }

}