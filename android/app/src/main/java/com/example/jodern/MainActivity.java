package com.example.jodern;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.jodern.activity.MapActivity;
import com.example.jodern.activity.SearchActivity;
import com.example.jodern.customwidget.MySnackbar;
import com.example.jodern.fragment.CartFragment;
import com.example.jodern.fragment.HomeFragment;
import com.example.jodern.fragment.ProductListFragment;
import com.example.jodern.fragment.WishlistFragment;
import com.example.jodern.provider.Provider;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;

public class MainActivity extends AppCompatActivity {
    private ImageButton homeBtn, mapBtn, cartBtn, wishlistBtn;
    private FloatingActionButton searchBtn;
    private HomeFragment homeFragment;
    private CartFragment cartFragment;
    private WishlistFragment wishlistFragment;
    private ConstraintLayout mainParentView;
    private BottomAppBar bottomAppBar;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        Provider.with(this.getApplicationContext());
        initViews();
        setEvents();
        setupInitialFragments();
    }

    @Override
    protected void onResume() {
        super.onResume();

        resetNavbarBtns();
        String currentFragment = Provider.with(this).getCurrentFragment();
        if (currentFragment.equals(HomeFragment.TAG))
            homeBtn.setImageResource(R.drawable.ic_home_filled);
        else if (currentFragment.equals(CartFragment.TAG))
            cartBtn.setImageResource(R.drawable.ic_cart_filled);
        else if (currentFragment.equals(WishlistFragment.TAG))
            wishlistBtn.setImageResource(R.drawable.ic_wishlist_filled);
    }

    private void initViews() {
        mainParentView = findViewById(R.id.mainParentView);

        homeBtn = findViewById(R.id.mainNavBarHomeBtn);
        mapBtn = findViewById(R.id.mainNavBarMapBtn);
        cartBtn = findViewById(R.id.mainNavBarCartBtn);
        wishlistBtn = findViewById(R.id.mainNavBarWishlistBtn);
        searchBtn = findViewById(R.id.mainNavBarSearchBtn);

        homeFragment = new HomeFragment(homeBtn);
        cartFragment = new CartFragment(cartBtn);
        wishlistFragment = new WishlistFragment(wishlistBtn);

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
                Provider.with(this).setSearchIntent(intent);
                // Receive data from search activity, forward it to product list fragment
                fragment = new ProductListFragment();
                bundle = retrieveBundleForProductListFragment(intent);
            }
            else if (nextFragment.equals(CartFragment.TAG)) {
                fragment = new CartFragment();
            }
            else if (nextFragment.equals(WishlistFragment.TAG)) {
                fragment = new WishlistFragment();
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
            if (intent.getBooleanExtra("hasJustLoggedIn", false)) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("hasJustLoggedIn", true);
                fragment.setArguments(bundle);
            }
        }
        else if (prevFragment.equals(CartFragment.TAG)) {
            cartBtn.setImageResource(R.drawable.ic_cart_filled);
            fragment = cartFragment;
        }
        else if (prevFragment.equals(WishlistFragment.TAG)) {
            wishlistBtn.setImageResource(R.drawable.ic_wishlist_filled);
            fragment = wishlistFragment;
        }
        else if (prevFragment.equals(ProductListFragment.TAG)) {
            // retrieve search params
            Intent searchIntent = Provider.with(this).getSearchIntent();
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
        wishlistBtn.setOnClickListener(onNavBarBtnClicked);
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
            intent.putExtra("previousFragment", Provider.with(MainActivity.this).getCurrentFragment());
            startActivity(intent);
        }
    };

    private final View.OnClickListener onNavBarBtnClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            resetNavbarBtns();

            int viewId = view.getId();

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
                        MySnackbar.inforSnackar(MainActivity.this, mainParentView, "Map is not available").show();
                    }
                    break;

                case R.id.mainNavBarCartBtn:
                    cartBtn.setImageResource(R.drawable.ic_cart_filled);
                    switchFragment(cartFragment, CartFragment.TAG);
                    break;

                case R.id.mainNavBarWishlistBtn:
                    wishlistBtn.setImageResource(R.drawable.ic_wishlist_filled);
                    switchFragment(wishlistFragment, WishlistFragment.TAG);
                    break;
            }
        }
    };

    private void resetNavbarBtns() {
        homeBtn.setImageResource(R.drawable.ic_home);
        mapBtn.setImageResource(R.drawable.ic_map);
        cartBtn.setImageResource(R.drawable.ic_cart);
        wishlistBtn.setImageResource(R.drawable.ic_wishlist);
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