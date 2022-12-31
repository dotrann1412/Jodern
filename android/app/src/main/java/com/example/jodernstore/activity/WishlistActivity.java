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
import com.example.jodernstore.model.Wishlist;
import com.example.jodernstore.provider.GeneralProvider;
import com.example.jodernstore.provider.WishlistProvider;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WishlistActivity extends AppCompatActivity {
    private FrameLayout parentView;
    private RecyclerView wishlistRecyclerView;
    private LinearLayout wishlistEmptyWrapper, wishlistLoadingWrapper;
    private NestedScrollView wishlistLayout;
    private ImageButton wishlistBackBtn, wishlistGoToHomeBtn;
    private MaterialButton wishlistGoToShopBtn;

    private Wishlist currentWishlist;
    private boolean shouldCallUpdateAPI = false;
    private boolean shouldCallFetchAPI = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);
//        WishlistProvider.getInstance();
        initViews();
        setEvents();
        getAndShowWishlistData();
    }

    private void initViews() {
        parentView = findViewById(R.id.wishlistParentView);
        wishlistLayout = findViewById(R.id.wishlistLayout);
        wishlistEmptyWrapper = findViewById(R.id.wishlistEmptyWrapper);
        wishlistLoadingWrapper = findViewById(R.id.wishlistLoadingWrapper);
        wishlistRecyclerView = findViewById(R.id.wishlistRecyclerView);
        wishlistBackBtn = findViewById(R.id.wishlistBackBtn);
        wishlistGoToHomeBtn = findViewById(R.id.wishlistGoToHomeBtn);
        wishlistGoToShopBtn = findViewById(R.id.wishlistGoToShopBtn);
    }

    private void setEvents() {
        wishlistBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        wishlistGoToHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WishlistActivity.this, MainActivity.class);
                startActivity(intent);
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
                GeneralProvider.with(WishlistActivity.this).setSearchIntent(searchIntent);

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

    @Override
    public void onResume() {
        super.onResume();
        if (shouldCallFetchAPI) {
            getAndShowWishlistData();
        }
    }

    @Override
    public void onStop() {
        shouldCallFetchAPI = true;

        // Call API to update wishlist data (if necessary)
        if (shouldCallUpdateAPI) {
            System.out.println("updating wishlist");
            updateWishlistData();
        }
        super.onStop();
    }

    private void getAndShowWishlistData() {
        wishlistLoadingWrapper.setVisibility(View.VISIBLE);

        // Call API
        String entry = "wishlist";
        String url = BuildConfig.SERVER_URL + entry + "/";
        String jwt = GeneralProvider.with(this).getJWT();
        System.out.println(jwt);
        JsonObjectRequest getRequest = new JsonObjectRequest(
                url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            handleResponse(response);
                        } catch (Exception e) {
                            showErrorMsg();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error.toString());
                        wishlistLoadingWrapper.setVisibility(View.GONE);
                        showErrorMsg();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Access-token", jwt);
                return params;
            }
        };

        GeneralProvider.with(this).addToRequestQueue(getRequest);
    }

    private void updateWishlistData() {
        try {
            String entry = "update-wishlist";
            JSONObject params = new JSONObject();
            // array of product id
            JSONArray wishlist = new JSONArray();
            ArrayList<Product> wishlistData = currentWishlist.getItems();
            for (Product product : wishlistData) {
                wishlist.put(product.getId());
            }
            params.put("wishlist", wishlist);
            String url = BuildConfig.SERVER_URL + entry + "/";
            String jwt = GeneralProvider.with(this).getJWT();
            JsonObjectRequest postRequest = new JsonObjectRequest(
                    url,
                    params,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                System.out.println("Update wishlist successfully");
                            } catch (Exception e) {
                                showErrorMsg();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println(error.toString());
                            showErrorMsg();
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("Access-token", jwt);
                    return params;
                }
            };
            GeneralProvider.with(this).addToRequestQueue(postRequest);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void showErrorMsg() {
        MySnackbar.inforSnackar(this, parentView, getString(R.string.error_message)).show();
    }

    private void handleResponse(JSONObject response) {
        shouldCallFetchAPI = false;
        wishlistLoadingWrapper.setVisibility(View.GONE);

        ArrayList<Product> wishlistItems = Product.parseProductListFromResponse(response);
//        WishlistProvider.getInstance().setItems(wishlistItems);
        currentWishlist = new Wishlist(wishlistItems);

        WishlistAdapter adapter = new WishlistAdapter(this, currentWishlist, new ChangeNumItemsListener() {
            @Override
            public void onChanged() {
                shouldCallUpdateAPI = true;
//                showWishlistLayout(WishlistProvider.getInstance().getItems().isEmpty());
                showWishlistLayout(currentWishlist.getItems().isEmpty());
            }
        });

        wishlistRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        wishlistRecyclerView.setLayoutManager(layoutManager);

        showWishlistLayout(wishlistItems.isEmpty());
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