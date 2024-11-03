package com.cmpt.memogram;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;


import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.cmpt.memogram.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ActivityMainBinding binding;
    private static String name;
    private static String groupID;
    private static String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

    }
    public void onStart() {
        super.onStart();
        login("test@test.ca", "PW12345");
    }

   //logs in with provided credentials
   void login (String username, String password) {
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(login -> {
            if (login.isSuccessful()) {
                // Sign in success, updates app variables with user info
                Log.d("login", "loginWithEmail:success");
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if(currentUser != null){
                    String uID = currentUser.getUid();
                    DocumentReference docRef = db.collection("Users").document(uID);
                    docRef.get().addOnCompleteListener(getUser -> {
                        if (getUser.isSuccessful()) {
                            DocumentSnapshot document = getUser.getResult();
                            if (document.exists()) {
                                Log.d("getUser", "DocumentSnapshot data: " + document.getData());
                                userID = uID;
                                name = document.getString("name");
                                groupID = document.getString("groupID");
                                Log.d("getUser", groupID + " " + name);
                            } else {
                                Log.d("getUser", "No such document");
                            }
                        } else {
                            Log.d("getUser", "get failed with ", getUser.getException());
                        }
                    });
                }
            } else {
                // If sign in fails, display a message to the user.
                Log.w("login", "loginUserWithEmail:failure", login.getException());
            }
        });
    }
    //Joins a group by groupID
    void joinGroup (String groupJoinID) {
        Map<String, Object> data = new HashMap<>();
        data.put("groupID", "groupJoinID");
        db.collection("Users").document(userID)
                .set(data, SetOptions.merge());
    }
}


