package com.cmpt.memogram.classes;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PostManager {
    private String fg = "testGroup";
    private FirebaseFirestore db;
    private FirebaseStorage fs;
    private StorageReference sr;
    public PostManager(FirebaseFirestore db, FirebaseStorage fs, String fg){
        this.db = db;
        this.fg = fg;
        this.fs = fs;
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
                            public void onSuccess(File imageFile) {
                                finalPost.localImagePath = imageFile.getAbsolutePath();

                                //get audio
                                getMedia(finalPost.audioPath, new OnGetFileListener() {
                                    @Override
                                    public void onSuccess(File audiuoFile) {
                                        finalPost.localAudioPath = imageFile.getAbsolutePath();
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
                        listener.onSuccess(post);
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
        post.datePosted = dr.get("datePosted");
        return post;
    }



    public void getMedia(String path, final OnGetFileListener listener) {
        try {
            // Create a temporary file
            File localFile = File.createTempFile("media", null);

            sr.child(path).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // File downloaded successfully
                    listener.onSuccess(localFile);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    listener.onFailure();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            listener.onFailure();
        }
    }
}

