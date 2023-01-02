package com.example.jodernstore.activity;

import static com.example.jodernstore.Utils.vndFormatPrice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jodernstore.MainActivity;
import com.example.jodernstore.R;
import com.example.jodernstore.adapter.CartAdapter;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.fragment.MyCartFragment;
import com.example.jodernstore.fragment.ProductListFragment;
import com.example.jodernstore.model.SharedCart;
import com.example.jodernstore.provider.GeneralProvider;
import com.example.jodernstore.provider.SharedCartProvider;
import com.google.android.material.button.MaterialButton;

public class JoinedCartActivity extends AppCompatActivity {

    private static final String TAG = "JoinedCartActivity";

    private static final long SHIPPING_FEE = 30000;
    private RecyclerView joinedCartInfoRecyclerView;
    private TextView cartInfoName, cartInfoQuantity, cartInfoNoMembers;
    private LinearLayout joinedCartSummaryWrapper;
    private MaterialButton joinedCartOrderBtn, joinedCartAppointBtn;
    private TextView joinedCartSubTotalText;
    private LinearLayout joinedCartEmptyWrapper;
    private MaterialButton joinedCartGoToShop;
    private ImageButton joinedCartBackBtn;
    private LinearLayout joinedCartLoadingWrapper;
    private LinearLayout joinedCartInfoParentView;
    private LinearLayout joinedCartLayout;

    private SharedCart joinedCart;
    private String subTotalStr, shippingStr, totalStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joined_cart);

        initViews();
        setEvents();

        getAndShowSharedCartInfo();
    }

    private void initViews() {
        joinedCartInfoRecyclerView = findViewById(R.id.joinedCartInfoRecyclerView);
        cartInfoName = findViewById(R.id.cartInfoName);
        cartInfoQuantity = findViewById(R.id.cartInfoQuantity);
        cartInfoNoMembers = findViewById(R.id.cartInfoNoMembers);
        joinedCartSummaryWrapper = findViewById(R.id.joinedCartSummaryWrapper);
        joinedCartBackBtn = findViewById(R.id.joinedCartBackBtn);
        joinedCartOrderBtn = findViewById(R.id.joinedCartOrderBtn);
        joinedCartAppointBtn = findViewById(R.id.joinedCartAppointBtn);
        joinedCartSubTotalText = findViewById(R.id.joinedCartSubTotalText);
        joinedCartEmptyWrapper = findViewById(R.id.joinedCartEmptyWrapper);
        joinedCartGoToShop = findViewById(R.id.joinedCartGoToShop);
        joinedCartLoadingWrapper = findViewById(R.id.joinedCartLoadingWrapper);
        joinedCartInfoParentView = findViewById(R.id.joinedCartInfoParentView);
        joinedCartLayout = findViewById(R.id.joinedCartLayout);
    }

    private void setEvents() {
        joinedCartBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        joinedCartOrderBtn.setOnClickListener(view -> {
            // TODO
        });

        joinedCartAppointBtn.setOnClickListener(view -> {
            // TODO
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
    }

    @SuppressLint("SetTextI18n")
    private void getAndShowSharedCartInfo() {
        joinedCartLoadingWrapper.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        int joinedCartIdx = intent.getIntExtra("index", -1);
        String cartName = intent.getStringExtra("cart-name");
        int nMembers = intent.getIntExtra("no-members", 0);
        int nItems = intent.getIntExtra("no-items", 0);
        long subTotal = intent.getLongExtra("sub-total", 0);

        if (joinedCartIdx == -1) {
            MySnackbar.inforSnackbar(this, joinedCartInfoParentView, getString(R.string.error_message)).show();
            joinedCartLoadingWrapper.setVisibility(View.GONE);
            return;
        }

        cartInfoName.setText(cartName);
        cartInfoQuantity.setText(Integer.toString(nItems));
        cartInfoNoMembers.setText(Integer.toString(nMembers));
        joinedCartSubTotalText.setText(vndFormatPrice(subTotal));

        joinedCart = SharedCartProvider.getInstance().getSharedCartItem(joinedCartIdx);

        CartAdapter adapter = new CartAdapter(this, joinedCart, this::updateTotalPrice);
        joinedCartInfoRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        joinedCartInfoRecyclerView.setLayoutManager(layoutManager);
        showCartLayout(joinedCart.getItems().isEmpty());

        joinedCartLoadingWrapper.setVisibility(View.GONE);
    }

    private void updateTotalPrice() {
        Long subTotal = joinedCart.getTotal();
        subTotalStr = vndFormatPrice(subTotal);
        totalStr = vndFormatPrice(subTotal + SHIPPING_FEE);
        shippingStr = vndFormatPrice(SHIPPING_FEE);

        joinedCartSubTotalText.setText(subTotalStr);

        if (subTotal.equals(0L)) {
            showCartLayout(true);
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