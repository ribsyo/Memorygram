package com.cmpt.memogram.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.cmpt.memogram.R;
import java.util.List;
import android.widget.ImageView;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {

    private Context context;
    private List<String> tagList;

    public TagAdapter(Context context, List<String> tagList) {
        this.context = context;
        this.tagList = tagList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tag_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String tag = tagList.get(position);
        holder.hashTag.setText(tag);
        holder.tagImage.setImageResource(R.drawable.ic_folder); // Ensure the image is always the same
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView hashTag;
        public ImageView tagImage;

        public ViewHolder(View itemView) {
            super(itemView);
            hashTag = itemView.findViewById(R.id.hash_tag);
            tagImage = itemView.findViewById(R.id.tag_image);
        }
    }
}