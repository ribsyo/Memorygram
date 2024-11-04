package com.cmpt.memogram;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.sql.Timestamp;
import java.util.concurrent.CountDownLatch;

public class PostManager {
    private String fg = "testGroup";
    private FirebaseFirestore db;
    private FirebaseStorage fs;
    private StorageReference sr;
    PostManager(FirebaseFirestore db, FirebaseStorage fs, String fg){
        this.db = db;
        this.fg = fg;
        this.fs = fs;
        this.sr = fs.getReference();
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
                    } else {
                        Log.d(TAG, "No such document");
                    }

                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
                listener.onSuccess(post);
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

    //returns byte array of media given firebase storage path
    public void getMedia(String path, final OnGetBytesListener listener){
        ;
        final long ONE_MEGABYTE = 1024 * 1024;
        sr.child(path).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                listener.onSuccess(bytes);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

    }
}

