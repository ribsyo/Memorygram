package com.cmpt.memogram.ui.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cmpt.memogram.R;
import com.cmpt.memogram.ui.settings.SettingsFragment;
import com.cmpt.memogram.ui.post.CreatePostFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Remove action bar if it exists
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);

        // Check for button clicks
        ImageButton settingsButton = findViewById(R.id.settings_btn);
        settingsButton.setOnClickListener(v -> {
            // Replace the current fragment with SettingsFragment
            SettingsFragment settingsFragment = new SettingsFragment();
            replaceFragment(settingsFragment);
        });

        // Set up the post button
        ImageButton postButton = findViewById(R.id.post_btn);
        postButton.setOnClickListener(v -> {
            // Replace the current fragment with CreatePostFragment
            CreatePostFragment createPostFragment = new CreatePostFragment();
            replaceFragment(createPostFragment);
        });

        // Add a back stack change listener to handle top block visibility
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                findViewById(R.id.top_block).setVisibility(View.VISIBLE);
            }
        });

        // Handle back press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                    // Show the top block layout
                    findViewById(R.id.top_block).setVisibility(View.VISIBLE);
                }
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    finish();
                }
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        // Hide the top block layout
        findViewById(R.id.top_block).setVisibility(View.GONE);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}