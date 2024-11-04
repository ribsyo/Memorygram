package com.cmpt.memogram;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.cmpt.memogram.databinding.ActivityMainBinding;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
    }
    public void onStart() {
        super.onStart();
        mAuth.signInWithEmailAndPassword("test@test.ca", "PW12345")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("login", "loginWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("login", "loginUserWithEmail:failure", task.getException());
                }
            }
        });;
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            setContentView(binding.getRoot());
            reload();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage sr = FirebaseStorage.getInstance();;

        //creates a post manager instance on startup
        PostManager pm = new PostManager(db, sr, "testGroup");

        //get post function demo
        pm.getPost("testPost", new OnGetPostListener() {
            @Override
            public void onSuccess(Post post) {
                System.out.println(post.title);
                System.out.println(post.text);
                System.out.println(post.audioPath);
                System.out.println(post.imagePath);
                System.out.println(post.datePosted);

                pm.getMedia(post.imagePath, new OnGetBytesListener() {
                    @Override
                    public void onSuccess(byte[] data) {
                        System.out.println("Image retrieved");
                        System.out.println(data);
                    }

                    @Override
                    public void onFailure() {
                        //getting data failed
                    }
                });
            }



            @Override
            public void onFailure() {
                //getting post failed
            }

        });




    }
    public void reload() {
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

}