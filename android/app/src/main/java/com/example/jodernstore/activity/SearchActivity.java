package com.example.jodernstore.activity;

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
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.example.jodernstore.ImagePicker;
import com.example.jodernstore.MainActivity;
import com.example.jodernstore.R;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.fragment.HomeFragment;
import com.example.jodernstore.fragment.ProductListFragment;
import com.example.jodernstore.provider.GeneralProvider;
import com.google.android.material.button.MaterialButton;

public class SearchActivity extends AppCompatActivity {
    // Show search history? https://stackoverflow.com/questions/21585326/implementing-searchview-in-action-bar
    // It can be done later... :D
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int MY_GALLERY_REQUEST_CODE = 101;
    private static final int MY_WRITE_EXTERNAL_REQUEST_CODE = 102;
    private static final int MY_MICRO_REQUEST_CODE = 103;

    private LinearLayout searchParentView;
    private String previousFragment;
    private ImageButton backBtn;
    private SearchView inputField;
    private ImageView cameraBtn, microBtn;
    private MaterialButton submitBtn;
    private ActivityResultLauncher<Intent> cameraActivityResultLauncher;
    private ActivityResultLauncher<Intent> microActivityResultLauncher;

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
        microBtn = findViewById(R.id.searchMicroBtn);
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
                    microBtn.setVisibility(View.VISIBLE);
                } else {
                    cameraBtn.setVisibility(View.GONE);
                    microBtn.setVisibility(View.GONE);
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

        setupMicro();
        microBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runMicro();
            }
        });
    }

    private void setupMicro() {
        microActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String query = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
                        submitTextQuery(query);
                    }
                }
            }
        });
    }

    private void runMicro() {
        // check permission
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MY_MICRO_REQUEST_CODE);
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hãy nói gì đó...");
        microActivityResultLauncher.launch(intent);
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
                                MySnackbar.inforSnackbar(SearchActivity.this, searchParentView, getString(R.string.error_message)).show();
                            }
                        }
                    }
                }
        );
    }

    private void submitTextQuery(String query) {
        if (query.length() == 0) {
            MySnackbar.inforSnackbar(SearchActivity.this, searchParentView, "Bạn vui lòng nhập nội dung tìm kiếm nhé!").show();
            return;
        }

        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
        intent.putExtra("entry", "search");
        intent.putExtra("query", query);
        intent.putExtra("nextFragment", ProductListFragment.TAG);
        startActivity(intent);
    }

    private void submitImageQuery(byte[] byteArray) {
        GeneralProvider.with(this).setImageBase64(Base64.encodeToString(byteArray, Base64.DEFAULT));
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
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_WRITE_EXTERNAL_REQUEST_CODE);
            return;
        }
        Intent chooseImageIntent = ImagePicker.getPickImageIntent(this);
        cameraActivityResultLauncher.launch(chooseImageIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_CAMERA_REQUEST_CODE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    MySnackbar.inforSnackbar(this, searchParentView, "Không thể truy cập vào camera!").show();
                } else {
                    runImagePicker();
                }
                break;
            case MY_GALLERY_REQUEST_CODE:
            case MY_WRITE_EXTERNAL_REQUEST_CODE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    MySnackbar.inforSnackbar(this, searchParentView, "Không thể truy cập vào thư viện ảnh!").show();
                } else {
                    runImagePicker();
                }
                break;
            case MY_MICRO_REQUEST_CODE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    MySnackbar.inforSnackbar(this, searchParentView, "Không thể truy cập vào microphone!").show();
                } else {
                    runMicro();
                }
                break;
            default:
                break;
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