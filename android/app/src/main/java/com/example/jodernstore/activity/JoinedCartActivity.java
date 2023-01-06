package com.example.jodernstore.activity;

import static com.example.jodernstore.Utils.vndFormatPrice;

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

import com.example.jodernstore.MainActivity;
import com.example.jodernstore.R;
import com.example.jodernstore.adapter.CartAdapter;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.fragment.MyCartFragment;
import com.example.jodernstore.fragment.ProductListFragment;
import com.example.jodernstore.model.CartItem;
import com.example.jodernstore.model.Product;
import com.example.jodernstore.model.SharedCart;
import com.example.jodernstore.provider.GeneralProvider;
import com.example.jodernstore.provider.SharedCartProvider;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class JoinedCartActivity extends AppCompatActivity {

    private static final String TAG = "JoinedCartActivity";

    private static final long SHIPPING_FEE = 30000;
    private RecyclerView joinedCartInfoRecyclerView;
    private TextView cartInfoName, cartInfoQuantity, cartInfoNoMembers;
    private TextView joinedCartSubTotalText;
    private LinearLayout joinedCartEmptyWrapper;
    private MaterialButton joinedCartGoToShop;
    private ImageButton joinedCartBackBtn;
    private LinearLayout joinedCartLoadingWrapper;
    private LinearLayout joinedCartInfoParentView;
    private LinearLayout joinedCartLayout;

    private ImageButton joinedCartHistoryBtn;

    private SharedCart joinedCart;
    private String subTotalStr;

    private List<String> logs;

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
        joinedCartBackBtn = findViewById(R.id.joinedCartBackBtn);
        joinedCartSubTotalText = findViewById(R.id.joinedCartSubTotalText);
        joinedCartEmptyWrapper = findViewById(R.id.joinedCartEmptyWrapper);
        joinedCartGoToShop = findViewById(R.id.joinedCartGoToShop);
        joinedCartLoadingWrapper = findViewById(R.id.joinedCartLoadingWrapper);
        joinedCartInfoParentView = findViewById(R.id.joinedCartInfoParentView);
        joinedCartLayout = findViewById(R.id.joinedCartLayout);
        joinedCartHistoryBtn = findViewById(R.id.joinedCartHistoryBtn);
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
    private void getAndShowSharedCartInfo() {
        joinedCartLoadingWrapper.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        String cartId = intent.getStringExtra("cartId");

        // TODO: call API
        // ...

        List<Long> pseudoId = new ArrayList<>();
        pseudoId.add(123L);
        pseudoId.add(125L);

        Log.d(TAG, "handleResponse: check point 2");

        ArrayList<String> urls = new ArrayList<>();
        urls.add("https://bizweb.sapocdn.net/100/438/408/products/vnk5274-hog-5.jpg?v=1663816469000");
        Product pseudoProd = new Product(
                141L,
                "Đầm Bé Gái In Thỏ Cột Nơ",
                urls,
                174300L,
                "",
                "vay-nu",
                "vay-nu",
                new Integer[]{1, 1, 2, 2, 1}
        );
        Log.d(TAG, "handleResponse: check point 3");

        List<CartItem> items = new ArrayList<>();
        items.add(new CartItem(pseudoProd, 1, "XL"));
        items.add(new CartItem(pseudoProd, 2, "L"));
        items.add(new CartItem(pseudoProd, 2, "XL"));
        items.add(new CartItem(pseudoProd, 1, "M"));
        items.add(new CartItem(pseudoProd, 1, "S"));

        ArrayList<String> history = new ArrayList<>();
        history.add("Member 1 thêm sản phẩm 1");
        history.add("Member 2 thêm sản phẩm 1");
        history.add("Member 3 thêm sản phẩm 1");

        joinedCart = new SharedCart("asd123", "Joined Cart 1", 100000L, items.size(), 5, "Hoàng Trọng Vũ", "https://i.pinimg.com/736x/89/90/48/899048ab0cc455154006fdb9676964b3.jpg", items, history);

        // TODO: set holder name and avatar

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