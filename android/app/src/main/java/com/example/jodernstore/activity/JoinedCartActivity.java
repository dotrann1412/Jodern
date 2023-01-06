package com.example.jodernstore.activity;

import static com.example.jodernstore.Utils.vndFormatPrice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.example.jodernstore.BuildConfig;
import com.example.jodernstore.MainActivity;
import com.example.jodernstore.R;
import com.example.jodernstore.adapter.SharedCartAdapter;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.fragment.ProductListFragment;
import com.example.jodernstore.interfaces.ChangeNumItemsListener;
import com.example.jodernstore.model.SharedCart;
import com.example.jodernstore.provider.GeneralProvider;
import com.google.android.material.button.MaterialButton;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoinedCartActivity extends AppCompatActivity {

    private static final String TAG = "JoinedCartActivity";

    private static final long SHIPPING_FEE = 30000;
    private RecyclerView joinedCartInfoRecyclerView;
    private LinearLayout joinedCartLayoutParentView;
    private TextView joinedCartName, joinedCartNumItems, joinedCartNumMembers;
    private TextView joinedCartSubTotalText;
    private LinearLayout joinedCartEmptyWrapper;
    private MaterialButton joinedCartGoToShop;
    private ImageButton joinedCartBackBtn;
    private LinearLayout joinedCartLoadingWrapper;
    private LinearLayout joinedCartInfoParentView;
    private LinearLayout joinedCartLayout;
    private RoundedImageView joinedCartHolderAvatar;
    private TextView joinedCartHolderName;

    private ImageButton joinedCartHistoryBtn;

    private SharedCart joinedCart;
    private String subTotalStr;

    private List<String> logs;

    private boolean shouldFetchData = false;    // prevent fetching data twice when activity is started

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joined_cart);

        initViews();
        setEvents();
        getAndShowJoinedCarts();
    }

    private void initViews() {
        joinedCartInfoRecyclerView = findViewById(R.id.joinedCartInfoRecyclerView);
        joinedCartLayoutParentView = findViewById(R.id.joinedCartLayoutParentView);
        joinedCartName = findViewById(R.id.joinedCartName);
        joinedCartNumItems = findViewById(R.id.joinedCartNumItems);
        joinedCartNumMembers = findViewById(R.id.joinedCartNumMembers);
        joinedCartBackBtn = findViewById(R.id.joinedCartBackBtn);
        joinedCartSubTotalText = findViewById(R.id.joinedCartSubTotalText);
        joinedCartEmptyWrapper = findViewById(R.id.joinedCartEmptyWrapper);
        joinedCartGoToShop = findViewById(R.id.joinedCartGoToShop);
        joinedCartLoadingWrapper = findViewById(R.id.joinedCartLoadingWrapper);
        joinedCartInfoParentView = findViewById(R.id.joinedCartInfoParentView);
        joinedCartLayout = findViewById(R.id.joinedCartLayout);
        joinedCartHolderAvatar = findViewById(R.id.joinedCartHolderAvatar);
        joinedCartHolderName = findViewById(R.id.joinedCartHolderName);
        joinedCartHistoryBtn = findViewById(R.id.joinedCartHistoryBtn);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!shouldFetchData) {
            shouldFetchData = true;
            return;
        }
        getAndShowJoinedCarts();
    }

    private void setEvents() {
        joinedCartBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        joinedCartGoToShop.setOnClickListener(view -> {
            // Back pressed handling
            Intent searchIntent = new Intent(JoinedCartActivity.this, ProductListFragment.class);
            searchIntent.putExtra("entry", "product-list");
            searchIntent.putExtra("sex", "nam");
            searchIntent.putExtra("categoryName", "Thời trang nam");
            GeneralProvider.with(JoinedCartActivity.this).setSearchIntent(searchIntent);

            // Go to product list fragment of main activity
            Intent intent = new Intent(JoinedCartActivity.this, MainActivity.class);
            intent.putExtra("nextFragment", ProductListFragment.TAG);
            intent.putExtra("entry", "product-list");
            intent.putExtra("sex", "nam");
            intent.putExtra("categoryName", "Thời trang nam");
            startActivity(intent);
        });

        joinedCartHistoryBtn.setOnClickListener(view -> {
            Log.d(TAG, "setEvents: joinedCartHistoryBtn");
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_cart_history);

            // Init views
            LinearLayout cartHistoryLayoutWrapper = dialog.findViewById(R.id.cartHistoryLayoutWrapper);

            // TODO history

            for (int i = 0; i < 15; ++i) {
                TextView tv = (TextView) getLayoutInflater().inflate(R.layout.history_item, null);
                tv.setText("Log " + i);
                cartHistoryLayoutWrapper.addView(tv);
            }

            dialog.show();
        });
    }

    @SuppressLint("SetTextI18n")
    private void getAndShowJoinedCarts() {
        joinedCartLayoutParentView.setVisibility(View.GONE);
        joinedCartLoadingWrapper.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        String cartId = intent.getStringExtra("cartId");

        try {
            String entry = "shared-cart";
            JSONObject params = new JSONObject();
            params.put("cartid", cartId);
            String url = BuildConfig.SERVER_URL + entry + "/";
            String jwt = GeneralProvider.with(JoinedCartActivity.this).getJWT();
            JsonObjectRequest postRequest = new JsonObjectRequest(
                    url,
                    params,
                    this::handleResponse,
                    error -> {
                        joinedCartLayoutParentView.setVisibility(View.VISIBLE);
                        joinedCartLoadingWrapper.setVisibility(View.GONE);
                        showCartLayout(joinedCart.getItems().isEmpty());
                        MySnackbar.inforSnackbar(JoinedCartActivity.this, joinedCartInfoParentView, getString(R.string.error_message)).show();
                    }
            ) {
                @NonNull
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    params.put("Access-token", jwt);
                    return params;
                }
            };
            GeneralProvider.with(this).addToRequestQueue(postRequest);
        } catch (Exception e) {
            MySnackbar.inforSnackbar(JoinedCartActivity.this, joinedCartInfoParentView, getString(R.string.error_message)).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void handleResponse(JSONObject response) {
        try {
            joinedCart = SharedCart.parseFullJson(response);

            // basic infor
            joinedCartName.setText(joinedCart.getName());
            joinedCartNumItems.setText(String.valueOf(joinedCart.getNumItems()));
            joinedCartNumMembers.setText(String.valueOf(joinedCart.getNumMembers()));

            // holder
            String avatar = joinedCart.getHolderAvatar();
            if (avatar != null && !avatar.equals("")) {
                Glide.with(this).load(avatar).into(joinedCartHolderAvatar);
            }
            joinedCartHolderName.setText(joinedCart.getHolderName());

            // items
            SharedCartAdapter adapter = new SharedCartAdapter(this, joinedCart, new ChangeNumItemsListener() {
                @Override
                public void onChanged() {
                    getAndShowJoinedCarts();
                }
            });
            joinedCartInfoRecyclerView.setAdapter(adapter);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            joinedCartInfoRecyclerView.setLayoutManager(layoutManager);
            showCartLayout(joinedCart.getItems().isEmpty());

            // summary
            joinedCartSubTotalText.setText(vndFormatPrice(joinedCart.getTotal()));

            joinedCartLayoutParentView.setVisibility(View.VISIBLE);
            joinedCartLoadingWrapper.setVisibility(View.GONE);
        } catch (Exception e) {
            joinedCartLayoutParentView.setVisibility(View.VISIBLE);
            joinedCartLoadingWrapper.setVisibility(View.GONE);
            MySnackbar.inforSnackbar(JoinedCartActivity.this, joinedCartInfoParentView, getString(R.string.error_message)).show();
        }
    }

    private void showCartLayout(boolean isEmpty) {
        if (isEmpty) {
            joinedCartEmptyWrapper.setVisibility(View.VISIBLE);
            joinedCartLayout.setVisibility(View.GONE);
        } else {
            joinedCartEmptyWrapper.setVisibility(View.GONE);
            joinedCartLayout.setVisibility(View.VISIBLE);
        }
    }

}