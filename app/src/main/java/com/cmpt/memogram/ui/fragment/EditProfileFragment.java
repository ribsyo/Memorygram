package com.cmpt.memogram.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.cmpt.memogram.R;
import com.cmpt.memogram.classes.OnUploadPostListener;
import com.cmpt.memogram.classes.UserManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private CircleImageView imagePreview;
    private TextView imageButton;
    private EditText nameEdit;
    private EditText emailEdit;
    private AutoCompleteTextView roleEdit;
    private Button saveButton;
    private Uri selectedImageUri;
    private byte[] imageBytes;
    private String selectedImageFileName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        imagePreview = view.findViewById(R.id.profile_picture);
        imageButton = view.findViewById(R.id.change_profile_picture_btn);
        nameEdit = view.findViewById(R.id.username);
        emailEdit = view.findViewById(R.id.email);
        roleEdit = view.findViewById(R.id.role);
        saveButton = view.findViewById(R.id.save_btn);
        UserManager um = new UserManager();
        um.getUserDoc(new UserManager.onGetUserDocListener() {
            @Override
            public void onSuccess() {
                nameEdit.setText(um.getName());
                emailEdit.setText(um.getEmail());
                roleEdit.setText(um.getRole());
            }

            @Override
            public void onFailure(String message) {

            }
        });
        nameEdit.setText(um.getName());
        emailEdit.setText(um.getEmail());
        roleEdit.setText(um.getRole());

        //back button
        ImageView backButton = view.findViewById(R.id.back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        //role dropdown
        AutoCompleteTextView roleDropdown = view.findViewById(R.id.role);
        String[] roles = getResources().getStringArray(R.array.Roles);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, R.id.textView, roles);
        roleDropdown.setAdapter(adapter);
        roleDropdown.setThreshold(1);

        //choose an image
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });


        // Set click listener for save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    saveButton.setEnabled(false);

                    String name = nameEdit.getText().toString();
                    String email = emailEdit.getText().toString().trim();
                    String role = roleEdit.getText().toString();
                    prepareImageBytes();


                    Toast.makeText(getContext(), "Saving...", Toast.LENGTH_SHORT).show();

                    //TODO upload image logic

                    um.update(name, email, role, "", new UserManager.onUpdateListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailure() {
                            Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });
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
                imagePreview.setImageURI(selectedImageUri);
                //get file name
                selectedImageFileName = getFileName(selectedImageUri);
                if (selectedImageFileName != null) {
                    Toast.makeText(getContext(), "Selected file: " + selectedImageFileName, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private void prepareImageBytes() {
        try {
            if (selectedImageUri == null) {return;}
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


    private void resetForm() {
        selectedImageUri = null;
        imageBytes = null;
        selectedImageFileName = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}