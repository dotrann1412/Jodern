package com.example.jodernstore.activity;

import static com.example.jodernstore.Utils.vndFormatPrice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.example.jodernstore.BuildConfig;
import com.example.jodernstore.R;
import com.example.jodernstore.adapter.OrderDetailAdapter;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.model.BranchInfo;
import com.example.jodernstore.model.CartItem;
import com.example.jodernstore.model.Order;
import com.example.jodernstore.provider.GeneralProvider;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OrderDetailActivity extends AppCompatActivity {
    private static final String TAG = OrderDetailActivity.class.getName();
    private RelativeLayout parentView;
    private ImageButton backBtn;
    private TextView orderID, orderStatus, orderDate, orderType, orderCount, orderTotal;
    private TextView customerName, customerEmail, customerPhone, customerAddress;
    private LinearLayout appointmentParent, orderDetailAddressParent;
    private LinearLayout loadingWrapper;
    private RecyclerView productRecycler;
    private TextView summarySubTotal, summaryShipping, summaryTotal;
    private MaterialButton confirmBtn;
    private LinearLayout customerAddressParent;
    private LinearLayout summarySubTotalParent;
    private LinearLayout summaryShippingParent;

    // Appointment information
    private MaterialButton mapBtn;
    private TextView orderDetailAppointBranch, orderDetailAppointDate;

    private Order currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        
        initViews();
        loadingWrapper.setVisibility(View.VISIBLE);
        retrieveOrderInfor();
        setEvents();
    }

    private void initViews() {
        parentView = findViewById(R.id.orderDetailParentView);

        backBtn = findViewById(R.id.orderDetailBackBtn);

        orderID = findViewById(R.id.orderDetailID);
        orderStatus = findViewById(R.id.orderDetailStatus);
        orderDate = findViewById(R.id.orderDetailDate);
        orderType = findViewById(R.id.orderDetailType);
        orderCount = findViewById(R.id.orderDetailCount);
        orderTotal = findViewById(R.id.orderDetailTotalPrice);

        customerName = findViewById(R.id.orderDetailCustomerName);
        customerEmail = findViewById(R.id.orderDetailCustomerEmail);
        customerPhone = findViewById(R.id.orderDetailCustomerPhone);
        customerAddress = findViewById(R.id.orderDetailCustomerAddress);
        customerAddressParent = findViewById(R.id.orderDetailAddressParent);

        appointmentParent = findViewById(R.id.orderDetailAppointParentView);
        orderDetailAddressParent = findViewById(R.id.orderDetailAddressParent);

        productRecycler = findViewById(R.id.orderDetailRecyclerView);

        loadingWrapper = findViewById(R.id.orderDetailLoadingWrapper);

        summarySubTotal = findViewById(R.id.orderDetailSummarySubTotal);
        summaryShipping = findViewById(R.id.orderDetailSummaryShipping);
        summaryTotal = findViewById(R.id.orderDetailSummaryTotal);
        summarySubTotalParent = findViewById(R.id.orderDetailSummarySubTotalParent);
        summaryShippingParent = findViewById(R.id.orderDetailSummaryShippingParent);

        confirmBtn = findViewById(R.id.orderDetailConfirmBtn);

        orderDetailAppointBranch = findViewById(R.id.orderDetailAppointBranch);
        orderDetailAppointDate = findViewById(R.id.orderDetailAppointDate);
        mapBtn = findViewById(R.id.orderDetailAppointMapBtn);
    }

    private void retrieveOrderInfor() {
        Log.d(TAG, "retrieveOrderInfor: start");
        Intent intent = getIntent();
        String id = intent.getStringExtra("orderID");

        try {
            loadingWrapper.setVisibility(View.VISIBLE);
            String entry = "order-data";
            String url = BuildConfig.SERVER_URL + entry + "/";
            JSONObject params = new JSONObject();
            params.put("orderid", id);
            String jwt = GeneralProvider.with(this).getJWT();
            JsonObjectRequest postRequest = new JsonObjectRequest(
                    url,
                    params,
                    this::handleResponse,
                    error -> {
                        System.out.println(error.toString());
                        loadingWrapper.setVisibility(View.GONE);
                        MySnackbar.inforSnackbar(OrderDetailActivity.this, parentView, getString(R.string.error_message)).show();
                    }
            ) {
                @NonNull
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String>  params = new HashMap<>();
                    params.put("Access-token", jwt);
                    return params;
                }
            };
            GeneralProvider.with(this).addToRequestQueue(postRequest);
        } catch (Exception e) {
            e.printStackTrace();
            MySnackbar.inforSnackbar(this, parentView, getString(R.string.error_message)).show();
        }
    }

    private void handleResponse(JSONObject response) {
        Log.d(TAG, "handleResponse: " + response.toString());
        currentOrder = Order.parseFullJSON(response, getIntent().getStringExtra("orderID"));
        loadingWrapper.setVisibility(View.GONE);
        setInfo();
    }

    @SuppressLint("SetTextI18n")
    private void setInfo() {
        Log.d(TAG, "setInfo: running");
        Log.d(TAG, "setInfo: currentOrderItems: " + currentOrder.getItems());
        orderID.setText(currentOrder.getId());
        orderStatus.setText(!currentOrder.getStatus() ? "Chưa nhận hàng" : "Đã nhận hàng");
        orderStatus.setTextColor(!currentOrder.getStatus() ? getResources().getColor(R.color.light_red) : getResources().getColor(R.color.light_green));
        orderDate.setText(currentOrder.getCheckoutDate());
        orderType.setText(currentOrder.getType() == 0 ? "Đặt giao hàng" : "Hẹn thử đồ");
        orderCount.setText(String.valueOf(currentOrder.getItems().size())); // TODO: change later
        Log.d(TAG, "setInfo: " + currentOrder.getId() + " with type " + currentOrder.getType());
        orderCount.setText(String.valueOf(currentOrder.getItems().size()));
        orderTotal.setText(vndFormatPrice(currentOrder.getTotalPrice()));

        customerName.setText(currentOrder.getCustomerInfo().get("name"));
        customerEmail.setText(currentOrder.getCustomerInfo().get("email"));
        customerPhone.setText(currentOrder.getCustomerInfo().get("phone"));

        if (currentOrder.getType() == 0) {
            Log.d(TAG, "setInfo: setting info for a shipping order " + currentOrder.getId());
            appointmentParent.setVisibility(View.GONE);
            customerAddressParent.setVisibility(View.VISIBLE);
            customerAddress.setText(currentOrder.getCustomerInfo().get("address"));
        } else {
            Log.d(TAG, "setInfo: setting info for an appointment order " + currentOrder.getId());
            appointmentParent.setVisibility(View.VISIBLE);
            customerAddressParent.setVisibility(View.GONE);

            BranchInfo branchInfo = currentOrder.getBranchInfo();

            if (branchInfo == null) {
                Log.e(TAG, "setInfo: branchInfo is null");
                return;
            }
            Log.d(TAG, "setInfo: branchInfo: " + branchInfo);
            Log.d(TAG, "setInfo: branchInfo: " + branchInfo.getBranchId());
            // TODO: set information for appointment order
            // init order detail information
            orderDetailAppointBranch.setText(branchInfo.getBranchName());
            orderDetailAppointDate.setText("hehehehehe");
        }

        Log.d(TAG, "setInfo: setting adapter");
        OrderDetailAdapter adapter = new OrderDetailAdapter(this);
        adapter.setCartItems(currentOrder.getItems());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        productRecycler.setLayoutManager(layoutManager);
        productRecycler.setAdapter(adapter);

        Log.d(TAG, "setInfo: calculating current order total cost");
        // total price of products
        Long subTotal = 0L;
        for (CartItem cartItem : currentOrder.getItems()) {
            subTotal += cartItem.getQuantity() * cartItem.getProduct().getPrice();
        }

        if (currentOrder.getType() == 0) {
            summarySubTotalParent.setVisibility(View.VISIBLE);
            summaryShippingParent.setVisibility(View.VISIBLE);
            summarySubTotal.setText(vndFormatPrice(subTotal));
            summaryShipping.setText(vndFormatPrice(30000L));
            summaryTotal.setText(vndFormatPrice(subTotal + 30000L));
            confirmBtn.setVisibility(currentOrder.getStatus() ? View.GONE : View.VISIBLE);
        } else {
            summarySubTotalParent.setVisibility(View.GONE);
            summaryShippingParent.setVisibility(View.GONE);
            confirmBtn.setVisibility(View.GONE);
            summaryTotal.setText(vndFormatPrice(subTotal));
        }

    }

    private void setEvents() {
        backBtn.setOnClickListener(view -> {
            onBackPressed();
            finish();
        });

        confirmBtn.setOnClickListener(view -> {
            // TODO: Call API to confirm order
        });

        mapBtn.setOnClickListener(view -> {
            // TODO: send current location of the branch to map activity to draw the pathway
            Intent intent = new Intent(OrderDetailActivity.this, MapActivity.class);
            intent.putExtra("lat", currentOrder.getBranchInfo().getLatitude());
            intent.putExtra("lng", currentOrder.getBranchInfo().getLongitude());
            intent.putExtra("order", true);
            startActivity(intent);
        });
    }
}