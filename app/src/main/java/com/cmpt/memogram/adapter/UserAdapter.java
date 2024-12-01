package com.cmpt.memogram.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.cmpt.memogram.R;
import com.cmpt.memogram.classes.User;
import com.cmpt.memogram.ui.fragment.CollectionFragment;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> users;
    private CollectionFragment fragment;

    public UserAdapter(List<User> users, CollectionFragment fragment) {
        this.users = users;
        this.fragment = fragment;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.userName.setText(user.name);
        holder.userRole.setText(user.role);
        Glide.with(holder.itemView.getContext())
                .load(user.imageDownloadLink)
                .into(holder.userImageBtn);

        // Set click listener on the CircleImageView
        holder.userImageBtn.setOnClickListener(v -> fragment.openCollectionHomeFragment("user", user.ID));

        // Optionally, set click listener on the entire item view if needed
        holder.itemView.setOnClickListener(v -> fragment.openCollectionHomeFragment("user", user.ID));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView userRole;
        CircleImageView userImageBtn;

        public UserViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userRole = itemView.findViewById(R.id.role);
            userImageBtn = itemView.findViewById(R.id.user_image_btn);
        }
    }
}