package com.example.jodernstore.activity;

import static com.example.jodernstore.Utils.vndFormatPrice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import android.widget.Toast;

import com.android.volley.toolbox.JsonObjectRequest;
import com.example.jodernstore.BuildConfig;
import com.example.jodernstore.R;
import com.example.jodernstore.adapter.CartAdapter;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.model.SharedCart;
import com.example.jodernstore.provider.GeneralProvider;
import com.example.jodernstore.provider.SharedCartProvider;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SharedCartActivity extends AppCompatActivity {

    private static final String TAG = "SharedCartActivity";
    private static final long SHIPPING_FEE = 30000;
    private RecyclerView sharedCartInfoRecyclerView;
    private TextView cartInfoName, cartInfoQuantity, cartInfoNoMembers;
    private LinearLayout sharedCartSummaryWrapper;
    private MaterialButton sharedCartOrderBtn, sharedCartAppointBtn;
    private TextView sharedCartSubTotalText;
    private LinearLayout sharedCartEmptyWrapper;
    private MaterialButton sharedCartGoToShop;
    private LinearLayout sharedCartLoadingWrapper;
    private LinearLayout sharedCartInfoParentView;
    private LinearLayout sharedCartLayout;
    private ImageButton shareCartBtn;

    private SharedCart sharedCart;
    private String subTotalStr, shippingStr, totalStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_cart);

        initViews();

        getAndShowSharedCartInfo();

        setEvents();
    }

    private void initViews() {
        sharedCartInfoRecyclerView = findViewById(R.id.sharedCartInfoRecyclerView);
        cartInfoName = findViewById(R.id.cartInfoName);
        cartInfoQuantity = findViewById(R.id.cartInfoQuantity);
        cartInfoNoMembers = findViewById(R.id.cartInfoNoMembers);
        sharedCartSummaryWrapper = findViewById(R.id.sharedCartSummaryWrapper);
        sharedCartOrderBtn = findViewById(R.id.sharedCartOrderBtn);
        sharedCartAppointBtn = findViewById(R.id.sharedCartAppointBtn);
        sharedCartSubTotalText = findViewById(R.id.sharedCartSubTotalText);
        sharedCartEmptyWrapper = findViewById(R.id.sharedCartEmptyWrapper);
        sharedCartGoToShop = findViewById(R.id.sharedCartGoToShop);
        sharedCartLoadingWrapper = findViewById(R.id.sharedCartLoadingWrapper);
        sharedCartInfoParentView = findViewById(R.id.sharedCartInfoParentView);
        sharedCartLayout = findViewById(R.id.sharedCartLayout);
        shareCartBtn = findViewById(R.id.shareCartBtn);
    }

    private void setEvents() {
        sharedCartOrderBtn.setOnClickListener(view -> {
            // TODO
        });

        sharedCartAppointBtn.setOnClickListener(view -> {
            // TODO
        });

        sharedCartGoToShop.setOnClickListener(view -> {
            // TODO
        });

        shareCartBtn.setOnClickListener(view -> {
            Log.d(TAG, "setEvents: 1");
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_share_code);

            Log.d(TAG, "setEvents: 2");

            // Init views
            TextView shareCodeText = dialog.findViewById(R.id.shareCodeText);
            ImageButton copyToClipboardBtn = dialog.findViewById(R.id.copyToClipboardBtn);

            if (shareCodeText == null) {
                Log.e(TAG, "setEvents: -1");
                return;
            }

            // Set data
            shareCodeText.setText(sharedCart.getShareCode());

            // Set events
            copyToClipboardBtn.setOnClickListener(view2 -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("share-code", sharedCart.getShareCode());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Mã đã được sao chép vào bộ nhớ tạm", Toast.LENGTH_SHORT).show();
            });

            Log.d(TAG, "setEvents: 3");

            dialog.show();
        });
    }

    @SuppressLint("SetTextI18n")
    private void getAndShowSharedCartInfo() {
        sharedCartLoadingWrapper.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        int sharedCartIdx = intent.getIntExtra("index", -1);
        String cartName = intent.getStringExtra("cart-name");
        int nMembers = intent.getIntExtra("no-members", 0);
        int nItems = intent.getIntExtra("no-items", 0);
        long subTotal = intent.getLongExtra("sub-total", 0);

        if (sharedCartIdx == -1) {
            MySnackbar.inforSnackbar(this, sharedCartInfoParentView, getString(R.string.error_message)).setAnchorView(R.id.mainNavBarSearchBtn).show();
            sharedCartLoadingWrapper.setVisibility(View.GONE);
            return;
        }

        cartInfoName.setText(cartName);
        cartInfoQuantity.setText(Integer.toString(nItems));
        cartInfoNoMembers.setText(Integer.toString(nMembers));
        sharedCartSubTotalText.setText(vndFormatPrice(subTotal));

        sharedCart = SharedCartProvider.getInstance().getSharedCartItem(sharedCartIdx);

        CartAdapter adapter = new CartAdapter(this, sharedCart, this::updateTotalPrice);
        sharedCartInfoRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        sharedCartInfoRecyclerView.setLayoutManager(layoutManager);
        showCartLayout(sharedCart.getItems().isEmpty());

        sharedCartLoadingWrapper.setVisibility(View.GONE);
    }

    private void updateTotalPrice() {
        Long subTotal = sharedCart.getTotal();
        subTotalStr = vndFormatPrice(subTotal);
        totalStr = vndFormatPrice(subTotal + SHIPPING_FEE);
        shippingStr = vndFormatPrice(SHIPPING_FEE);

        sharedCartSubTotalText.setText(subTotalStr);

        if (subTotal.equals(0L)) {
            showCartLayout(true);
        }
    }

    private void showCartLayout(boolean isEmpty) {
        if (isEmpty) {
            sharedCartEmptyWrapper.setVisibility(View.VISIBLE);
            sharedCartLayout.setVisibility(View.GONE);
        } else {
            sharedCartEmptyWrapper.setVisibility(View.GONE);
            sharedCartLayout.setVisibility(View.VISIBLE);
        }
    }

}