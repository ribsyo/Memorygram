package com.cmpt.memogram.adapter;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cmpt.memogram.R;
import com.cmpt.memogram.classes.Post;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Post> mPosts;

    public PostAdapter(Context context, List<Post> posts) {
        mContext = context;
        mPosts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            Post post = mPosts.get(position);
            holder.description.setText(post.text);
            holder.title.setText(post.title);
            holder.postDate.setText(getRelativeTime(post.datePosted));

            String imageUrl = post.imageDownloadLink;
            String audioUrl = post.audioDownloadLink;

            if (imageUrl == null || imageUrl.isEmpty()) {
                System.out.println("Error: Image URL is null or empty");
            } else {
                Glide.with(mContext)
                        .load(imageUrl)
                        .into(holder.postImage);
            }

            holder.postImage.setOnClickListener(v -> showFullScreenImage(imageUrl));

            holder.playAudio.setOnClickListener(v -> playAudio(audioUrl));

            holder.editPostBtn.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(mContext, holder.editPostBtn);
                popupMenu.inflate(R.menu.popup_menu);
//                popupMenu.setOnMenuItemClickListener(item -> {
//                    switch (item.getItemId()) {
//                        case R.id.edit:
//                            // Handle edit action
//                            return true;
//                        case R.id.delete:
//                            // Handle delete action
//                            return true;
//                        default:
//                            return false;
//                    }
//                });
                popupMenu.show();
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in onBindViewHolder: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    private void showFullScreenImage(String imageUrl) {
        Dialog dialog = new Dialog(mContext, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_fullscreen_image);
        ImageView fullscreenImage = dialog.findViewById(R.id.fullscreen_image);
        ImageButton closeButton = dialog.findViewById(R.id.close_button);

        Glide.with(mContext).load(imageUrl).into(fullscreenImage);

        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void playAudio(String audioUrl) {
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error playing audio: " + e.getMessage());
        }
    }

    public String getRelativeTime(String datePosted) {
        try {
            long postTime;
            if (datePosted.contains("Timestamp")) {
                String[] parts = datePosted.split("[=,)]");
                long seconds = Long.parseLong(parts[1].trim());
                postTime = seconds * 1000;
            } else {
                postTime = Long.parseLong(datePosted);
            }

            long currentTime = System.currentTimeMillis();
            long diff = currentTime - postTime;

            long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            long days = TimeUnit.MILLISECONDS.toDays(diff);

            if (seconds < 60) {
                return "just now";
            } else if (minutes == 1) {
                return "a minute ago";
            } else if (minutes > 1 && minutes < 60) {
                return minutes + " minutes ago";
            } else if (hours < 24) {
                return hours + " hours ago";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
                Date date = new Date(postTime);
                return sdf.format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown time";
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView description;
        public TextView title;
        public ImageView postImage;
        public TextView postDate;
        public Button playAudio;
        public ImageView editPostBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.description);
            title = itemView.findViewById(R.id.title);
            postImage = itemView.findViewById(R.id.post_image);
            postDate = itemView.findViewById(R.id.post_date);
            playAudio = itemView.findViewById(R.id.play_audio);
            editPostBtn = itemView.findViewById(R.id.edit_post_btn);
        }
    }
}