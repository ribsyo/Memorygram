package com.cmpt.memogram;

import com.cmpt.memogram.classes.PostManager;
import com.cmpt.memogram.classes.Post;
import com.cmpt.memogram.classes.OnGetPostListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.Test;

public class GetPostTest {
    @Test
    public void testGetPost() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage fs = FirebaseStorage.getInstance();
        PostManager postManager = new PostManager(db, fs, "testGroup", "testUser");

        postManager.getPost("testPost", new OnGetPostListener() {
            @Override
            public void onSuccess(Post post) {
                // Print out the text result
                System.out.println("Post text: " + post.text);
            }

            @Override
            public void onFailure() {
                System.err.println("Failed to get the post.");
            }
        });
    }
}