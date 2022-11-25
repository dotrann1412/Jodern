package android.jodern.app.activity;

import android.annotation.SuppressLint;
import android.jodern.app.adapter.CartAdapter;
import android.jodern.app.controller.CartController;
import android.jodern.app.interfaces.ChangeNumItemsListener;
import android.jodern.app.R;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ScrollView;
import android.widget.TextView;

public class CartActivity extends AppCompatActivity {
    private RecyclerView.Adapter adapter;
    private RecyclerView cartRecyclerView;
    private CartController cartController;

    TextView subTotalTextView, shippingTextView, totalTextView;

    private double total;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartController = new CartController(this);

        initView();
        initCartList();
//        renderCart();

    }

    private void initView() {
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        scrollView = findViewById(R.id.cartScrollView);
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
        double shippingCost = 15000;
        double subTotal = cartController.getSubTotal();
        double total = subTotal + shippingCost;

        subTotalTextView.setText(String.valueOf(subTotal) + " VND");
        shippingTextView.setText(String.valueOf(shippingCost) + " VND");
        totalTextView.setText(String.valueOf(total) + "VND");


    }
}