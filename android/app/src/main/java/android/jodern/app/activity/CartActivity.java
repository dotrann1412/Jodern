package android.jodern.app.activity;

import android.annotation.SuppressLint;
import android.jodern.app.adapter.CartAdapter;
import android.jodern.app.controller.CartController;
import android.jodern.app.interfaces.ChangeNumItemsListener;
import android.jodern.app.R;
import android.jodern.app.utils.StringUtils;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CartActivity extends AppCompatActivity {
    private RecyclerView.Adapter adapter;
    private RecyclerView cartRecyclerView;
    private CartController cartController;

    TextView subTotalTextView, shippingTextView, totalTextView;

    private long total;
    private LinearLayout cartScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartController = new CartController(this);

        initView();
        initCartList();
        renderCart();
    }

    private void initView() {
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        cartScrollView = findViewById(R.id.cartScrollView);
        subTotalTextView = findViewById(R.id.subTotalValueCartTextView);
        shippingTextView = findViewById(R.id.shippingValueCartTextView);
        totalTextView = findViewById(R.id.totalValueCartTextView);
    }

    private void initCartList() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        cartRecyclerView.setLayoutManager(linearLayoutManager);

        adapter = new CartAdapter(cartController.getCartList(), this, new ChangeNumItemsListener() {
            @Override
            public void onChanged() {
                renderCart();
            }
        });

        cartRecyclerView.setAdapter(adapter);
        if (cartController.getCartList() != null && cartController.getCartList().isEmpty()) {
            // TODO do something when cart is empty
        }
    }

    @SuppressLint("SetTextI18n")
    private void renderCart() {
        long subTotal = cartController.getSubTotal();
        long shippingCost = (subTotal == -1) ? 0 : 15000;
        total = subTotal + shippingCost;

        subTotalTextView.setText(StringUtils.long2money(subTotal));
        shippingTextView.setText(StringUtils.long2money(shippingCost));
        totalTextView.setText(StringUtils.long2money(total));


    }
}