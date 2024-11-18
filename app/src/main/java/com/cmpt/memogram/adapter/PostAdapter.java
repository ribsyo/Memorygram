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
            holder.postDate.setText(post.getRelativeTime());

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