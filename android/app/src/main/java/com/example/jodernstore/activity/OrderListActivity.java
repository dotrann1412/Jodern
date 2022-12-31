package com.example.jodernstore.activity;

import static com.example.jodernstore.Utils.stringToDate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.jodernstore.BuildConfig;
import com.example.jodernstore.R;
import com.example.jodernstore.adapter.OrderListAdapter;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.model.Order;
import com.example.jodernstore.provider.GeneralProvider;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderListActivity extends AppCompatActivity {
    private RelativeLayout parentView;
    private LinearLayout loadingWrapper, emptyWrapper;
    private ImageButton backBtn, goToHomeBtn;
    private LinearLayout allBtn, deliveryBtn, appointBtn;
    private NestedScrollView scrollView;
    private RecyclerView recyclerView;


    private ArrayList<Order> allOrders;
    private ArrayList<Order> shownOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        initViews();
        setEvents();
        handleAPICall();
    }

    private void initViews() {
        parentView = findViewById(R.id.orderListParentView);
        loadingWrapper = findViewById(R.id.orderListLoadingWrapper);
        emptyWrapper = findViewById(R.id.orderListEmptyWrapper);
        backBtn = findViewById(R.id.orderListBackBtn);
        goToHomeBtn = findViewById(R.id.orderListGoToHomeBtn);
        allBtn = findViewById(R.id.orderListAllBtn);
        deliveryBtn = findViewById(R.id.orderListDeliveryBtn);
        appointBtn = findViewById(R.id.orderListAppointBtn);
        recyclerView = findViewById(R.id.orderListRecyclerView);
        scrollView = findViewById(R.id.orderListScrollView);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void highlightBtn(LinearLayout btn) {
        btn.findViewWithTag("image").setBackground(getDrawable(R.drawable.card_image_selected));
        ((TextView)btn.findViewWithTag("text")).setTextColor(getColor(R.color.primary));
        ((TextView)btn.findViewWithTag("text")).setTypeface(null, Typeface.BOLD);
    }
    
    @SuppressLint("UseCompatLoadingForDrawables")
    private void resetBtn(LinearLayout btn) {
        btn.findViewWithTag("image").setBackground(getDrawable(R.drawable.card_image_shape));
        ((TextView)btn.findViewWithTag("text")).setTextColor(getColor(R.color.text));
        ((TextView)btn.findViewWithTag("text")).setTypeface(null, Typeface.NORMAL);
    }

    private void resetBtns() {
        resetBtn(allBtn);
        resetBtn(deliveryBtn);
        resetBtn(appointBtn);
    }

    private void setEvents() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });

        goToHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        allBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetBtns();
                highlightBtn(allBtn);
                shownOrders = allOrders;
                showOrderList();
            }
        });

        deliveryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetBtns();
                highlightBtn(deliveryBtn);
                shownOrders = new ArrayList<>();
                for (Order order : allOrders) {
                    if (order.getType() == 0) {
                        shownOrders.add(order);
                    }
                }
                showOrderList();
            }
        });

        appointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetBtns();
                highlightBtn(appointBtn);
                shownOrders = new ArrayList<>();
                for (Order order : allOrders) {
                    if (order.getType() == 1) {
                        shownOrders.add(order);
                    }
                }
                showOrderList();
            }
        });
    }


    private void handleAPICall() {
        loadingWrapper.setVisibility(View.VISIBLE);
        String entry = "order-data";
        String url = BuildConfig.SERVER_URL + entry + "/";
        String jwt = GeneralProvider.with(this).getJWT();
        JsonObjectRequest getRequest = new JsonObjectRequest (
                url,
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
                        MySnackbar.inforSnackar(OrderListActivity.this, parentView, getString(R.string.error_message)).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Access-token", jwt);
                return params;
            }
        };
        GeneralProvider.with(this).addToRequestQueue(getRequest);
    }

    private void handleResponse(JSONObject response) {
        try {
            JSONArray orders = (JSONArray)response.get("orders");
            allOrders = new ArrayList<>();
            for (int i = 0; i < orders.length(); i++) {
                JSONObject json = orders.getJSONObject(i);
                Order order = Order.parseBasicJSON(json);
                allOrders.add(order);
            }
            shownOrders = allOrders;
            showOrderList();
            loadingWrapper.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showOrderList() {
        if (shownOrders.size() == 0) {
            scrollView.setVisibility(View.GONE);
            emptyWrapper.setVisibility(View.VISIBLE);
            return;
        }

        scrollView.setVisibility(View.VISIBLE);
        emptyWrapper.setVisibility(View.GONE);

        OrderListAdapter adapter = new OrderListAdapter(this);
        adapter.setOrderList(shownOrders);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

}