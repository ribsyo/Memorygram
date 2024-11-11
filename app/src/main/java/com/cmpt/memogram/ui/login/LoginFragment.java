package com.cmpt.memogram.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import com.cmpt.memogram.R;

public class LoginFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button loginButton = view.findViewById(R.id.login_button);
        loginButton.setOnClickListener(v -> {
            // Close the LoginFragment and show the main content
            getActivity().getSupportFragmentManager().beginTransaction()
                    .remove(LoginFragment.this)
                    .commit();
            getActivity().findViewById(R.id.top_block).setVisibility(View.VISIBLE);
        });
    }
}