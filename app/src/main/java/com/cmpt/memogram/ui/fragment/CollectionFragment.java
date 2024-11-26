package com.cmpt.memogram.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cmpt.memogram.R;
import com.cmpt.memogram.adapter.UserAdapter;
import com.cmpt.memogram.adapter.TagAdapter;
import com.cmpt.memogram.classes.Post;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private TagAdapter tagAdapter;
    private List<Post> postList;
    private List<String> uniqueTags;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection, container, false);

        recyclerView = view.findViewById(R.id.collection_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns

        postList = new ArrayList<>();
        uniqueTags = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), postList);
        tagAdapter = new TagAdapter(getContext(), uniqueTags);
        recyclerView.setAdapter(userAdapter); // Default to userAdapter

        Button sortByUserBtn = view.findViewById(R.id.sort_by_user_btn);
        Button sortByTagBtn = view.findViewById(R.id.sort_by_tag_btn);

        sortByUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayExampleGrid();
            }
        });

        sortByTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayTagGrid();
            }
        });

        return view;
    }

    private void displayExampleGrid() {
        postList.clear();
        for (int i = 1; i <= 10; i++) {
            Post post = new Post();
            post.posterID = "User " + i;
            post.imageDownloadLink = "android.resource://" + getContext().getPackageName() + "/mipmap/cat";
            postList.add(post);
        }
        recyclerView.setAdapter(userAdapter);
        userAdapter.notifyDataSetChanged();
    }

    private void displayTagGrid() {
        Map<String, List<Post>> tagMap = new HashMap<>();
        for (Post post : postList) {
            if (!tagMap.containsKey(post.tag)) {
                tagMap.put(post.tag, new ArrayList<Post>());
            }
            tagMap.get(post.tag).add(post);
        }

        uniqueTags.clear();
        uniqueTags.addAll(tagMap.keySet());
        recyclerView.setAdapter(tagAdapter);
        tagAdapter.notifyDataSetChanged();
    }
}