package com.example.jodern.activity;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.jodern.MainActivity;
import com.example.jodern.customwidget.MySnackbar;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import com.example.jodern.R;

public class AuthActivity extends AppCompatActivity {
    private static final String TAG = "AuthActivity";

    private FirebaseAuth mAuth;
    private CallbackManager mFbCallbackManager;

    // just for demo
    private TextView name, id;
    private ImageView avatar;
    private Button loginBtn, logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupFbAuth();
        // google goes here...
        setEvents();
    }

    private void initViews() {
        name = findViewById(R.id.authUserName);
        id = findViewById(R.id.authUserId);
        avatar = findViewById(R.id.authUserImage);
        loginBtn = findViewById(R.id.authLoginBtn);
        logoutBtn = findViewById(R.id.authLogoutBtn);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        handleLoginInfor(currentUser);
    }

    private void handleLoginInfor(FirebaseUser user) {
        if (user == null) {
            // TODO: process UI if user is null
            // just demo
            name.setVisibility(View.GONE);
            id.setVisibility(View.GONE);
            avatar.setVisibility(View.GONE);
            loginBtn.setVisibility(View.VISIBLE);
            logoutBtn.setVisibility(View.GONE);
            return;
        }

        // just demo
        name.setVisibility(View.VISIBLE);
        id.setVisibility(View.VISIBLE);
        avatar.setVisibility(View.VISIBLE);
        logoutBtn.setVisibility(View.VISIBLE);
        loginBtn.setVisibility(View.GONE);
        String url = user.getPhotoUrl() + "?access_token=" + AccessToken.getCurrentAccessToken().getToken() + "&type=large";
        name.setText(user.getDisplayName());
        id.setText(user.getUid());
        Glide.with(this).load(url).into(avatar);

        // TODO: process something (if necessary) before go to main activity
        new Handler().postDelayed((Runnable) () -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("hasJustLoggedIn", true);
            startActivity(intent);
            finish();
        }, 2000);
    }

    private void setupFbAuth() {
        mFbCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mFbCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            handleLoginInfor(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            MySnackbar.inforSnackar(AuthActivity.this, findViewById(R.id.authParentView), "Đăng nhập thất bại. Bạn vui lòng thử lại sau nhé.").show();
                            handleLoginInfor(null);
                        }
                    }
                });
    }

    private void setEvents() {
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(AuthActivity.this, Arrays.asList("public_profile", "email"));
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                AccessToken.setCurrentAccessToken(null);
                handleLoginInfor(null);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(mFbCallbackManager.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }

}