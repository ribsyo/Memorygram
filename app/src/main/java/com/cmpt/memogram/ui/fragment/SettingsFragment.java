package com.cmpt.memogram.ui.fragment;

import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import com.cmpt.memogram.R;
import com.cmpt.memogram.classes.UserManager;
import com.cmpt.memogram.ui.MainActivity;

public class SettingsFragment extends Fragment {

    private UserManager userManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        userManager = new UserManager();

        EditText oneTimeCodeInput = view.findViewById(R.id.one_time_code_input);
        Button generateOneTimeCodeButton = view.findViewById(R.id.generate_one_time_code_btn);
        TextView oneTimeCodeDisplay = view.findViewById(R.id.one_time_code_display);
        Button leaveFamilyButton = view.findViewById(R.id.leave_family_btn);
        Button logoutButton = view.findViewById(R.id.logout_btn);

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
                                    navigateToGroupSelection();
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

        // Set an OnClickListener for the logout button
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userManager.logout();
                navigateToLogin();
            }
        });

        generateOneTimeCodeButton.setOnClickListener(v -> {
            userManager.getInviteCode(new UserManager.onGetInviteCodeListener() {
                @Override
                public void onSuccess(String groupCode) {
                    oneTimeCodeDisplay.setText("One-Time Code: " + groupCode);
                    oneTimeCodeDisplay.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFailure() {
                    Toast.makeText(getContext(), "Failed to generate one-time code", Toast.LENGTH_SHORT).show();
                }
            });
        });

        Button editProfileButton = view.findViewById(R.id.edit_profile_btn);
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new EditProfileFragment());
                transaction.addToBackStack(null); // Optional: add to back stack to allow user to navigate back
                transaction.commit();
            }
        });

        return view;
    }

    private void navigateToGroupSelection() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new GroupSelectionFragment());
        transaction.commit();
    }

    private void navigateToLogin() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new LoginFragment());
        transaction.commit();
    }
}