package com.cmpt.memogram.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.cmpt.memogram.R;
import com.cmpt.memogram.classes.UserManager;

public class SettingsFragment extends Fragment {

    private UserManager userManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        userManager = new UserManager();

        EditText oneTimeCodeInput = view.findViewById(R.id.one_time_code_input);
        Button joinGroupButton = view.findViewById(R.id.join_group_btn);
        Button generateOneTimeCodeButton = view.findViewById(R.id.generate_one_time_code_btn);
        TextView oneTimeCodeDisplay = view.findViewById(R.id.one_time_code_display);

        joinGroupButton.setOnClickListener(v -> {
            if (oneTimeCodeInput.getVisibility() == View.GONE) {
                oneTimeCodeInput.setVisibility(View.VISIBLE);
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
                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(getContext(), "Failed to join group", Toast.LENGTH_SHORT).show();
                    }
                });
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
}