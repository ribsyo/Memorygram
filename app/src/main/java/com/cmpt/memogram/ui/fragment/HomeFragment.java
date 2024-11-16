package com.cmpt.memogram.ui.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cmpt.memogram.R;
import com.cmpt.memogram.adapter.PostAdapter;
import com.cmpt.memogram.classes.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postLists;

    private List<String> followingList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        postLists = new ArrayList<>();

        // Initialize a sample Post object
        Post samplePost1 = new Post("cayden_yoo_trip_to_hawaii/trip_to_hawaii_description.txt", "cayden_yoo_trip_to_hawaii/maui.jpg", "cayden_yoo_trip_to_hawaii/title.txt");
        postLists.add(samplePost1);
        Post samplePost2 = new Post("cayden_yoo_trip_to_hawaii/trip_to_hawaii_description.txt", "cayden_yoo_trip_to_hawaii/maui.jpg", "cayden_yoo_trip_to_hawaii/title.txt");
        postLists.add(samplePost2);
        Post samplePost3 = new Post("cayden_yoo_trip_to_hawaii/trip_to_hawaii_description.txt", "cayden_yoo_trip_to_hawaii/maui.jpg", "cayden_yoo_trip_to_hawaii/title.txt");
        postLists.add(samplePost3);

        postAdapter = new PostAdapter(getContext(), postLists);
        recyclerView.setAdapter(postAdapter);

        // readPosts();

        return view;
    }

//    private void readPosts() {
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
//
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                postLists.clear();
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    Post post = snapshot.getValue(Post.class);
//                    for (String id : followingList) {
//                        //if (post.getPublisher().equals(id)) {
//                        //    postLists.add(post);
//                        //}
//                    }
//                }
//                postAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }
}