package com.cmpt.memogram.ui.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.cmpt.memogram.R;
import com.cmpt.memogram.adapter.TagAdapter;
import com.cmpt.memogram.adapter.UserAdapter;
import com.cmpt.memogram.classes.PostManager;
import com.cmpt.memogram.classes.User;
import com.cmpt.memogram.classes.UserManager;
import com.cmpt.memogram.classes.OnGetPostNamesListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollectionFragment extends Fragment {

    private UserManager userManager;
    private PostManager postManager;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private TagAdapter tagAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection, container, false);

        userManager = new UserManager();
        postManager = new PostManager(FirebaseFirestore.getInstance(), FirebaseStorage.getInstance(), "testGroup", "testUser");
        recyclerView = view.findViewById(R.id.collection_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        Button sortByUserBtn = view.findViewById(R.id.sort_by_user_btn);
        Button sortByTagBtn = view.findViewById(R.id.sort_by_tag_btn);

        sortByUserBtn.setOnClickListener(v -> fetchAndDisplayGroupMembers());
        sortByTagBtn.setOnClickListener(v -> fetchAndDisplayTags());

        TextView titleTextView = view.findViewById(R.id.view_collection_tutorial_title);
        TextView descriptionTextView = view.findViewById(R.id.view_collection_tutorial_description);

        titleTextView.setOnClickListener(v -> {
            if (descriptionTextView.getVisibility() == View.GONE) {
                descriptionTextView.setVisibility(View.VISIBLE);
                titleTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_up, 0);
            } else {
                descriptionTextView.setVisibility(View.GONE);
                titleTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_drop_down, 0);
            }
        });

        return view;
    }

    private void fetchAndDisplayGroupMembers() {
        userManager.getGroupMembers(new UserManager.onGetGroupMembersListener() {
            @Override
            public void onSuccess(List<User> users) {
                List<User> usersWithPosts = new ArrayList<>();
                int[] processedUsersCount = {0}; // To keep track of processed users

                for (User user : users) {
                    postManager.getAllPostNamesByPoster(user.ID, new OnGetPostNamesListener() {
                        @Override
                        public void onSuccess(List<String> postNames) {
                            if (!postNames.isEmpty()) {
                                usersWithPosts.add(user);
                            }
                            processedUsersCount[0]++;
                            if (processedUsersCount[0] == users.size()) {
                                // Sort users by name in alphabetical order
                                Collections.sort(usersWithPosts, (user1, user2) -> user1.name.compareToIgnoreCase(user2.name));
                                userAdapter = new UserAdapter(usersWithPosts, CollectionFragment.this);
                                recyclerView.setAdapter(userAdapter);
                            }
                        }

                        @Override
                        public void onFailure() {
                            processedUsersCount[0]++;
                            if (processedUsersCount[0] == users.size()) {
                                // Sort users by name in alphabetical order
                                Collections.sort(usersWithPosts, (user1, user2) -> user1.name.compareToIgnoreCase(user2.name));
                                userAdapter = new UserAdapter(usersWithPosts, CollectionFragment.this);
                                recyclerView.setAdapter(userAdapter);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure() {
                // Handle failure if needed
            }
        });
    }

    private void fetchAndDisplayTags() {
        postManager.getFamilyGroupTags(new OnGetPostNamesListener() {
            @Override
            public void onSuccess(List<String> tags) {
                if (tags != null) {
                    tagAdapter = new TagAdapter(tags, CollectionFragment.this);
                    recyclerView.setAdapter(tagAdapter);
                } else {
                    Log.e("CollectionFragment", "Tags list is null");
                }
            }

            @Override
            public void onFailure() {
                Log.e("CollectionFragment", "Failed to fetch tags");
            }
        });
    }

    public void openCollectionHomeFragment(String filterType, String filterValue) {
        CollectionHomeFragment fragment = new CollectionHomeFragment();
        Bundle args = new Bundle();
        args.putString("filterType", filterType);
        args.putString("filterValue", filterValue);
        args.putString("tagName", filterValue);
        fragment.setArguments(args);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}