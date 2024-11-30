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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cmpt.memogram.R;
import com.cmpt.memogram.classes.AppPreferences;
import com.cmpt.memogram.classes.OnDeletePostListener;
import com.cmpt.memogram.classes.Post;
import com.cmpt.memogram.classes.PostManager;
import com.cmpt.memogram.classes.User;
import com.cmpt.memogram.classes.UserManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_TUTORIAL = 0;
    private static final int VIEW_TYPE_POST = 1;
    private static final int VIEW_TYPE_POST_NO_AUDIO = 2;
    private final Context mContext;
    private final List<Post> mPosts;
    private boolean isTutorialExpanded = false;
    private boolean showTutorial;
    private UserManager userManager;
    private long lastLaunchTimestamp;
    private long currentSessionTimestamp;

    public PostAdapter(Context context, List<Post> posts, boolean showTutorial) {
        mContext = context;
        mPosts = posts;
        this.showTutorial = showTutorial;
        this.userManager = new UserManager();
        this.lastLaunchTimestamp = AppPreferences.getLastLaunchTimestamp(context);
        this.currentSessionTimestamp = System.currentTimeMillis();
    }

    @Override
    public int getItemViewType(int position) {
        if (showTutorial && position == 0) {
            return VIEW_TYPE_TUTORIAL;
        } else {
            Post post = mPosts.get(showTutorial ? position - 1 : position);
            if (post != null) {
                return post.includeAudio ? VIEW_TYPE_POST : VIEW_TYPE_POST_NO_AUDIO;
            } else {
                return VIEW_TYPE_POST_NO_AUDIO;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_TUTORIAL) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.tutorial_post_item, parent, false);
            return new TutorialViewHolder(view);
        } else if (viewType == VIEW_TYPE_POST_NO_AUDIO) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.post_item_no_audio, parent, false);
            return new PostViewHolder(view);
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
            ((PostViewHolder) holder).bind(post, lastLaunchTimestamp, currentSessionTimestamp);
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
        public TextView tagTextView;
        public View postContainer;
        private PostManager postManager;

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
            tagTextView = itemView.findViewById(R.id.tag);
            postContainer = itemView.findViewById(R.id.post_container);
            postManager = new PostManager(FirebaseFirestore.getInstance(), FirebaseStorage.getInstance(), "alexGroup", userManager.getID());
        }

        private void bind(Post post, long lastLaunchTimestamp, long currentSessionTimestamp) {
            try {
                description.setText(post.text);
                title.setText(post.title);
                postDate.setText(getRelativeTime(post.dateListed));
                tagTextView.setText("#" + post.tag);

                String imageUrl = post.imageDownloadLink;
                String audioUrl = post.audioDownloadLink;

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(mContext).load(imageUrl).into(postImage);
                }

                postImage.setOnClickListener(v -> showFullScreenImage(imageUrl));

                if (playAudio != null) {
                    if (post.includeAudio) {
                        playAudio.setVisibility(View.VISIBLE);
                        playAudio.setOnClickListener(v -> playAudio(audioUrl));
                    } else {
                        playAudio.setVisibility(View.GONE);
                    }
                }

                if (post.posterID.equals(userManager.getID())) {
                    editPostBtn.setVisibility(View.VISIBLE);
                    editPostBtn.setOnClickListener(v -> {
                        PopupMenu popupMenu = new PopupMenu(mContext, editPostBtn);
                        popupMenu.inflate(R.menu.popup_menu);
                        popupMenu.setOnMenuItemClickListener(item -> {
                            if (item.getItemId() == R.id.delete) {
                                postManager.deletePost(post.postID, new OnDeletePostListener() {
                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(mContext, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Toast.makeText(mContext, "Failed to delete post", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return true;
                            }
                            return false;
                        });
                        popupMenu.show();
                    });
                } else {
                    editPostBtn.setVisibility(View.GONE);
                }

                userManager.getGroupMembers(new UserManager.onGetGroupMembersListener() {
                    @Override
                    public void onSuccess(List<User> users) {
                        for (User user : users) {
                            if (user.ID.equals(post.posterID)) {
                                nameTextView.setText(user.name);
                                roleTextView.setText(user.role);
                                Glide.with(mContext).load(user.imageDownloadLink).into(profileImageView);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onFailure() {
                        System.out.println("PostAdapter: Failed to fetch group members");
                    }
                });

                // Convert timestamps to milliseconds
                long lastLaunchTimestampMillis = lastLaunchTimestamp * 1000;
                long currentSessionTimestampMillis = currentSessionTimestamp * 1000;

                // Highlight new posts based on timestamps and if not viewed
                long postTimestamp = getPostTimestamp(post.datePosted);
                System.out.println("PostAdapter: Post timestamp: " + postTimestamp);
                System.out.println("PostAdapter: Last launch timestamp: " + lastLaunchTimestampMillis);

                if (postTimestamp > lastLaunchTimestampMillis && !AppPreferences.isPostViewed(mContext, post.postID)) {
                    postContainer.setBackgroundColor(mContext.getResources().getColor(R.color.highlight_color));
                    System.out.println("PostAdapter: Post " + post.postID + " is new.");
                } else {
                    postContainer.setBackgroundColor(mContext.getResources().getColor(R.color.default_color));
                    System.out.println("PostAdapter: Post " + post.postID + " is not new.");
                }

                // Remove highlight on click
                postContainer.setOnClickListener(v -> {
                    postContainer.setBackgroundColor(mContext.getResources().getColor(R.color.default_color));
                    AppPreferences.setPostViewed(mContext, post.postID);
                });

                // Print viewed posts
                AppPreferences.printViewedPosts(mContext);

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("PostAdapter: Exception: " + e.getMessage());
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

        private long getPostTimestamp(String datePosted) {
            try {
                if (datePosted.contains("Timestamp")) {
                    String[] parts = datePosted.split("[=,)]");
                    long seconds = Long.parseLong(parts[1].trim());
                    return seconds * 1000;
                } else {
                    return Long.parseLong(datePosted);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }

        public String getRelativeTime(String datePosted) {
            try {
                long postTime = getPostTimestamp(datePosted);
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
}