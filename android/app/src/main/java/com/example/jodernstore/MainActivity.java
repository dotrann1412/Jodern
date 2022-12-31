package com.example.jodernstore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.jodernstore.activity.CartActivity;
import com.example.jodernstore.activity.MapActivity;
import com.example.jodernstore.activity.SearchActivity;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.fragment.MyCartFragment;
import com.example.jodernstore.fragment.HomeFragment;
import com.example.jodernstore.fragment.ProductListFragment;
import com.example.jodernstore.fragment.UserFragment;
import com.example.jodernstore.provider.GeneralProvider;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;

public class MainActivity extends AppCompatActivity {
    private ImageButton homeBtn, mapBtn, cartBtn, userBtn;
    private FloatingActionButton searchBtn;
    private HomeFragment homeFragment;
    private UserFragment userFragment;
    private ConstraintLayout mainParentView;
    private BottomAppBar bottomAppBar;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        GeneralProvider.with(this.getApplicationContext());
        System.out.println("JWT: " + GeneralProvider.with(this).getJWT());
        initViews();
        setEvents();
        setupInitialFragments();
    }

    @Override
    protected void onResume() {
        super.onResume();

        resetNavbarBtns();
        String currentFragment = GeneralProvider.with(this).getCurrentFragment();
        if (currentFragment.equals(HomeFragment.TAG)) {
            homeBtn.setImageResource(R.drawable.ic_home_filled);
        } else if (currentFragment.equals(UserFragment.TAG)) {
            userBtn.setImageResource(R.drawable.ic_user_filled);
        }
    }

    private void initViews() {
        mainParentView = findViewById(R.id.mainParentView);

        homeBtn = findViewById(R.id.mainNavBarHomeBtn);
        mapBtn = findViewById(R.id.mainNavBarMapBtn);
        cartBtn = findViewById(R.id.mainNavBarCartBtn);
        userBtn = findViewById(R.id.mainNavBarUserBtn);
        searchBtn = findViewById(R.id.mainNavBarSearchBtn);

        homeFragment = new HomeFragment(homeBtn);
        userFragment = new UserFragment(userBtn);

        bottomAppBar = findViewById(R.id.mainBottomNavBar);

        // bottomAppBar corner radius
        MaterialShapeDrawable bottomBarBackground = (MaterialShapeDrawable) bottomAppBar.getBackground();
        bottomBarBackground.setShapeAppearanceModel(
                bottomBarBackground.getShapeAppearanceModel()
                        .toBuilder()
                        .setTopRightCorner(CornerFamily.ROUNDED, 60)
                        .setTopLeftCorner(CornerFamily.ROUNDED, 60)
                        .build());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void setupInitialFragments() {
        Intent intent = getIntent();
        String prevFragment = intent.getStringExtra("previousFragment");
        String nextFragment = intent.getStringExtra("nextFragment");
        String message = intent.getStringExtra("message");

        // Forward to the next fragment
        if (nextFragment != null) {
            Fragment fragment = null;
            Bundle bundle = null;
            if (nextFragment.equals(ProductListFragment.TAG)) {
                GeneralProvider.with(this).setSearchIntent(intent);
                // Receive data from search activity, forward it to product list fragment
                fragment = new ProductListFragment();
                bundle = retrieveBundleForProductListFragment(intent);
            }
            else if (nextFragment.equals(MyCartFragment.TAG)) {
                fragment = new MyCartFragment();
            }
            else if (nextFragment.equals(UserFragment.TAG)) {
                fragment = new UserFragment();
                bundle = new Bundle();
                bundle.putString("nextFragment", nextFragment);
            }

            if (fragment == null)
                return;
            if (message != null) {
                if (bundle == null)
                    bundle = new Bundle();
                bundle.putString("message", message);
            }
            fragment.setArguments(bundle);
            switchFragmentWithoutPushingToBackStack(fragment);
            return;
        }

        // Back to previous fragment
        if (prevFragment == null) {
            prevFragment = HomeFragment.TAG;
        }
        Fragment fragment = null;
        if (prevFragment.equals(HomeFragment.TAG)) {
            homeBtn.setImageResource(R.drawable.ic_home_filled);
            fragment = homeFragment;
        }
        else if (prevFragment.equals(UserFragment.TAG)) {
            userBtn.setImageResource(R.drawable.ic_user_filled);
            fragment = userFragment;
            Bundle bundle = new Bundle();
            bundle.putString("previousFragment", prevFragment);
            fragment.setArguments(bundle);
        }
        else if (prevFragment.equals(ProductListFragment.TAG)) {
            // retrieve search params
            Intent searchIntent = GeneralProvider.with(this).getSearchIntent();
            fragment = new ProductListFragment();
            Bundle bundle = retrieveBundleForProductListFragment(searchIntent);
            fragment.setArguments(bundle);
        }
        switchFragmentWithoutPushingToBackStack(fragment);
    }

    private void setEvents() {
        homeBtn.setOnClickListener(onNavBarBtnClicked);
        mapBtn.setOnClickListener(onNavBarBtnClicked);
        cartBtn.setOnClickListener(onNavBarBtnClicked);
        userBtn.setOnClickListener(onNavBarBtnClicked);
        searchBtn.setOnClickListener(onSearchBtnClicked);
    }

    private Bundle retrieveBundleForProductListFragment(Intent searchIntent) {
        Bundle bundle = new Bundle();
        String entry = searchIntent.getStringExtra("entry");

        if (entry.equals("search")) {
            bundle.putString("entry", "search");
            bundle.putString("query", searchIntent.getStringExtra("query"));
            String method = searchIntent.getStringExtra("method");
            if (method != null) {
                bundle.putString("method", method);
            }
        }
        else if (entry.equals("product-list")) {
            bundle.putString("entry", "product-list");
            String categoryRaw = searchIntent.getStringExtra("categoryRaw");
            String categoryName = searchIntent.getStringExtra("categoryName");
            String sex = searchIntent.getStringExtra("sex");
            if (categoryRaw != null) {
                bundle.putString("categoryRaw", categoryRaw);
            }
            if (categoryName != null) {
                bundle.putString("categoryName", categoryName);
            }
            if (sex != null) {
                bundle.putString("sex", sex);
            }
        }

        return bundle;
    }

    private final View.OnClickListener onSearchBtnClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            intent.putExtra("previousFragment", GeneralProvider.with(MainActivity.this).getCurrentFragment());
            startActivity(intent);
        }
    };

    private final View.OnClickListener onNavBarBtnClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            resetNavbarBtns();
            switch (viewId) {
                case R.id.mainNavBarHomeBtn:
                    homeBtn.setImageResource(R.drawable.ic_home_filled);
                    homeFragment = new HomeFragment(homeBtn);
                    switchFragment(homeFragment, HomeFragment.TAG);
                    break;
                case R.id.mainNavBarMapBtn:
                    mapBtn.setImageResource(R.drawable.ic_map_filled);
                    try {
                        Intent intent = new Intent(MainActivity.this, MapActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        MySnackbar.inforSnackar(MainActivity.this, mainParentView, getString(R.string.error_message)).show();
                    }
                    break;

                case R.id.mainNavBarCartBtn:
                    cartBtn.setImageResource(R.drawable.ic_cart_filled);
                    try {
                        Intent intent = new Intent(MainActivity.this, CartActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        MySnackbar.inforSnackar(MainActivity.this, mainParentView, getString(R.string.error_message)).show();
                    }
                    break;

                case R.id.mainNavBarUserBtn:
                    userBtn.setImageResource(R.drawable.ic_user_filled);
                    switchFragment(userFragment, UserFragment.TAG);
                    break;

                default:
                    break;
            }
        }
    };

    private void resetNavbarBtns() {
        homeBtn.setImageResource(R.drawable.ic_home);
        mapBtn.setImageResource(R.drawable.ic_map);
        cartBtn.setImageResource(R.drawable.ic_cart);
        userBtn.setImageResource(R.drawable.ic_user);
    }

    private void switchFragment(Fragment fragmentObject, String name) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainFragmentContainer, fragmentObject)
                .addToBackStack(name)
                .commit();
    }

    private void switchFragmentWithoutPushingToBackStack(Fragment fragmentObject) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainFragmentContainer, fragmentObject)
                .commit();
    }
}