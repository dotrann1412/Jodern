package com.example.jodern.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.jodern.ImagePicker;
import com.example.jodern.MainActivity;
import com.example.jodern.R;
import com.example.jodern.fragment.ProductListFragment;
import com.example.jodern.model.Category;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import com.example.jodern.customwidget.MyToast;
import com.example.jodern.MainActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;

public class SearchActivity extends AppCompatActivity {
    // Show search history? https://stackoverflow.com/questions/21585326/implementing-searchview-in-action-bar
    // It can be done later... :D
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int MY_GALLERY_REQUEST_CODE = 101;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 102;

    private String previousFragment;
    private ImageButton backBtn;
    private SearchView inputField;
    private LinearLayout cameraBtn;
    private MaterialButton submitBtn;
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        specifyPreviousFragment();
        initViews();
        setEvents();
    }

    private void specifyPreviousFragment() {
        Intent intent = getIntent();
        String previousFragment = intent.getStringExtra("previousFragment");
        if (previousFragment == null) {
            this.previousFragment = "home";
        } else
            this.previousFragment = previousFragment;
    }

    private void initViews() {
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
//                startActivity(new Intent(SearchActivity.this, MainActivity.class));
//                finish();
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
                submitTextQuery(inputField.getQuery().toString());
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
                            Bitmap bitmap = ImagePicker.getImageFromResult(SearchActivity.this, data);
                            if (bitmap != null) {
                                submitImageQuery(bitmap);
                            } else {
                                MyToast.makeText(SearchActivity.this, getString(R.string.error_message), Toast.LENGTH_SHORT);
                            }
                        }
                    }
                }
        );
    }

    private void submitTextQuery(String query) {
        if (query.length() == 0) {
            MyToast.makeText(SearchActivity.this, "Bạn vui lòng nhập nội dung tìm kiếm nhé!", Toast.LENGTH_SHORT);
            return;
        }

        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
        intent.putExtra("entry", "search");
        intent.putExtra("query", query);
        intent.putExtra("nextFragment", "productList");
        startActivity(intent);

//        Fragment fragment = new ProductListFragment();
//        Bundle bundle = new Bundle();
//        bundle.putString("entry", "product-list");
//        bundle.putString("query", query);
//        fragment.setArguments(bundle);

//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.mainFragmentContainer, fragment);
//        fragmentTransaction.addToBackStack("productList");
//        fragmentTransaction.commit();
    }

    private void submitImageQuery(Bitmap bitmap) {
        // Bitmap to Base64 string
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
        intent.putExtra("entry", "search");
        intent.putExtra("query", encoded);
        intent.putExtra("method", "post");
        intent.putExtra("nextFragment", "productList");
        startActivity(intent);
//        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
//        intent.putExtra("entry", "search");
//        intent.putExtra("query", query);
//        startActivity(intent);

//        Fragment fragment = new ProductListFragment();
//        Bundle bundle = new Bundle();
//        bundle.putString("entry", "product-list");
//        bundle.putString("query", encoded);
//        bundle.putString("method", "post");
//        fragment.setArguments(bundle);
//
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.mainFragmentContainer, fragment);
//        fragmentTransaction.addToBackStack("productList");
//        fragmentTransaction.commit();
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
                MyToast.makeText(this, "Không thể truy cập vào camera!", Toast.LENGTH_SHORT);
            } else {
                runImagePicker();
            }
        } else if (requestCode == MY_GALLERY_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                MyToast.makeText(this, "Không thể truy cập vào thư viện!", Toast.LENGTH_SHORT);
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