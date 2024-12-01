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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.cmpt.memogram.R;
import com.cmpt.memogram.ui.LoginViewModel;
import com.cmpt.memogram.ui.MainActivity;

public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private boolean isSignUp = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View viewLine = getActivity().findViewById(R.id.view_line);
        if (viewLine != null) {
            viewLine.setVisibility(View.GONE);
        }

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        EditText emailEditText = view.findViewById(R.id.username);
        EditText passwordEditText = view.findViewById(R.id.password);
        EditText nameEditText = view.findViewById(R.id.name);
        EditText roleEditText = view.findViewById(R.id.role);
        Button loginButton = view.findViewById(R.id.login_button);
        Button signUpButton = view.findViewById(R.id.sign_up_button);
        Button submitButton = view.findViewById(R.id.submit_button);

        loginButton.setOnClickListener(v -> {
            isSignUp = false;
            emailEditText.setVisibility(View.VISIBLE);
            passwordEditText.setVisibility(View.VISIBLE);
            nameEditText.setVisibility(View.GONE);
            roleEditText.setVisibility(View.GONE);
            submitButton.setVisibility(View.VISIBLE);
        });

        signUpButton.setOnClickListener(v -> {
            isSignUp = true;
            emailEditText.setVisibility(View.VISIBLE);
            passwordEditText.setVisibility(View.VISIBLE);
            nameEditText.setVisibility(View.VISIBLE);
            roleEditText.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.VISIBLE);
        });

        submitButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isSignUp) {
                String name = nameEditText.getText().toString();
                String role = roleEditText.getText().toString();

                if (name.isEmpty() || role.isEmpty()) {
                    Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                loginViewModel.signUp(email, password, name, role);

                loginViewModel.getSignUpSuccess().observe(getViewLifecycleOwner(), result -> {
                    if (result.first) {
                        Toast.makeText(getContext(), "Sign-up successful", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Sign-up failed: " + result.second, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                loginViewModel.setUsername(email);
                loginViewModel.setPassword(password);
                loginViewModel.login();

                loginViewModel.getLoginSuccess().observe(getViewLifecycleOwner(), success -> {
                    if (success) {
                        ((MainActivity) getActivity()).createMainContent();
                    } else {
                        Toast.makeText(getContext(), "Login failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Show the view line when the LoginFragment is destroyed
        View viewLine = getActivity().findViewById(R.id.view_line);
        if (viewLine != null) {
            viewLine.setVisibility(View.VISIBLE);
        }
    }
}