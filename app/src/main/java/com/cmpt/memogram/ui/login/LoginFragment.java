package com.cmpt.memogram.ui.login;

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
import com.cmpt.memogram.classes.User;

public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        EditText emailEditText = view.findViewById(R.id.username);
        EditText passwordEditText = view.findViewById(R.id.password);
        Button loginButton = view.findViewById(R.id.login_button);
        Button signUpButton = view.findViewById(R.id.sign_up_button);

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            loginViewModel.setUsername(email);
            loginViewModel.setPassword(password);
            loginViewModel.login();

            loginViewModel.getLoginSuccess().observe(getViewLifecycleOwner(), success -> {
                if (success) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .remove(LoginFragment.this)
                            .commit();
                    getActivity().findViewById(R.id.top_block).setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getContext(), "Login failed", Toast.LENGTH_SHORT).show();
                }
            });
        });

        signUpButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            loginViewModel.signUp(email, password);

            loginViewModel.getSignUpSuccess().observe(getViewLifecycleOwner(), result -> {
                if (result.first) {
                    Toast.makeText(getContext(), "Sign-up successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Sign-up failed: " + result.second, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}