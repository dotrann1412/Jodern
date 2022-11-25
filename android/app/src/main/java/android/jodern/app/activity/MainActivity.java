package android.jodern.app.activity;

import android.jodern.app.R;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.jodern.app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
}