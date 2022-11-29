package android.jodern.app.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.jodern.app.cart.CartAdapter;
import android.jodern.app.cart.CartController;
import android.jodern.app.cart.cartitem.CartItem;
import android.jodern.app.interfaces.ChangeNumItemsListener;
import android.jodern.app.R;
import android.jodern.app.utils.StringUtils;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CartActivity extends AppCompatActivity {
    private static final String TAG = CartActivity.class.getName();
    private RecyclerView cartRecyclerView;
    private CartController cartController;

    TextView subTotalTextView, shippingTextView, totalTextView;

    private LinearLayout cartLayout, emptyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartController = CartController.with(this);

        initView();
        initCartList();
        renderCartBottomSection();
    }

    private void initView() {
        Log.d(TAG, "initView: initializing cart activity view");
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        cartLayout = findViewById(R.id.cartLayout);
        subTotalTextView = findViewById(R.id.subTotalValueCartTextView);
        shippingTextView = findViewById(R.id.shippingValueCartTextView);
        totalTextView = findViewById(R.id.totalValueCartTextView);
        emptyLayout = findViewById(R.id.cartEmptyLayout);
    }

    private void initCartList() {
        Log.d(TAG, "initCartList: initializing cart list recycler view");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        cartRecyclerView.setLayoutManager(linearLayoutManager);

        // handle add to cart intent
        Intent intent = getIntent();
        CartItem item = new CartItem();
        try {
            item.setProductId(intent.getLongExtra("productID", -1));
            if (item.getProductId() != -1) {
                item.setSize(intent.getStringExtra("size"));
                item.setQuantity(intent.getIntExtra("quantity", 1));
                cartController.addToCart(item);
            } else {
                throw new Exception("is not add to cart intent");
            }
        } catch (Exception e) {
            Log.d(TAG, "initCartList: " + e.getMessage());
        }

        RecyclerView.Adapter<CartAdapter.ViewHolder> adapter = new CartAdapter(cartController.getCartList(), this, new ChangeNumItemsListener() {
            @Override
            public void onChanged() {
                renderCartBottomSection();
            }
        });

        cartRecyclerView.setAdapter(adapter);
        if (cartController.getCartList() == null || cartController.getCartList().isEmpty()) {
            emptyLayout.setVisibility(View.VISIBLE);
            cartLayout.setVisibility(View.GONE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            cartLayout.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void renderCartBottomSection() {
        Log.d(TAG, "renderCartBottomSection: ");
//        long subTotal = cartController.getSubTotal();
        long subTotal = -1L;
        long shippingCost = (subTotal == -1) ? 0 : 15000;
        long total = subTotal + shippingCost;

        subTotalTextView.setText(StringUtils.long2money(subTotal));
        shippingTextView.setText(StringUtils.long2money(shippingCost));
        totalTextView.setText(StringUtils.long2money(total));
    }
}