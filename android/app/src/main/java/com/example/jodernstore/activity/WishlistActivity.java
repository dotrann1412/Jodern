package com.example.jodernstore.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.jodernstore.BuildConfig;
import com.example.jodernstore.MainActivity;
import com.example.jodernstore.R;
import com.example.jodernstore.adapter.WishlistAdapter;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.fragment.ProductListFragment;
import com.example.jodernstore.interfaces.ChangeNumItemsListener;
import com.example.jodernstore.model.Product;
import com.example.jodernstore.provider.Provider;
import com.example.jodernstore.wishlist.WishlistController;
import com.example.jodernstore.wishlist.wishlistitem.WishlistItem;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends AppCompatActivity {
    private FrameLayout parentView;
    private RecyclerView wishlistRecyclerView;
    private WishlistController wishlistController;
    private LinearLayout wishlistEmptyWrapper, wishlistLoadingWrapper;
    private NestedScrollView wishlistLayout;
    private ImageButton wishlistBackBtn;
    private MaterialButton wishlistGoToShopBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        wishlistController = WishlistController.with(this);

        initViews();
        setEvents();
        showWishlistItems();
    }

    private void initViews() {
        parentView = findViewById(R.id.wishlistParentView);
        wishlistLayout = findViewById(R.id.wishlistLayout);
        wishlistEmptyWrapper = findViewById(R.id.wishlistEmptyWrapper);
        wishlistLoadingWrapper = findViewById(R.id.wishlistLoadingWrapper);
        wishlistRecyclerView = findViewById(R.id.wishlistRecyclerView);
        wishlistBackBtn = findViewById(R.id.wishlistBackBtn);
        wishlistGoToShopBtn = findViewById(R.id.wishlistGoToShopBtn);
    }

    private void setEvents() {
        wishlistBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        wishlistGoToShopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Back pressed handling
                Intent searchIntent = new Intent(WishlistActivity.this, ProductListFragment.class);
                searchIntent.putExtra("entry", "product-list");
                searchIntent.putExtra("sex", "nam");
                searchIntent.putExtra("categoryName", "Thời trang nam");
                Provider.with(WishlistActivity.this).setSearchIntent(searchIntent);

                // Go to product list fragment of main activity
                Intent intent = new Intent(WishlistActivity.this, MainActivity.class);
                intent.putExtra("nextFragment", ProductListFragment.TAG);
                intent.putExtra("entry", "product-list");
                intent.putExtra("sex", "nam");
                intent.putExtra("categoryName", "Thời trang nam");
                startActivity(intent);
            }
        });
    }


    private void showWishlistItems() {
        if (wishlistController.getWishlistItemList().size() == 0) {
            wishlistEmptyWrapper.setVisibility(View.VISIBLE);
            wishlistLoadingWrapper.setVisibility(View.GONE);
            wishlistLayout.setVisibility(View.GONE);
            return;
        }

        ArrayList<Long> productIds = new ArrayList<>();
        for (WishlistItem wishlistItem : wishlistController.getWishlistItemList()) {
            productIds.add(wishlistItem.getProductId());
        }

        // call API to get products
        wishlistLoadingWrapper.setVisibility(View.VISIBLE);
        String entry = "product-list";
        String params = "id=";
        for (int i = 0; i < productIds.size(); i++) {
            params += String.valueOf(productIds.get(i));
            if (i != productIds.size() - 1)
                params += ",";
        }
        String url = BuildConfig.SERVER_URL + entry + "?" + params;
        JsonObjectRequest getRequest = new JsonObjectRequest (
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        handleResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        wishlistLoadingWrapper.setVisibility(View.GONE);
                        MySnackbar.inforSnackar(WishlistActivity.this, parentView, getString(R.string.error_message)).show();
                    }
                }
        );
        Provider.with(this).addToRequestQueue(getRequest);
    }

    private void handleResponse(JSONObject response) {
        wishlistLoadingWrapper.setVisibility(View.GONE);

        // Get the information of products in cart
        List<Product> wishlistProducts = Product.parseProductListFromResponse(response);
        wishlistController.setProductList(wishlistProducts);

        WishlistAdapter adapter = new WishlistAdapter(wishlistController, this, new ChangeNumItemsListener() {
            @Override
            public void onChanged() {
                showWishlistLayout(wishlistController.getProductList().isEmpty());
            }
        });

        wishlistRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        wishlistRecyclerView.setLayoutManager(layoutManager);

        showWishlistLayout(wishlistProducts.isEmpty());
    }

    private void showWishlistLayout(boolean isEmpty) {
        if (isEmpty) {
            wishlistEmptyWrapper.setVisibility(View.VISIBLE);
            wishlistLayout.setVisibility(View.GONE);
        } else {
            wishlistEmptyWrapper.setVisibility(View.GONE);
            wishlistLayout.setVisibility(View.VISIBLE);
        }
    }
}