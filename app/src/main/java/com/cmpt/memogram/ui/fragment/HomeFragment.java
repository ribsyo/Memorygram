package com.cmpt.memogram.ui.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.material.button.MaterialButton;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postLists = new ArrayList<>();

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this::refreshPosts);

        postAdapter = new PostAdapter(getContext(), postLists, true);
        recyclerView.setAdapter(postAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                swipeRefreshLayout.setEnabled(!recyclerView.canScrollVertically(-1));
            }
        });

        MaterialButton scrollToTopBtn = view.findViewById(R.id.scroll_to_top_btn);
        MaterialButton scrollToBottomBtn = view.findViewById(R.id.scroll_to_bottom_btn);

        scrollToTopBtn.setOnClickListener(v -> recyclerView.smoothScrollToPosition(0));
        scrollToBottomBtn.setOnClickListener(v -> recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1));

        refreshPosts();

        return view;
    }

    public void refreshPosts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage fs = FirebaseStorage.getInstance();
        PostManager postManager = new PostManager(db, fs, "alexGroup", "testUser");

        postLists.clear();
        postManager.getAllPostNamesByDateListed(new OnGetPostNamesListener() {
            @Override
            public void onSuccess(List<String> postNames) {
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
                                for (int j = totalPosts - 1; j >= 0; j--) {
                                    postLists.add(tempPostList.get(j));
                                }
                                postAdapter.notifyDataSetChanged();
                                swipeRefreshLayout.setRefreshing(false);
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
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}