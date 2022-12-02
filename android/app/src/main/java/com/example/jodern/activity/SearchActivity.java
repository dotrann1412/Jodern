package com.example.jodern.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.example.jodern.ImagePicker;
import com.example.jodern.MainActivity;
import com.example.jodern.R;
import com.example.jodern.customwidget.MySnackbar;
import com.example.jodern.fragment.HomeFragment;
import com.example.jodern.fragment.ProductListFragment;
import com.example.jodern.provider.Provider;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class SearchActivity extends AppCompatActivity {
    // Show search history? https://stackoverflow.com/questions/21585326/implementing-searchview-in-action-bar
    // It can be done later... :D
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int MY_GALLERY_REQUEST_CODE = 101;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 102;

    private LinearLayout searchParentView;
    private String previousFragment;
    private ImageButton backBtn;
    private SearchView inputField;
    private LinearLayout cameraBtn;
    private MaterialButton submitBtn;
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_search);
        specifyPreviousFragment();
        initViews();
        setEvents();
    }

    private void specifyPreviousFragment() {
        Intent intent = getIntent();
        String previousFragment = intent.getStringExtra("previousFragment");
        if (previousFragment == null) {
            this.previousFragment = HomeFragment.TAG;
        } else
            this.previousFragment = previousFragment;
    }

    private void initViews() {
        searchParentView = findViewById(R.id.searchWrapperLayout);

        backBtn = findViewById(R.id.searchBackBtn);
        inputField = findViewById(R.id.searchInputField);
        cameraBtn = findViewById(R.id.searchCameraBtn);
        submitBtn = findViewById(R.id.searchSubmitBtn);

        inputField.requestFocus();
    }

    private void setEvents() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        inputField.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() == 0) {
                    cameraBtn.setVisibility(View.VISIBLE);
                } else {
                    cameraBtn.setVisibility(View.GONE);
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                submitTextQuery(query);
                return true;
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = inputField.getQuery().toString();
                submitTextQuery(query);
            }
        });

        setupCamera();
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runImagePicker();
            }
        });
    }

    private void setupCamera() {
        cameraActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            byte[] byteArray = ImagePicker.getImageFromResult(SearchActivity.this, data);
                            if (byteArray != null) {
                                submitImageQuery(byteArray);
                            } else {
                                MySnackbar.inforSnackar(SearchActivity.this, searchParentView, getString(R.string.error_message)).show();
                            }
                        }
                    }
                }
        );
    }

    private void submitTextQuery(String query) {
        if (query.length() == 0) {
            MySnackbar.inforSnackar(SearchActivity.this, searchParentView, "Bạn vui lòng nhập nội dung tìm kiếm nhé!").show();
            return;
        }

        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
        intent.putExtra("entry", "search");
        intent.putExtra("query", query);
        intent.putExtra("nextFragment", ProductListFragment.TAG);
        startActivity(intent);
    }

    private void submitImageQuery(byte[] byteArray) {
        Provider.with(this).setImageBase64(Base64.encodeToString(byteArray, Base64.DEFAULT));
        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
        intent.putExtra("entry", "search");
        intent.putExtra("method", "post");
        intent.putExtra("nextFragment", ProductListFragment.TAG);
        startActivity(intent);
    }

    private void runImagePicker() {
        // check camera and permission first
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
            return;
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_GALLERY_REQUEST_CODE);
            return;
        }
        Intent chooseImageIntent = ImagePicker.getPickImageIntent(this);
        cameraActivityResultLauncher.launch(chooseImageIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                MySnackbar.inforSnackar(this, searchParentView, "Không thể truy cập vào camera!").show();
            } else {
                runImagePicker();
            }
        } else if (requestCode == MY_GALLERY_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                MySnackbar.inforSnackar(this, searchParentView, "Không thể truy cập vào thư viện!").show();
            } else {
                runImagePicker();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
        intent.putExtra("previousFragment", previousFragment);
        startActivity(intent);
        finish();
    }
}