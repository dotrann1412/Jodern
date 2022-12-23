package com.example.jodernstore.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.jodernstore.MainActivity;
import com.example.jodernstore.R;
import com.example.jodernstore.provider.GeneralProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        GeneralProvider.with(this);

        new Handler().postDelayed((Runnable) () -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null || GeneralProvider.with(this).getJWT() == null) {
                startActivity(new Intent(SplashActivity.this, AuthActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
            finish();
        }, 1000);
    }
}