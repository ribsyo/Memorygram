package com.cmpt.memogram.classes;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

public class PostManager {
    private String fg = "testGroup";
    private String userID = "testUser";
    private FirebaseFirestore db;
    private FirebaseStorage fs;
    private StorageReference sr;
    public PostManager(FirebaseFirestore db, FirebaseStorage fs, String fg, String userID){
        this.db = db;
        this.fg = fg;
        this.fs = fs;
        this.userID = userID;
        this.sr = this.fs.getReference();
    }

    //returns post and calls a callback function once post is generated
    public void getPost(String postName, final OnGetPostListener listener){
        DocumentReference post = this.db.collection("FamilyGroups").document(this.fg).collection("Posts").document(postName);

        post.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Post post = null;
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        post = toPost(document);

                        Post finalPost = post;
                        //get image
                        getMedia(finalPost.imagePath, new OnGetFileListener() {
                            @Override
                            public void onSuccess(String downloadLink) {
                                System.out.println("got image file");
                                finalPost.imageDownloadLink = downloadLink;

                                //get audio
                                getMedia(finalPost.audioPath, new OnGetFileListener() {
                                    @Override
                                    public void onSuccess(String downloadLink) {
                                        finalPost.audioDownloadLink = downloadLink;
                                        listener.onSuccess(finalPost);
                                    }

                                    @Override
                                    public void onFailure() {
                                        // Handle the failure
                                        listener.onFailure();
                                    }
                                });
                            }

                            @Override
                            public void onFailure() {
                                // Handle the failure
                                listener.onFailure();
                            }
                        });
                    } else {
                        Log.d(TAG, "No such document");
                        listener.onFailure();
                    }

                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    listener.onFailure();
                }

            }
        });


    }


    //private method to map document data to a post object
    private Post toPost(DocumentSnapshot dr){
        Post post = new Post();
        post.title = (String)dr.get("title");
        post.text = (String)dr.get("text");
        post.audioPath = (String)dr.get("audioPath");
        post.imagePath = (String)dr.get("imagePath");
        post.datePosted = dr.get("datePosted").toString();
        return post;
    }



    public void getMedia(String path, final OnGetFileListener listener) {
        StorageReference fileRef = this.sr.child(path);

        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            // File download URL retrieved successfully
            listener.onSuccess(uri.toString());
        }).addOnFailureListener(exception -> {
            // Handle any errors
            listener.onFailure();
        });
    }

    public void getAllPostNames(final OnGetPostNamesListener listener) {
        CollectionReference postsCollection = this.db.collection("FamilyGroups").document(this.fg).collection("Posts");

        postsCollection.orderBy("datePosted").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<String> postNames = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        postNames.add(document.getId());
                    }
                    listener.onSuccess(postNames);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                    listener.onFailure();
                }
            }
        });
    }

    // OnUploadFileListener.java
    public void uploadFile(String filePath, byte[] fileData, final OnUploadFileListener listener) {
        StorageReference fileRef = this.sr.child(filePath);

        fileRef.putBytes(fileData)
                .addOnSuccessListener(taskSnapshot -> {
                    // File uploaded successfully
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // File download URL retrieved successfully
                        listener.onSuccess(uri.toString());
                    }).addOnFailureListener(exception -> {
                        // Handle any errors
                        listener.onFailure();
                    });
                })
                .addOnFailureListener(exception -> {
                    // Handle any errors
                    listener.onFailure();
                });
    }

    public void uploadPost(String title, String text, byte[] audioData, byte[] imageData, String userID, final OnUploadPostListener listener) {
        // Upload audio file
        String audioFilePath = fg + "/" + UUID.randomUUID().toString()+ "." + "mp3";
        String imageFilePath = fg + "/" + UUID.randomUUID().toString()+ "." + "jpeg";

        StorageMetadata audioMetadata = new StorageMetadata.Builder()
                .setContentType("audio/mpeg")
                .build();

        StorageMetadata imageMetadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        StorageReference audioRef = this.sr.child(audioFilePath);
        audioRef.putBytes(audioData, audioMetadata)
                .addOnSuccessListener(audioTaskSnapshot -> {
                    audioRef.getDownloadUrl().addOnSuccessListener(audioUri -> {
                        // Upload image file
                        StorageReference imageRef = this.sr.child(imageFilePath);
                        imageRef.putBytes(imageData, imageMetadata)
                                .addOnSuccessListener(imageTaskSnapshot -> {
                                    imageRef.getDownloadUrl().addOnSuccessListener(imageUri -> {
                                        // Create post object
                                        PostUpload newPost = new PostUpload();
                                        newPost.audioPath = audioFilePath;
                                        newPost.imagePath = imageFilePath;
                                        newPost.text = text;
                                        newPost.title = title;
                                        newPost.posterID = userID;
                                        newPost.datePosted = new Date();
                                        // Save post to Firestore
                                        this.db.collection("FamilyGroups").document(this.fg).collection("Posts")
                                                .add(newPost)
                                                .addOnSuccessListener(documentReference -> {
                                                    listener.onSuccess();
                                                })
                                                .addOnFailureListener(e -> {
                                                    listener.onFailure();
                                                });
                                    }).addOnFailureListener(e -> {
                                        listener.onFailure();
                                    });
                                }).addOnFailureListener(e -> {
                                    listener.onFailure();
                                });
                    }).addOnFailureListener(e -> {
                        listener.onFailure();
                    });
                }).addOnFailureListener(e -> {
                    listener.onFailure();
                });
    }

}

