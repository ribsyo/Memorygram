package com.cmpt.memogram.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.cmpt.memogram.R;
import com.cmpt.memogram.classes.AppPreferences;
import com.cmpt.memogram.classes.UserManager;
import com.cmpt.memogram.ui.fragment.CollectionFragment;
import com.cmpt.memogram.ui.fragment.GroupSelectionFragment;
import com.cmpt.memogram.ui.fragment.HomeFragment;
import com.cmpt.memogram.ui.fragment.LoginFragment;
import com.cmpt.memogram.ui.fragment.PostFragment;
import com.cmpt.memogram.ui.fragment.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    UserManager userManager;
    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        // Save the last launch timestamp
        long currentTimestamp = System.currentTimeMillis() / 1000; // Current time in seconds
        AppPreferences.saveLastLaunchTimestamp(this, currentTimestamp);

        userManager = new UserManager();
        if (userManager.loginStatus()) {
            userManager.getUserDoc(new UserManager.onGetUserDocListener() {
                @Override
                public void onSuccess() {
                    checkGroupStatus();
                }

                @Override
                public void onFailure(String message) {
                    navigateToLogin();
                }
            });
        } else {
            navigateToLogin();
        }

        MaterialButton viewCollectionBtn = findViewById(R.id.view_collection_btn);
        viewCollectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new CollectionFragment());
            }
        });
    }

    public void checkGroupStatus() {
        String groupID = userManager.getGroupID();
        if (groupID == null || groupID.isEmpty()) {
            navigateToGroupSelection();
        } else {
            createMainContent();
        }
    }

    private void navigateToLogin() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new LoginFragment());
        transaction.addToBackStack(null); // Optional: add to back stack to allow user to navigate back
        transaction.commit();
    }

    public void navigateToGroupSelection() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new GroupSelectionFragment());
        transaction.commit();
    }

    public void createMainContent() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(navigationItemSelectedListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private BottomNavigationView.OnItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            int id = menuItem.getItemId();
            if (id == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            } else if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_post) {
                selectedFragment = new PostFragment();
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }
            return true;
        }
    };

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void leaveGroup() {
            userManager.leaveGroup(new UserManager.onLeaveGroupListener() {
                @Override
                public void onSuccess() {
                    navigateToGroupSelection();
                }

                @Override
                public void onFailure() {
                    Toast.makeText(MainActivity.this, "Failed to leave group", Toast.LENGTH_SHORT).show();
                }
            });
        }
}