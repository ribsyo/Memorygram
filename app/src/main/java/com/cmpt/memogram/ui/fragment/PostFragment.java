package com.cmpt.memogram.ui.fragment;

import java.util.Date;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.cmpt.memogram.R;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.cmpt.memogram.classes.OnUploadPostListener;
import com.cmpt.memogram.classes.PostManager;
import com.cmpt.memogram.classes.UserManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import android.Manifest;
import android.database.Cursor;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class PostFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private ImageButton imageButton;
    private EditText captionEditText;
    private EditText tagsEditText;
    private EditText titleEditText;
    private Button postButton;
    private Button audioButton;
    private Button playbackButton;
    private Uri selectedImageUri;
    private byte[] imageBytes;
    private byte[] audioBytes;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String audioFilePath;
    private boolean isRecording = false;
    private boolean hasAudioRecording = false;

    private Button datePickerButton;
    private Date selectedDate;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        imageButton = view.findViewById(R.id.imageButton);
        captionEditText = view.findViewById(R.id.AddCaption);
        tagsEditText = view.findViewById(R.id.AddTags);
        titleEditText = view.findViewById(R.id.AddTitle);
        postButton = view.findViewById(R.id.Post);
        audioButton = view.findViewById(R.id.Audio);
        playbackButton = view.findViewById(R.id.PlayBack);
        datePickerButton = view.findViewById(R.id.DatePickerButton);

        selectedDate = new Date();
        updateDateButtonText();

        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions()) {
                    if (!isRecording) {
                        startRecording();
                    } else {
                        stopRecording();
                        hasAudioRecording = true;
                    }
                } else {
                    requestPermissions();
                }

            }
        });

        playbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioFilePath != null) {
                    if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
                        playAudio();
                    } else {
                        stopPlayback();
                    }
                } else {
                    Toast.makeText(getContext(), R.string.toast_record_audio_first, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set click listener for post button
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImageUri != null) {

                    postButton.setEnabled(false);

                    String caption = captionEditText.getText().toString();
                    String tags = tagsEditText.getText().toString().trim();
                    String title = titleEditText.getText().toString();

                    String finalTags = tags.isEmpty() ? getString(R.string.no_tag) : tags;

                    prepareImageBytes();


                    if (hasAudioRecording) {
                        prepareAudioBytes();
                    }


                    PostData postData = new PostData(
                            imageBytes,
                            caption,
                            finalTags,
                            audioBytes
                    );

                    Toast.makeText(getContext(), R.string.toast_creating_post, Toast.LENGTH_SHORT).show();

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseStorage fs = FirebaseStorage.getInstance();
                    UserManager userManager = new UserManager();
                    PostManager postManager = new PostManager(db, fs, "alexGroup", userManager.getID());
                    OnUploadPostListener uploadListener = new OnUploadPostListener() {
                        @Override
                        public void onSuccess() {
                            System.out.println(getString(R.string.post_upload_success));
                            if (getActivity() != null) {
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.fragment_container, new HomeFragment())
                                        .commit();
                            }
                        }

                        @Override
                        public void onFailure() {
                            System.out.println(getString(R.string.post_upload_fail));
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        postButton.setEnabled(true);
                                        Toast.makeText(getContext(), R.string.toast_post_upload_failed, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    };

                    byte[] safeAudioBytes = (hasAudioRecording && audioBytes != null) ? audioBytes : new byte[0];

                    // Choose upload method based on audio presence
                    if (hasAudioRecording) {
                        postManager.uploadPost(title, caption, safeAudioBytes, imageBytes, finalTags, new Date(), uploadListener);
                    } else {
                        postManager.uploadPost(title, caption, imageBytes, finalTags, new Date(), uploadListener);
                    }

                } else {
                    Toast.makeText(getContext(), R.string.toast_image_required, Toast.LENGTH_SHORT).show();
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

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQUEST_CODE);
    }

    private void startRecording() {
        audioFilePath = getContext().getExternalCacheDir().getAbsolutePath() + "/audio_record.mp3";
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(audioFilePath);
        hasAudioRecording = false;

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            audioButton.setText("STOP");
            Toast.makeText(getContext(), "Recording started", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to start recording", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;
                audioButton.setText("AUDIO");
                Toast.makeText(getContext(), "Recording stopped", Toast.LENGTH_SHORT).show();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error stopping recording", Toast.LENGTH_SHORT).show();
            }
        }
        hasAudioRecording = true;
    }

    private void setHasAudioRecording(boolean hasAudio) {
        this.hasAudioRecording = hasAudio;
    }


    private void playAudio() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            playbackButton.setText("STOP");

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playbackButton.setText("PLAYBACK");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to play audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            playbackButton.setText("PLAYBACK");
        }
    }

    private void prepareAudioBytes() {
        if (audioFilePath != null) {
            try {
                File audioFile = new File(audioFilePath);
                if (audioFile.exists()) {
                    FileInputStream fis = new FileInputStream(audioFile);
                    audioBytes = new byte[(int) audioFile.length()];
                    fis.read(audioBytes);
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error preparing audio", Toast.LENGTH_SHORT).show();
            }
//        } else {
//            audioBytes = new byte[0];
        }
    }

    private void cleanupMediaResources() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (audioFilePath != null) {
            File audioFile = new File(audioFilePath);
            if (audioFile.exists()) {
                audioFile.delete();
            }
        }
        isRecording = false;
    }

    private void resetForm() {
        captionEditText.setText("");
        tagsEditText.setText("");
        imageButton.setImageResource(android.R.drawable.ic_menu_gallery);
        selectedImageUri = null;
        imageBytes = null;
        audioBytes = null;
        audioFilePath = null;
        audioButton.setText("AUDIO");
        playbackButton.setText("PLAYBACK");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanupMediaResources();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar selectedCalendar = Calendar.getInstance();
                        selectedCalendar.set(year, month, dayOfMonth);
                        selectedDate = selectedCalendar.getTime();
                        updateDateButtonText();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void updateDateButtonText() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        datePickerButton.setText(sdf.format(selectedDate));
    }



    public static class PostData {
        private byte[] imageBytes;
        private String caption;
        private String tags;
        private byte[] audioBytes;

        public PostData(byte[] imageBytes, String caption, String tags, byte[] audioBytes) {
            this.imageBytes = imageBytes;
            this.caption = caption;
            this.tags = tags;
            this.audioBytes = audioBytes;
        }

        // Getters
        public byte[] getImageBytes() { return imageBytes; }
        public String getCaption() { return caption; }
        public String getTags() { return tags; }
        public byte[] getAudioBytes() {return audioBytes; }
    }

}