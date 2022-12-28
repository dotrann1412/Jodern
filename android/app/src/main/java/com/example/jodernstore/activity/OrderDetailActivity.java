package com.example.jodernstore.activity;

import static com.example.jodernstore.Utils.localDateToString;
import static com.example.jodernstore.Utils.stringToDate;
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

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.jodernstore.BuildConfig;
import com.example.jodernstore.R;
import com.example.jodernstore.adapter.OrderDetailAdapter;
import com.example.jodernstore.cart.cartitem.CartItem;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.model.AppointmentOrder;
import com.example.jodernstore.model.BranchInfo;
import com.example.jodernstore.model.Order;
import com.example.jodernstore.model.Product;
import com.example.jodernstore.provider.Provider;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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

    // Appointment information
    private MaterialButton mapBtn;
    private TextView orderDetailAppointBranch, orderDetailAppointDate;

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
        orderDetailAddressParent = findViewById(R.id.orderDetailAddressParent);

        productRecycler = findViewById(R.id.orderDetailRecyclerView);

        loadingWrapper = findViewById(R.id.orderDetailLoadingWrapper);

        summarySubTotal = findViewById(R.id.orderDetailSummarySubTotal);
        summaryShipping = findViewById(R.id.orderDetailSummaryShipping);
        summaryTotal = findViewById(R.id.orderDetailSummaryTotal);

        confirmBtn = findViewById(R.id.orderDetailConfirmBtn);

        orderDetailAppointBranch = findViewById(R.id.orderDetailAppointBranch);
        orderDetailAppointDate = findViewById(R.id.orderDetailAppointDate);
        mapBtn = findViewById(R.id.orderDetailAppointMapBtn);
    }

    private void retrieveOrderInfor() {
        Intent intent = getIntent();
        String id = intent.getStringExtra("orderID");

        // TODO: Call API to get order detail information, then get products and show them in recycler view
        // ...

        // the below codes are used for demo purpose
//        Order order = new Order(Long.valueOf(id), 0, 5, 1000000L, stringToDate("10/12/2022"), true);
//        order.setBranchInfo(new BranchInfo(10.762986, 106.682835, "Jodern Demo"));
        Order order = new AppointmentOrder(Long.valueOf(id), 5, 1000000L, stringToDate("25/12/2022"), true, stringToDate("31/12/2022"), null, null, new BranchInfo(10.762986, 106.682835, "Jodern Demo"));
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
        Log.d(TAG, "retrieveOrderInfor: productIds " + productIds);
        String entry = "product-list";
        StringBuilder params = new StringBuilder("id=");
        for (int i = 0; i < productIds.size(); i++) {
            params.append(productIds.get(i));
            if (i != productIds.size() - 1)
                params.append(",");
        }
        String url = BuildConfig.SERVER_URL + entry + "?" + params;
        Log.d(TAG, "retrieveOrderInfor: API URL " + url);
        JsonObjectRequest getRequest = new JsonObjectRequest (
                Request.Method.GET,
                url,
                null,
                response -> {
                    handleResponse(response, productIds);
                },
                error -> {
                    loadingWrapper.setVisibility(View.GONE);
                    MySnackbar.inforSnackar(OrderDetailActivity.this, parentView, getString(R.string.error_message)).show();
                }
        );
        Provider.with(this).addToRequestQueue(getRequest);
    }

    private void handleResponse(JSONObject response, @NonNull List<Long> productIds) {
        List<Product> receivedProducts = Product.parseProductListFromResponse(response);
        ArrayList<Product> displayedProducts = new ArrayList<>();

        for (Long id : productIds) {
            for (Product product : receivedProducts) {
                if (id.equals(product.getId())) {
                    displayedProducts.add(product);
                }
            }
        }

        this.products = displayedProducts;

        Log.d(TAG, "handleResponse: done with parsing product list with length of " + products.size());
        loadingWrapper.setVisibility(View.GONE);
        setInfo();
    }

    @SuppressLint("SetTextI18n")
    private void setInfo() {
        Log.d(TAG, "setInfo: running");
        orderID.setText(currentOrder.getId().toString());
        orderStatus.setText(!currentOrder.getStatus() ? "Chưa nhận hàng" : "Đã nhận hàng");
        orderStatus.setTextColor(!currentOrder.getStatus() ? getResources().getColor(R.color.light_red) : getResources().getColor(R.color.light_green));
        orderDate.setText(localDateToString(currentOrder.getCheckoutDate()));
        orderType.setText(currentOrder.getType() == 0 ? "Đặt giao hàng" : "Hẹn thử đồ");
        Log.d(TAG, "setInfo: " + currentOrder.getId() + " with type " + currentOrder.getType());
        orderCount.setText(String.valueOf(currentOrder.getItems().size()));
        orderTotal.setText(vndFormatPrice(currentOrder.getTotalPrice()));

        customerName.setText(currentOrder.getCustomerInfor().get("customerName"));
        customerEmail.setText(currentOrder.getCustomerInfor().get("customerEmail"));
        customerPhone.setText(currentOrder.getCustomerInfor().get("customerPhone"));

        if (currentOrder.getType() == 0) {
            Log.d(TAG, "setInfo: setting info for a shipping order " + currentOrder.getId());
            appointmentParent.setVisibility(View.GONE);
            orderDetailAddressParent.setVisibility(View.VISIBLE);
            customerAddress.setText(currentOrder.getCustomerInfor().get("customerAddress"));

        } else {
            Log.d(TAG, "setInfo: setting info for an appointment order " + currentOrder.getId());
            appointmentParent.setVisibility(View.VISIBLE);
            orderDetailAddressParent.setVisibility(View.GONE);

            // init order detail information
            orderDetailAppointBranch.setText(currentOrder.getBranchInfo().getBranchName());
            orderDetailAppointDate.setText("hehehehehe");
        }

        Log.d(TAG, "setInfo: setting adapter");
        OrderDetailAdapter adapter = new OrderDetailAdapter(this);
        adapter.setProducts(products);
        adapter.setCartItems(currentOrder.getItems());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        productRecycler.setLayoutManager(layoutManager);
        productRecycler.setAdapter(adapter);

        Log.d(TAG, "setInfo: calculating current order total cost");
        // total price of products
        Long subTotal = 0L;
        for (CartItem cartItem : currentOrder.getItems()) {
            for (Product product : products) {
                if (Objects.equals(product.getId(), cartItem.getProductId())) {
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