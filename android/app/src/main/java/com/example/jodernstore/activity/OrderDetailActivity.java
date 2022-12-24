package com.example.jodernstore.activity;

import static com.example.jodernstore.Utils.localDateToString;
import static com.example.jodernstore.Utils.stringToDate;
import static com.example.jodernstore.Utils.vndFormatPrice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.jodernstore.BuildConfig;
import com.example.jodernstore.R;
import com.example.jodernstore.adapter.OrderDetailAdapter;
import com.example.jodernstore.cart.cartitem.CartItem;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.model.Order;
import com.example.jodernstore.model.Product;
import com.example.jodernstore.provider.Provider;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class OrderDetailActivity extends AppCompatActivity {
    private RelativeLayout parentView;
    private ImageButton backBtn;
    private TextView orderID, orderStatus, orderDate, orderType, orderCount, orderTotal;
    private TextView customerName, customerEmail, customerPhone, customerAddress;
    private LinearLayout appointmentParent;
    private LinearLayout loadingWrapper;
    private RecyclerView productRecycler;
    private TextView summarySubTotal, summaryShipping, summaryTotal;
    private MaterialButton confirmBtn;

    private Order currentOrder;
    private ArrayList<Product> products;

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

        appointmentParent = findViewById(R.id.orderDetailAppointParentView);

        productRecycler = findViewById(R.id.orderDetailRecyclerView);

        loadingWrapper = findViewById(R.id.orderDetailLoadingWrapper);

        summarySubTotal = findViewById(R.id.orderDetailSummarySubTotal);
        summaryShipping = findViewById(R.id.orderDetailSummaryShipping);
        summaryTotal = findViewById(R.id.orderDetailSummaryTotal);

        confirmBtn = findViewById(R.id.orderDetailConfirmBtn);
    }

    private void retrieveOrderInfor() {
        Intent intent = getIntent();
        String id = intent.getStringExtra("orderID");

        // TODO: Call API to get order detail information, then get products and show them in recycler view
        // ...

        // the below codes are used for demo purpose
        Order order = new Order(1L, 0, 2, 1000000L, stringToDate("10/12/2022"), true);
        currentOrder = order;
        HashMap<String, String> customerInfor = new HashMap<>();
        customerInfor.put("customerName", "Hoàng Trọng Vũ");
        customerInfor.put("customerEmail", "trongvulqd@gmail.com");
        customerInfor.put("customerPhone", "0947124559");
        customerInfor.put("customerAddress", "39 Cao Lỗ, P4, Q8, TPHCM");
        order.setCustomerInfor(customerInfor);
        ArrayList<CartItem> items = new ArrayList<>();
        items.add(new CartItem(1L, 1, "XL"));
        items.add(new CartItem(2L, 2, "L"));
        items.add(new CartItem(2L, 2, "XL"));
        items.add(new CartItem(2L, 1, "M"));
        items.add(new CartItem(2L, 1, "S"));
        order.setItems(items);

        // call API to get products
        ArrayList<Long> productIds = new ArrayList<>();
        for (CartItem cartItem : currentOrder.getItems()) {
            productIds.add(cartItem.getProductId());
        }
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
                        loadingWrapper.setVisibility(View.GONE);
                        MySnackbar.inforSnackar(OrderDetailActivity.this, parentView, getString(R.string.error_message)).show();
                    }
                }
        );
        Provider.with(this).addToRequestQueue(getRequest);
    }

    private void handleResponse(JSONObject response) {
        this.products = Product.parseProductListFromResponse(response);
        loadingWrapper.setVisibility(View.GONE);
        setInfo();
    }

    private void setInfo() {
        orderID.setText(currentOrder.getId().toString());
        orderStatus.setText(!currentOrder.getStatus() ? "Chưa nhận hàng" : "Đã nhận hàng");
        orderStatus.setTextColor(!currentOrder.getStatus() ? getResources().getColor(R.color.light_red) : getResources().getColor(R.color.light_green));
        orderDate.setText(localDateToString(currentOrder.getCheckoutDate()));
        orderType.setText(currentOrder.getType() == 0 ? "Đặt giao hàng" : "Hẹn thử đồ");
        orderCount.setText(String.valueOf(currentOrder.getItems().size()));
        orderTotal.setText(vndFormatPrice(currentOrder.getTotalPrice()));

        customerName.setText(currentOrder.getCustomerInfor().get("customerName"));
        customerEmail.setText(currentOrder.getCustomerInfor().get("customerEmail"));
        customerPhone.setText(currentOrder.getCustomerInfor().get("customerPhone"));

        if (currentOrder.getType() == 0) {
            appointmentParent.setVisibility(View.GONE);
            customerAddress.setVisibility(View.VISIBLE);
            customerAddress.setText(currentOrder.getCustomerInfor().get("customerAddress"));
        } else {
            appointmentParent.setVisibility(View.VISIBLE);
            customerAddress.setVisibility(View.GONE);

            // TODO: set information for appointment order
        }

        OrderDetailAdapter adapter = new OrderDetailAdapter(this);
        adapter.setProducts(products);
        adapter.setCartItems(currentOrder.getItems());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        productRecycler.setLayoutManager(layoutManager);
        productRecycler.setAdapter(adapter);

        // total price of products
        Long subTotal = 0L;
        for (CartItem cartItem : currentOrder.getItems()) {
            for (Product product : products) {
                if (product.getId() == cartItem.getProductId()) {
                    subTotal += product.getPrice() * cartItem.getQuantity();
                    break;
                }
            }
        }
        summarySubTotal.setText(vndFormatPrice(subTotal));
        summaryShipping.setText(vndFormatPrice(30000L));
        summaryTotal.setText(vndFormatPrice(subTotal + 30000L));

        confirmBtn.setVisibility(currentOrder.getStatus() ? View.GONE : View.VISIBLE);
    }

    private void setEvents() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Call API to confirm order
            }
        });
    }
}