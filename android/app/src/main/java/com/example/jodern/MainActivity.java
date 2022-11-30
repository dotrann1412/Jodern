package com.example.jodern;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.jodern.activity.SearchActivity;
import com.example.jodern.fragment.CartFragment;
import com.example.jodern.fragment.HomeFragment;
import com.example.jodern.fragment.MapFragment;
import com.example.jodern.fragment.ProductListFragment;
import com.example.jodern.fragment.WishlistFragment;
import com.example.jodern.provider.Provider;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    private ImageButton homeBtn, mapBtn, cartBtn, wishlistBtn;
    private MaterialButton searchBtn;
    private HomeFragment homeFragment;
    private MapFragment mapFragment;
    private CartFragment cartFragment;
    private WishlistFragment wishlistFragment;
    private String currentFragment = "home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Provider.with(this.getApplicationContext());
        initViews();
        setEvents();
        setupInitialFragments();
    }

    private void initViews() {
        homeBtn = findViewById(R.id.mainNavBarHomeBtn);
        mapBtn = findViewById(R.id.mainNavBarMapBtn);
        cartBtn = findViewById(R.id.mainNavBarCartBtn);
        wishlistBtn = findViewById(R.id.mainNavBarWishlistBtn);
        searchBtn = findViewById(R.id.mainNavBarSearchBtn);

        homeFragment = new HomeFragment(homeBtn);
        mapFragment = new MapFragment(mapBtn);
        cartFragment = new CartFragment(cartBtn);
        wishlistFragment = new WishlistFragment(wishlistBtn);
    }

    private void setupInitialFragments() {
        Intent intent = getIntent();
        String prevFragment = intent.getStringExtra("previousFragment");
        String nextFragment = intent.getStringExtra("nextFragment");

        if (nextFragment != null) {
            if (nextFragment.equals("productList")) {
                // Receive data from search activity, forward it to product list fragment
                Fragment fragment = new ProductListFragment();
                Bundle bundle = new Bundle();
                bundle.putString("entry", "search");
                bundle.putString("query", intent.getStringExtra("query"));
                String method = intent.getStringExtra("method");
                if (method != null) {
                    bundle.putString("method", method);
                }
                fragment.setArguments(bundle);
                switchFragment(fragment, nextFragment);
                return;
            }

            // other, if any
            return;
        }

        if (prevFragment == null) {
            homeBtn.setImageResource(R.drawable.ic_home_filled);
            switchFragment(homeFragment, "home");
            return;
        }
        currentFragment = prevFragment;
        if (prevFragment.equals("home")) {
            homeBtn.setImageResource(R.drawable.ic_home_filled);
            switchFragment(homeFragment, "home");
        } else if (prevFragment.equals("map")) {
            mapBtn.setImageResource(R.drawable.ic_map_filled);
            switchFragment(mapFragment, "map");
        } else if (prevFragment.equals("cart")) {
            cartBtn.setImageResource(R.drawable.ic_cart_filled);
            switchFragment(cartFragment, "cart");
        } else if (prevFragment.equals("wishlist")) {
            wishlistBtn.setImageResource(R.drawable.ic_wishlist_filled);
            switchFragment(wishlistFragment, "wishlist");
        }
    }

    private void setEvents() {
        homeBtn.setOnClickListener(onNavBarBtnClicked);
        mapBtn.setOnClickListener(onNavBarBtnClicked);
        cartBtn.setOnClickListener(onNavBarBtnClicked);
        wishlistBtn.setOnClickListener(onNavBarBtnClicked);
        searchBtn.setOnClickListener(onSearchBtnClicked);
    }

    private final View.OnClickListener onSearchBtnClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            intent.putExtra("previousFragment", currentFragment);
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
                    switchFragment(homeFragment, "home");
                    break;
                case R.id.mainNavBarMapBtn:
                    mapBtn.setImageResource(R.drawable.ic_map_filled);
                    switchFragment(mapFragment, "map");
                    break;
                case R.id.mainNavBarCartBtn:
                    cartBtn.setImageResource(R.drawable.ic_cart_filled);
                    switchFragment(cartFragment, "cart");
                    break;
                case R.id.mainNavBarWishlistBtn:
                    wishlistBtn.setImageResource(R.drawable.ic_wishlist_filled);
                    switchFragment(wishlistFragment, "wishlist");
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
        currentFragment = name;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainFragmentContainer, fragmentObject)
                .addToBackStack(name)
                .commit();
    }
}