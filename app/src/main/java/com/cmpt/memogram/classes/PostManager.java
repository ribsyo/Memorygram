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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

public class PostManager {
    private String fg = "alexGroup";
    private String userID = "testUser";
    private FirebaseFirestore db;
    private FirebaseStorage fs;
    private StorageReference sr;

    public PostManager(FirebaseFirestore db, FirebaseStorage fs, String fg, String userID) {
        this.db = db;
        this.fg = "alexGroup";
        this.fs = fs;
        this.userID = userID;
        this.sr = this.fs.getReference();
    }

    //returns post and calls a callback function once post is generated
    public void getPost(String postName, final OnGetPostListener listener) {
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

                                if(finalPost.includeAudio) {
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
                                else {
                                    finalPost.audioDownloadLink = "https://firebasestorage.googleapis.com/v0/b/memorygram-1b8ca.appspot.com/o/testGroup%2F08873e69-2335-48aa-9814-2d9b9e774bff.mp3?alt=media&token=12a42a3b-7d40-4e4a-bb09-15c6dafb7077";
                                    listener.onSuccess(finalPost);
                                }
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
    private Post toPost(DocumentSnapshot dr) {
        Post post = new Post();
        post.title = (String) dr.get("title");
        post.text = (String) dr.get("text");
        post.audioPath = (String) dr.get("audioPath");
        post.imagePath = (String) dr.get("imagePath");
        post.datePosted = dr.get("datePosted").toString();
        post.dateListed = dr.get("dateListed").toString();
        post.posterID = (String) dr.get("posterID");
        post.tag = (String) dr.get("tag");
        post.includeAudio = (boolean) dr.get("includeAudio");
        return post;
    }


    //gets media file from storage
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

    //returns all post names and calls a callback function once post names are generated
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


    public void getFamilyGroupTags(final OnGetPostNamesListener listener) {
        DocumentReference familyGroupRef = this.db.collection("FamilyGroups").document(this.fg);

        familyGroupRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> tags = (List<String>) document.get("tags");
                        listener.onSuccess(tags);
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

    public void addFamilyGroupTag(String newTag, final OnCompleteListener<Void> listener) {
        DocumentReference familyGroupRef = this.db.collection("FamilyGroups").document(this.fg);

        familyGroupRef.update("tags", FieldValue.arrayUnion(newTag))
                .addOnCompleteListener(listener)
                .addOnFailureListener(e -> Log.d(TAG, "Error adding tag: ", e));
    }

    public void getPostNamesByTag(String tag, final OnGetPostNamesListener listener) {
        CollectionReference postsRef = this.db.collection("FamilyGroups").document(this.fg).collection("Posts");

        postsRef.whereEqualTo("tag", tag).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<String> postNames = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        postNames.add(document.getId());
                    }
                    listener.onSuccess(postNames);
                } else {
                    Log.d(TAG, "Error getting posts: ", task.getException());
                    listener.onFailure();
                }
            }
        });
    }

    public void getAllPostNamesByPoster(String user, final OnGetPostNamesListener listener) {
        CollectionReference postsCollection = this.db.collection("FamilyGroups").document(this.fg).collection("Posts");

        postsCollection.whereEqualTo("posterID", user).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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

    public void getAllPostNamesByDateListed(final OnGetPostNamesListener listener) {
        CollectionReference postsCollection = this.db.collection("FamilyGroups").document(this.fg).collection("Posts");

        postsCollection.orderBy("dateListed").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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

    public void uploadPost(String title, String text, byte[] imageData, String tag, Date dateListed, final OnUploadPostListener listener) {

        addFamilyGroupTag(tag, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Handle the success case
                    System.out.println("Tag added successfully.");
                    // Upload audio file
                    String imageFilePath = fg + "/" + UUID.randomUUID().toString() + "." + "jpeg";

                    StorageMetadata imageMetadata = new StorageMetadata.Builder()
                            .setContentType("image/jpeg")
                            .build();

                    // Upload image file
                    StorageReference imageRef = sr.child(imageFilePath);
                    imageRef.putBytes(imageData, imageMetadata)
                            .addOnSuccessListener(imageTaskSnapshot -> {
                                imageRef.getDownloadUrl().addOnSuccessListener(imageUri -> {
                                    // Create post object
                                    PostUpload newPost = new PostUpload();
                                    newPost.audioPath = "none";
                                    newPost.includeAudio = false;
                                    newPost.imagePath = imageFilePath;
                                    newPost.text = text;
                                    newPost.title = title;
                                    newPost.posterID = userID;
                                    newPost.datePosted = new Date();
                                    newPost.dateListed = dateListed;
                                    newPost.tag = tag;
                                    // Save post to Firestore
                                    db.collection("FamilyGroups").document(fg).collection("Posts")
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
                } else {
                    // Handle the failure case
                    System.out.println("Failed to add tag.");
                }
            }
        });


    }
    // uploads a post to the database
    public void uploadPost(String title, String text, byte[] audioData, byte[] imageData, String tag, Date dateListed, final OnUploadPostListener listener) {

        addFamilyGroupTag(tag, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Handle the success case
                    System.out.println("Tag added successfully.");
                    // Upload audio file
                    String audioFilePath = fg + "/" + UUID.randomUUID().toString() + "." + "mp3";
                    String imageFilePath = fg + "/" + UUID.randomUUID().toString() + "." + "jpeg";

                    StorageMetadata audioMetadata = new StorageMetadata.Builder()
                            .setContentType("audio/mpeg")
                            .build();

                    StorageMetadata imageMetadata = new StorageMetadata.Builder()
                            .setContentType("image/jpeg")
                            .build();

                    StorageReference audioRef = sr.child(audioFilePath);
                    audioRef.putBytes(audioData, audioMetadata)
                            .addOnSuccessListener(audioTaskSnapshot -> {
                                audioRef.getDownloadUrl().addOnSuccessListener(audioUri -> {
                                    // Upload image file
                                    StorageReference imageRef = sr.child(imageFilePath);
                                    imageRef.putBytes(imageData, imageMetadata)
                                            .addOnSuccessListener(imageTaskSnapshot -> {
                                                imageRef.getDownloadUrl().addOnSuccessListener(imageUri -> {
                                                    // Create post object
                                                    PostUpload newPost = new PostUpload();
                                                    newPost.audioPath = audioFilePath;
                                                    newPost.imagePath = imageFilePath;
                                                    newPost.includeAudio = true;
                                                    newPost.text = text;
                                                    newPost.title = title;
                                                    newPost.posterID = userID;
                                                    newPost.datePosted = new Date();
                                                    newPost.dateListed = dateListed;
                                                    newPost.tag = tag;
                                                    // Save post to Firestore
                                                    db.collection("FamilyGroups").document(fg).collection("Posts")
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
                } else {
                    // Handle the failure case
                    System.out.println("Failed to add tag.");
                }
            }
        });


    }


    //deletes Post from database
    public void deletePost(String postName, final OnDeletePostListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference postsRef = db.collection("FamilyGroups").document(this.fg).collection("Posts");

        postsRef.document(postName).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    String imagePath = document.getString("imagePath");
                    String audioPath = document.getString("audioPath");

                    // Delete image file
                    StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
                    imageRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> imageTask) {
                            if (imageTask.isSuccessful()) {
                                // Delete audio file
                                StorageReference audioRef = FirebaseStorage.getInstance().getReference().child(audioPath);
                                audioRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> audioTask) {
                                        if (audioTask.isSuccessful()) {
                                            // Delete post document
                                            postsRef.document(postName).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> deleteTask) {
                                                    if (deleteTask.isSuccessful()) {
                                                        listener.onSuccess();
                                                    } else {
                                                        listener.onFailure(deleteTask.getException());
                                                    }
                                                }
                                            });
                                        } else {
                                            listener.onFailure(audioTask.getException());
                                        }
                                    }
                                });
                            } else {
                                listener.onFailure(imageTask.getException());
                            }
                        }
                    });
                } else {
                    listener.onFailure(task.getException());
                }
            }
        });
    }
}

