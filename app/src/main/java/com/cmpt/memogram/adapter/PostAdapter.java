package com.cmpt.memogram.adapter;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cmpt.memogram.R;
import com.cmpt.memogram.classes.Post;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private Context mContext;
    private List<Post> mPosts;

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
        Post post = mPosts.get(position);
        holder.description.setText(readFileContent(post.getDescriptionPath()));
        holder.title.setText(readFileContent(post.getTitlePath()));

        Picasso.get()
                .load("file:///android_asset/" + post.getImagePath())
                .placeholder(R.mipmap.cat) // Add a placeholder image
                .into(holder.postImage);
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView description;
        public TextView title;
        public ImageView postImage;

        public ViewHolder(View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.description);
            title = itemView.findViewById(R.id.title);
            postImage = itemView.findViewById(R.id.post_image);
        }
    }

    private String readFileContent(String filePath) {
        StringBuilder content = new StringBuilder();
        AssetManager assetManager = mContext.getAssets();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(filePath)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Content unavailable";
        }
        return content.toString().trim();
    }
}