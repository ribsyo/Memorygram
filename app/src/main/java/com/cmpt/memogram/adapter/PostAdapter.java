package com.cmpt.memogram.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    // Constructor to initialize context and post list
    public PostAdapter(Context context, List<Post> posts) {
        mContext = context;
        mPosts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
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
            System.out.println("Loading image URL: " + imageUrl);

            if (imageUrl == null || imageUrl.isEmpty()) {
                System.out.println("Error: Image URL is null or empty");
            } else {
                Glide.with(mContext)
                        .load(imageUrl)
                        .into(holder.postImage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in onBindViewHolder: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        // Return the total number of items in the list
        return mPosts.size();
    }

    public String getRelativeTime(String datePosted) {
        try {
            long postTime;
            if (datePosted.contains("Timestamp")) {
                // Assuming datePosted is in the format "Timestamp(seconds=..., nanoseconds=...)"
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

        // ViewHolder constructor to initialize views
        public ViewHolder(View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.description);
            title = itemView.findViewById(R.id.title);
            postImage = itemView.findViewById(R.id.post_image);
            postDate = itemView.findViewById(R.id.post_date);
        }
    }
}