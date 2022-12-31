package com.example.jodernstore.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.jodernstore.BuildConfig;
import com.example.jodernstore.MainActivity;
import com.example.jodernstore.R;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.model.BranchInfo;
import com.example.jodernstore.model.CartItem;
import com.example.jodernstore.provider.BranchesProvider;
import com.example.jodernstore.provider.GeneralProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ConstraintLayout parentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        parentView = findViewById(R.id.splashParentView);

        mAuth = FirebaseAuth.getInstance();
        GeneralProvider.with(this);

        // Get branch info
        try {
            String entry = "stores-location";
            String url = BuildConfig.SERVER_URL + entry + "/";
            JsonObjectRequest getRequest = new JsonObjectRequest (
                    url,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray items = (JSONArray)response.get("branchs");
                                ArrayList<BranchInfo> branches = new ArrayList<>();
                                for (int j = 0; j < items.length(); j++) {
                                    JSONObject item = (JSONObject)items.get(j);
                                    int id = item.getInt("branch_id");
                                    String name = item.getString("branch_name");
                                    BranchInfo info = new BranchInfo(id, name);
                                    branches.add(info);
                                }
                                BranchesProvider.getInstance().setBranches(branches);

                                new Handler().postDelayed((Runnable) () -> {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user == null || GeneralProvider.with(SplashActivity.this).getJWT() == null) {
                                        startActivity(new Intent(SplashActivity.this, AuthActivity.class));
                                    } else {
                                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                                    }
                                    finish();
                                }, 500);
                            } catch (Exception e) {
                                e.printStackTrace();
                                MySnackbar.inforSnackar(SplashActivity.this, parentView, getString(R.string.error_message)).show();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            MySnackbar.inforSnackar(SplashActivity.this, parentView, getString(R.string.error_message)).show();
                        }
                    }
            );
            GeneralProvider.with(this).addToRequestQueue(getRequest);
        } catch (Exception e) {
            MySnackbar.inforSnackar(this, parentView, getString(R.string.error_message)).show();
        }


    }
}