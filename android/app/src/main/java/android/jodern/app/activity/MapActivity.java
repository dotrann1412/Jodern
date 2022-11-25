package android.jodern.app.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.jodern.app.R;
import android.os.Bundle;

public class MapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
    }
}