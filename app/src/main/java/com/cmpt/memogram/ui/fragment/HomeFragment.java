package com.cmpt.memogram.ui.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cmpt.memogram.R;
import com.cmpt.memogram.adapter.PostAdapter;
import com.cmpt.memogram.classes.OnGetPostListener;
import com.cmpt.memogram.classes.OnGetPostNamesListener;
import com.cmpt.memogram.classes.Post;
import com.cmpt.memogram.classes.PostManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postLists;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        postLists = new ArrayList<>();

        // Initialize SwipeRefreshLayout and set the refresh listener
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshPosts();
            }
        });

        // Initialize PostAdapter and set it to the RecyclerView
        postAdapter = new PostAdapter(getContext(), postLists);
        recyclerView.setAdapter(postAdapter);

        // Add scroll listener to control scrolling behavior
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(-1)) {
                    swipeRefreshLayout.setEnabled(true);
                } else {
                    swipeRefreshLayout.setEnabled(false);
                }
            }
        });

        // Load posts initially
        refreshPosts();

        return view;
    }

    // Method to refresh posts from Firebase
    private void refreshPosts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage fs = FirebaseStorage.getInstance();
        PostManager postManager = new PostManager(db, fs, "testGroup");

        postLists.clear();
        postManager.getAllPostNames(new OnGetPostNamesListener() {
            @Override
            public void onSuccess(List<String> postNames) {
                final int totalPosts = postNames.size();
                final int[] fetchedPosts = {0};
                List<Post> tempPostList = new ArrayList<>(totalPosts);

                // Initialize the list with null to maintain order
                for (int i = 0; i < totalPosts; i++) {
                    tempPostList.add(null);
                }

                // Fetch each post by its name
                for (int i = 0; i < totalPosts; i++) {
                    final int index = i;
                    String postName = postNames.get(i);
                    postManager.getPost(postName, new OnGetPostListener() {
                        @Override
                        public void onSuccess(Post post) {
                            // Set the post at the correct index
                            tempPostList.set(index, post);
                            fetchedPosts[0]++;
                            // If all posts are fetched, add them to postLists in reverse order
                            if (fetchedPosts[0] == totalPosts) {
                                for (int j = totalPosts - 1; j >= 0; j--) {
                                    postLists.add(tempPostList.get(j));
                                }
                                postAdapter.notifyDataSetChanged();
                                swipeRefreshLayout.setRefreshing(false); // Stop the refresh animation
                            }
                        }

                        @Override
                        public void onFailure() {
                            fetchedPosts[0]++;
                            // If all posts are fetched, stop the refresh animation
                            if (fetchedPosts[0] == totalPosts) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                            System.out.println("Failed to get post: " + postName);
                        }
                    });
                }
            }

            @Override
            public void onFailure() {
                System.out.println("Failed to get post names.");
                swipeRefreshLayout.setRefreshing(false); // Stop the refresh animation
            }
        });
    }
}