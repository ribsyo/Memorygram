package com.cmpt.memogram.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.cmpt.memogram.R;
import com.cmpt.memogram.classes.Post;
import com.cmpt.memogram.classes.PostManager;
import com.cmpt.memogram.classes.OnGetPostNamesListener;
import com.cmpt.memogram.classes.OnGetPostListener;
import com.cmpt.memogram.adapter.PostAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import java.util.ArrayList;
import java.util.List;

public class CollectionHomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postLists;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PostManager postManager;
    private static final String TAG = "CollectionHomeFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection_home, container, false);

        recyclerView = view.findViewById(R.id.collection_home_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postLists = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postLists, false);
        recyclerView.setAdapter(postAdapter);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this::refreshPosts);

        postManager = new PostManager(FirebaseFirestore.getInstance(), FirebaseStorage.getInstance(), "alexGroup", "testUser");

        // Retrieve the arguments passed and use them to filter posts
        Bundle args = getArguments();
        if (args != null) {
            String filterType = args.getString("filterType");
            String filterValue = args.getString("filterValue");
            String tagName = args.getString("tagName");

            // Set the title according to the tag name
            TextView titleTextView = view.findViewById(R.id.collection_home_title);
            titleTextView.setText(tagName);

            if ("tag".equals(filterType)) {
                filterPostsByTag(filterValue);
            }
        }

        return view;
    }

    private void refreshPosts() {
        // Assuming the filter type and value are stored in instance variables
        String filterType = getArguments().getString("filterType");
        String filterValue = getArguments().getString("filterValue");
        if ("tag".equals(filterType)) {
            filterPostsByTag(filterValue);
        }
    }

    private void filterPostsByTag(String tag) {
        postLists.clear();
        postManager.getPostNamesByTag(tag, new OnGetPostNamesListener() {
            @Override
            public void onSuccess(List<String> postNames) {
                if (postNames.isEmpty()) {
                    Log.d(TAG, "No posts found with tag: " + tag);
                    swipeRefreshLayout.setRefreshing(false);
                    return;
                }
                final int totalPosts = postNames.size();
                final int[] fetchedPosts = {0};
                List<Post> tempPostList = new ArrayList<>(totalPosts);

                for (int i = 0; i < totalPosts; i++) {
                    tempPostList.add(null);
                }

                for (int i = 0; i < totalPosts; i++) {
                    final int index = i;
                    String postName = postNames.get(i);
                    postManager.getPost(postName, new OnGetPostListener() {
                        @Override
                        public void onSuccess(Post post) {
                            tempPostList.set(index, post);
                            fetchedPosts[0]++;
                            if (fetchedPosts[0] == totalPosts) {
                                sortAndDisplayPosts(tempPostList);
                            }
                        }

                        @Override
                        public void onFailure() {
                            fetchedPosts[0]++;
                            if (fetchedPosts[0] == totalPosts) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure() {
                Log.e(TAG, "Failed to fetch post names for tag: " + tag);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void sortAndDisplayPosts(List<Post> posts) {
        try {
            posts.sort((p1, p2) -> {
                if (p1.dateListed == null || p2.dateListed == null) {
                    Log.e(TAG, "dateListed is null for one of the posts");
                    return 0;
                }
                // Extract seconds from the Timestamp string
                long date1 = extractSecondsFromTimestamp(p1.dateListed);
                long date2 = extractSecondsFromTimestamp(p2.dateListed);
                Log.d(TAG, "Comparing dates: " + date1 + " and " + date2);
                return Long.compare(date2, date1); // Reverse the order
            });
        } catch (NumberFormatException e) {
            Log.e(TAG, "Failed to parse dateListed", e);
        }
        Log.d(TAG, "All posts fetched and sorted, updating RecyclerView");
        postLists.clear();
        postLists.addAll(posts);
        postAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    private long extractSecondsFromTimestamp(String timestamp) {
        // Assuming the format is "Timestamp(seconds=..., nanoseconds=...)"
        String secondsString = timestamp.split(",")[0].split("=")[1];
        return Long.parseLong(secondsString);
    }
}