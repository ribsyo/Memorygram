package com.cmpt.memogram.ui.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cmpt.memogram.R;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.cmpt.memogram.R;
import com.cmpt.memogram.classes.OnUploadPostListener;
import com.cmpt.memogram.classes.PostManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.content.res.AssetManager;

public class PostFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageButton imageButton;
    private EditText captionEditText;
    private EditText tagsEditText;
    private Button postButton;
    private Uri selectedImageUri;
    private byte[] imageBytes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        imageButton = view.findViewById(R.id.imageButton);
        captionEditText = view.findViewById(R.id.AddCaption);
        tagsEditText = view.findViewById(R.id.AddTags);
        postButton = view.findViewById(R.id.Post);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        // Set click listener for post button
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImageUri != null) {
                    String caption = captionEditText.getText().toString();
                    String tags = tagsEditText.getText().toString();

                    prepareImageBytes();

                    PostData postData = new PostData(
                            imageBytes,
                            caption,
                            tags
                    );

                    String title = "first post!!!";
                    byte[] audioData = new byte[1];

                    Toast.makeText(getContext(), "Creating post...", Toast.LENGTH_SHORT).show();

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseStorage fs = FirebaseStorage.getInstance();
                    PostManager postManager = new PostManager(db, fs, "testGroup", "testUser");

                    postManager.uploadPost(title, caption, audioData, imageBytes, new OnUploadPostListener() {
                        @Override
                        public void onSuccess() {
                            System.out.println("Post uploaded successfully.");
                        }

                        @Override
                        public void onFailure() {
                            System.out.println("Failed to upload post.");
                        }
                    });

                } else {
                    Toast.makeText(getContext(), "Please select an image first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                imageButton.setImageURI(selectedImageUri);
            }
        }
    }

    private void prepareImageBytes() {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(selectedImageUri);
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            imageBytes = byteBuffer.toByteArray();

            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error preparing image", Toast.LENGTH_SHORT).show();
        }
    }

    public static class PostData {
        private byte[] imageBytes;
        private String caption;
        private String tags;

        public PostData(byte[] imageBytes, String caption, String tags) {
            this.imageBytes = imageBytes;
            this.caption = caption;
            this.tags = tags;
        }

        // Getters
        public byte[] getImageBytes() { return imageBytes; }
        public String getCaption() { return caption; }
        public String getTags() { return tags; }
    }

}