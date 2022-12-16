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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import com.example.jodern.R;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthActivity extends AppCompatActivity {
    private static final String TAG = "AuthActivity";
    private static final int GOOGLE_AUTH_REQUEST = 584;

    private FirebaseAuth mAuth;
    private CallbackManager mFbCallbackManager;
    private GoogleSignInClient mGoogleSignInClient;

    // just for demo
    private TextView name, id;
    private ImageView avatar;
    private Button gLoginBtn, fbLoginBtn, logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupFbAuth();
        setupGoogleAuth();
        setEvents();
    }

    private void initViews() {
        name = findViewById(R.id.authUserName);
        id = findViewById(R.id.authUserId);
        avatar = findViewById(R.id.authUserImage);
        fbLoginBtn = findViewById(R.id.fbAuthLoginBtn);
        gLoginBtn = findViewById(R.id.gAuthLoginBtn);
        logoutBtn = findViewById(R.id.authLogoutBtn);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        handleLoginInfo(currentUser);
    }

    private void handleLoginInfo(FirebaseUser user) {
        if (user == null) {
            // TODO: process UI if user is null
            // just demo
            name.setVisibility(View.GONE);
            id.setVisibility(View.GONE);
            avatar.setVisibility(View.GONE);
            fbLoginBtn.setVisibility(View.VISIBLE);
            gLoginBtn.setVisibility(View.VISIBLE);
            logoutBtn.setVisibility(View.GONE);
            return;
        }

        // just demo
        name.setVisibility(View.VISIBLE);
        id.setVisibility(View.VISIBLE);
        avatar.setVisibility(View.VISIBLE);
        logoutBtn.setVisibility(View.VISIBLE);
        fbLoginBtn.setVisibility(View.GONE);
        gLoginBtn.setVisibility(View.GONE);
        String url = "";
        if (AccessToken.getCurrentAccessToken() != null) {
            url = user.getPhotoUrl() + "?access_token=" + AccessToken.getCurrentAccessToken().getToken() + "&type=large";
        } else
            url = user.getPhotoUrl() + "?type=large";
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

    private void setupGoogleAuth() {
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, options);
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

    private void handleGoogleIdToken(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            handleLoginInfo(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            handleLoginInfo(null);
                        }
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
                            handleLoginInfo(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            MySnackbar.inforSnackar(AuthActivity.this, findViewById(R.id.authParentView), "Đăng nhập thất bại. Bạn vui lòng thử lại sau nhé.").show();
                            handleLoginInfo(null);
                        }
                    }
                });
    }

    private void logInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_AUTH_REQUEST);
    }

    private void setEvents() {
        fbLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(AuthActivity.this, Arrays.asList("public_profile", "email"));
            }
        });

        gLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logInWithGoogle();
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                AccessToken.setCurrentAccessToken(null);
                handleLoginInfo(null);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_AUTH_REQUEST) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "handleGoogleIdToken:" + account.getId());
                handleGoogleIdToken(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }

        if(mFbCallbackManager.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }

}