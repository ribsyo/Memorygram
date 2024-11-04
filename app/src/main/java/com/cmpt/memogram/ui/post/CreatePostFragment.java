package com.cmpt.memogram.ui.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import com.cmpt.memogram.R;

public class CreatePostFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout using the renamed XML file
        View view = inflater.inflate(R.layout.post_layout, container, false);

        // Find the button by its ID
        Button postToMainBtn = view.findViewById(R.id.post_to_main_btn);

        // Set an OnClickListener on the button
        postToMainBtn.setOnClickListener(v -> {
            // Check if the fragment is attached to an activity
            if (getActivity() != null) {
                // Pop the back stack to navigate back to the previous fragment
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
    }
}