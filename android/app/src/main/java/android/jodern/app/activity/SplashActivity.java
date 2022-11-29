package android.jodern.app.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.jodern.app.MainActivity;
import android.jodern.app.R;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed((Runnable) () -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 3000);
    }
}