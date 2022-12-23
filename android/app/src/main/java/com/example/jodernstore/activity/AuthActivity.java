package com.example.jodernstore.activity;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.jodernstore.MainActivity;
import com.example.jodernstore.adapter.ProductPreviewSliderAdapter;
import com.example.jodernstore.customwidget.MySnackbar;
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

import java.util.ArrayList;
import java.util.Arrays;

import com.example.jodernstore.R;
import com.google.firebase.auth.GoogleAuthProvider;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

public class AuthActivity extends AppCompatActivity {
    private static final String TAG = "AuthActivity";
    private static final int GOOGLE_AUTH_REQUEST = 584;

    private FirebaseAuth mAuth;
    private CallbackManager mFbCallbackManager;
    private GoogleSignInClient mGoogleSignInClient;

    private LinearLayout authParentView;
    private SliderView previewSlider;
    private Button gLoginBtn, fbLoginBtn;


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

        ArrayList<String> images = new ArrayList<>(Arrays.asList(
                "https://bizweb.sapocdn.net/100/438/408/products/cvn5072-nab-2-4.jpg?v=1666411625000",
                "https://bizweb.sapocdn.net/100/438/408/products/vay-dam-nu-van6146-vag-1-yodyvn.jpg?v=1670042146000",
                "https://bizweb.sapocdn.net/100/438/408/products/quan-jeans-nu-baggy-cap-chun-mem-mai-qjn5096-den-yodyvn-2.jpg?v=1670655253000",
                "https://bizweb.sapocdn.net/100/438/408/products/apm5393-xhd-3.jpg?v=1662358179000",
                "https://bizweb.sapocdn.net/100/438/408/products/scm3030-xgi-qam3128-den-2.jpg?v=1642234995000",
                "https://bizweb.sapocdn.net/100/438/408/products/qjm5005-xdm-15.jpg?v=1666834758000"
                ));
        ProductPreviewSliderAdapter adapter = new ProductPreviewSliderAdapter(this);
        adapter.setItems(images);
        previewSlider.setSliderAdapter(adapter, false);
        previewSlider.setIndicatorAnimation(IndicatorAnimationType.WORM);
        previewSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        previewSlider.startAutoCycle();
    }

    private void initViews() {
        authParentView = findViewById(R.id.authParentView);
        fbLoginBtn = findViewById(R.id.fbAuthLoginBtn);
        gLoginBtn = findViewById(R.id.gAuthLoginBtn);
        previewSlider = findViewById(R.id.productPreviewImageSlider);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        handleLoginSucessfully(currentUser);
    }

    private void handleLoginSucessfully(FirebaseUser user) {
        if (user == null) {
            MySnackbar.inforSnackar(this, authParentView, "Đăng nhập thất bại. Bạn vui lòng thử lại sau nhé!").show();
            return;
        }

        // call API to get access token
//        try {
//            String entry = "login";
//            JSONObject params = new JSONObject();
//            params.put("userid", user.getUid());
//            params.put("email", user.getEmail());
//            params.put("fullname", user.getDisplayName());
//
//            String url = BuildConfig.SERVER_URL + entry + "/";
//            JsonObjectRequest postRequest = new JsonObjectRequest(
//                    url,
//                    params,
//                    new Response.Listener<JSONObject>() {
//                        @Override
//                        public void onResponse(JSONObject response) {
//                            Log.d(TAG, "onResponse: " + response.toString());
////                            Intent intent = new Intent(AuthActivity.this, MainActivity.class);
////                            startActivity(intent);
////                            finish();
//                        }
//                    },
//                    new Response.ErrorListener() {
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//                            System.out.println(error.toString());
//
//                        }
//                    }
//            );
//            Provider.with(this).addToRequestQueue(postRequest);
//
//
//        } catch (Exception e) {
//            System.out.println(e.toString());
//        }
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
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
        System.out.println(idToken);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            handleLoginSucessfully(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            handleLoginSucessfully(null);
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
                            handleLoginSucessfully(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            MySnackbar.inforSnackar(AuthActivity.this, findViewById(R.id.authParentView), "Đăng nhập thất bại. Bạn vui lòng thử lại sau nhé.").show();
                            handleLoginSucessfully(null);
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

//        logoutBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mAuth.signOut();
//                LoginManager.getInstance().logOut();
//                AccessToken.setCurrentAccessToken(null);
//                handleLoginSucessfully(null);
//            }
//        });
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