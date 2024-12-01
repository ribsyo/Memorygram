package com.cmpt.memogram.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.cmpt.memogram.R;
import com.cmpt.memogram.ui.fragment.CollectionFragment;
import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

    private List<String> tags;
    private CollectionFragment fragment;

    public TagAdapter(List<String> tags, CollectionFragment fragment) {
        this.tags = tags;
        this.fragment = fragment;
    }

    @Override
    public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_item, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TagViewHolder holder, int position) {
        String tag = tags.get(position);
        holder.tagName.setText(tag);
        holder.tagButton.setOnClickListener(v -> fragment.openCollectionHomeFragment("tag", tag));
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public static class TagViewHolder extends RecyclerView.ViewHolder {
        TextView tagName;
        ImageButton tagButton;

        public TagViewHolder(View itemView) {
            super(itemView);
            tagName = itemView.findViewById(R.id.tag_name);
            tagButton = itemView.findViewById(R.id.tag_btn);
        }
    }
}