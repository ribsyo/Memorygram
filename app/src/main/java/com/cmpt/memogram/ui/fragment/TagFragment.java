package com.cmpt.memogram.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.cmpt.memogram.R;

public class TagFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection_home, container, false);

        TextView tagNameTextView = view.findViewById(R.id.collection_home_title);
        if (getArguments() != null) {
            String tagName = getArguments().getString("tag_name");
            tagNameTextView.setText(tagName);
        }

        return view;
    }
}