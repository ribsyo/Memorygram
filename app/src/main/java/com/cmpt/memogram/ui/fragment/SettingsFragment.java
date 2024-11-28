package com.cmpt.memogram.ui.fragment;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.cmpt.memogram.R;
import com.cmpt.memogram.classes.UserManager;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize UserManager
        UserManager userManager = new UserManager();

        // Find the leave family button
        Button leaveFamilyButton = view.findViewById(R.id.leave_family_btn);

        // Set an OnClickListener for the leave family button
        leaveFamilyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an AlertDialog to confirm the action
                new AlertDialog.Builder(getContext())
                        .setTitle("Leave Family Group")
                        .setMessage("Are you sure you want to leave the family group?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Call the leaveGroup method from UserManager
                            userManager.leaveGroup(new UserManager.onLeaveGroupListener() {
                                @Override
                                public void onSuccess() {
                                    // Handle success
                                    Toast.makeText(getContext(), "Left the family group successfully.", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure() {
                                    // Handle failure
                                    Toast.makeText(getContext(), "Failed to leave the family group.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            // Dismiss the dialog
                            dialog.dismiss();
                        })
                        .create()
                        .show();
            }
        });


        return view;
    }
}