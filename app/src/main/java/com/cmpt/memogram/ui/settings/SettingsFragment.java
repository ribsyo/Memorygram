package com.cmpt.memogram.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.cmpt.memogram.R;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.settings_layout, container, false);

        // Find the button in the layout
        Button settingsToMainButton = view.findViewById(R.id.settings_to_main_btn);

        // Set an OnClickListener for the button using a lambda expression
        settingsToMainButton.setOnClickListener(v -> {
            // Check if the fragment is attached to an activity
            if (getActivity() != null) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.popBackStack(); // This will close the fragment and return to the previous one
            }
        });

        return view;
    }
}