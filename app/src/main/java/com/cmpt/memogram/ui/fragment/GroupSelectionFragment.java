package com.cmpt.memogram.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import com.cmpt.memogram.R;
import com.cmpt.memogram.classes.UserManager;
import com.cmpt.memogram.ui.MainActivity;

public class GroupSelectionFragment extends Fragment {

    private UserManager userManager;
    private boolean isCreatingGroup = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_selection, container, false);

        userManager = new UserManager();

        EditText groupNameInput = view.findViewById(R.id.group_name_input);
        EditText oneTimeCodeInput = view.findViewById(R.id.one_time_code_input);
        Button joinGroupButton = view.findViewById(R.id.join_group_btn);
        Button createGroupButton = view.findViewById(R.id.create_group_btn);
        Button submitButton = view.findViewById(R.id.submit_btn);
        ConstraintLayout constraintLayout = view.findViewById(R.id.constraint_layout);

        joinGroupButton.setOnClickListener(v -> {
            isCreatingGroup = false;
            oneTimeCodeInput.setVisibility(View.VISIBLE);
            groupNameInput.setVisibility(View.GONE);
            submitButton.setVisibility(View.VISIBLE);
            submitButton.setText("Join Group");

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(R.id.submit_btn, ConstraintSet.TOP, R.id.one_time_code_input, ConstraintSet.BOTTOM, 16);
            constraintSet.applyTo(constraintLayout);
        });

        createGroupButton.setOnClickListener(v -> {
            isCreatingGroup = true;
            groupNameInput.setVisibility(View.VISIBLE);
            oneTimeCodeInput.setVisibility(View.GONE);
            submitButton.setVisibility(View.VISIBLE);
            submitButton.setText("Create Group");

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(R.id.submit_btn, ConstraintSet.TOP, R.id.group_name_input, ConstraintSet.BOTTOM, 16);
            constraintSet.applyTo(constraintLayout);
        });

        submitButton.setOnClickListener(v -> {
            if (isCreatingGroup) {
                String groupName = groupNameInput.getText().toString().trim();
                if (groupName.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter a group name", Toast.LENGTH_SHORT).show();
                    return;
                }

                userManager.createGroup(groupName, new UserManager.onCreateGroupListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Group created successfully", Toast.LENGTH_SHORT).show();
                        navigateToMainContent();
                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(getContext(), "Failed to create group", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                String oneTimeCode = oneTimeCodeInput.getText().toString().trim();
                if (oneTimeCode.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter a one-time code", Toast.LENGTH_SHORT).show();
                    return;
                }

                userManager.joinGroup(oneTimeCode, userManager.getRole(), new UserManager.onJoinGroupListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Joined group successfully", Toast.LENGTH_SHORT).show();
                        navigateToMainContent();
                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(getContext(), "Failed to join group", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return view;
    }

    private void navigateToMainContent() {
        ((MainActivity) getActivity()).checkGroupStatus();
    }
}