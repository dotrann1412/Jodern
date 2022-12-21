package com.example.jodern.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.jodern.MainActivity;
import com.example.jodern.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed((Runnable) () -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                startActivity(new Intent(SplashActivity.this, AuthActivity.class));
            } else {
                System.out.println("Userid: " + user.getUid());
                System.out.println("User name: " + user.getDisplayName());
                System.out.println("Token: " + user.getIdToken(false));
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
            finish();
        }, 1000);
    }
}