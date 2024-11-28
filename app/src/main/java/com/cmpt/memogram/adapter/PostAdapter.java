package com.cmpt.memogram.adapter;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cmpt.memogram.R;
import com.cmpt.memogram.classes.Post;
import com.cmpt.memogram.classes.User;
import com.cmpt.memogram.classes.UserManager;
import com.cmpt.memogram.ui.fragment.CollectionHomeFragment;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_TUTORIAL = 0;
    private static final int VIEW_TYPE_POST = 1;
    private final Context mContext;
    private final List<Post> mPosts;
    private boolean isTutorialExpanded = false;
    private boolean showTutorial;
    private UserManager userManager;

    public PostAdapter(Context context, List<Post> posts, boolean showTutorial) {
        mContext = context;
        mPosts = posts;
        this.showTutorial = showTutorial;
        this.userManager = new UserManager();
    }

    @Override
    public int getItemViewType(int position) {
        if (showTutorial && position == 0) {
            return VIEW_TYPE_TUTORIAL;
        } else {
            return VIEW_TYPE_POST;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_TUTORIAL) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.tutorial_post_item, parent, false);
            return new TutorialViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
            return new PostViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_TUTORIAL) {
            ((TutorialViewHolder) holder).bind();
        } else {
            Post post = mPosts.get(showTutorial ? position - 1 : position);
            ((PostViewHolder) holder).bind(post);
        }
    }

    @Override
    public int getItemCount() {
        return showTutorial ? mPosts.size() + 1 : mPosts.size();
    }

    class TutorialViewHolder extends RecyclerView.ViewHolder {
        TextView tutorialTitle;
        TextView tutorialDescription;

        public TutorialViewHolder(View itemView) {
            super(itemView);
            tutorialTitle = itemView.findViewById(R.id.tutorial_title);
            tutorialDescription = itemView.findViewById(R.id.tutorial_description);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isTutorialExpanded = !isTutorialExpanded;
                    tutorialDescription.setVisibility(isTutorialExpanded ? View.VISIBLE : View.GONE);
                    tutorialTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, isTutorialExpanded ? R.drawable.ic_drop_top : R.drawable.ic_drop_down, 0);
                }
            });
        }

        public void bind() {
            tutorialDescription.setVisibility(isTutorialExpanded ? View.VISIBLE : View.GONE);
            tutorialTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, isTutorialExpanded ? R.drawable.ic_drop_top : R.drawable.ic_drop_down, 0);
        }
    }

    class PostViewHolder extends RecyclerView.ViewHolder {

        public TextView description;
        public TextView title;
        public ImageView postImage;
        public TextView postDate;
        public Button playAudio;
        public ImageView editPostBtn;
        public TextView nameTextView;
        public TextView roleTextView;
        public ImageView profileImageView;

        public PostViewHolder(View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.description);
            title = itemView.findViewById(R.id.title);
            postImage = itemView.findViewById(R.id.post_image);
            postDate = itemView.findViewById(R.id.post_date);
            playAudio = itemView.findViewById(R.id.play_audio);
            editPostBtn = itemView.findViewById(R.id.edit_post_btn);
            nameTextView = itemView.findViewById(R.id.poster);
            roleTextView = itemView.findViewById(R.id.role);
            profileImageView = itemView.findViewById(R.id.profile_image);
        }

        public void bind(Post post) {
            description.setText(post.text);
            title.setText(post.title);
            postDate.setText(getRelativeTime(post.dateListed)); // Use dateListed instead of datePosted

            String imageUrl = post.imageDownloadLink;
            String audioUrl = post.audioDownloadLink;

            if (imageUrl == null || imageUrl.isEmpty()) {
                System.out.println("Error: Image URL is null or empty");
            } else {
                Glide.with(mContext)
                        .load(imageUrl)
                        .into(postImage);
            }

            postImage.setOnClickListener(v -> showFullScreenImage(imageUrl));

            playAudio.setOnClickListener(v -> playAudio(audioUrl));

            editPostBtn.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(mContext, editPostBtn);
                popupMenu.inflate(R.menu.popup_menu);
                popupMenu.show();
            });

            // Fetch group members and bind user details
            userManager.getGroupMembers(new UserManager.onGetGroupMembersListener() {
                @Override
                public void onSuccess(List<User> users) {
                    for (User user : users) {
                        if (user.ID.equals(post.posterID)) {
                            System.out.println("PosterID: " + post.posterID);
                            System.out.println("User Name: " + user.name);
                            nameTextView.setText(user.name);
                            roleTextView.setText(user.role);
                            Glide.with(mContext).load(user.imageDownloadLink).into(profileImageView);

                            // Set OnClickListener to open CollectionHomeFragment
                            View.OnClickListener userClickListener = v -> openCollectionHomeFragment(user.name);
                            nameTextView.setOnClickListener(userClickListener);
                            profileImageView.setOnClickListener(userClickListener);

                            break;
                        }
                    }
                }

                @Override
                public void onFailure() {
                    Log.e("PostAdapter", "Failed to fetch group members");
                }
            });
        }

        private void openCollectionHomeFragment(String userName) {
            CollectionHomeFragment fragment = CollectionHomeFragment.newInstance(userName);
            ((FragmentActivity) mContext).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
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
            } else if (hours == 1) {
                return "an hour ago";
            } else if (hours > 1 && hours < 24) {
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
}