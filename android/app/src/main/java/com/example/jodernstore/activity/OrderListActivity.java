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

import com.example.jodernstore.R;
import com.example.jodernstore.adapter.OrderListAdapter;
import com.example.jodernstore.model.Order;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class OrderListActivity extends AppCompatActivity {
    private RelativeLayout parentView;
    private LinearLayout loadingWrapper, emptyWrapper;
    private ImageButton backBtn;
    private LinearLayout allBtn, deliveryBtn, appointBtn;
    private TextView currentType;
    private NestedScrollView scrollView;
    private RecyclerView recyclerView;

    private FirebaseAuth mAuth;

    private ArrayList<Order> allOrders;
    private ArrayList<Order> shownOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setEvents();
        handleAPICall();
    }

    private void initViews() {
        parentView = findViewById(R.id.orderListParentView);
        loadingWrapper = findViewById(R.id.orderListLoadingWrapper);
        emptyWrapper = findViewById(R.id.orderListEmptyWrapper);
        backBtn = findViewById(R.id.orderListBackBtn);
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
        backBtn.setOnClickListener(view -> {
            onBackPressed();
            finish();
        });

        allBtn.setOnClickListener(view -> {
            resetBtns();
            highlightBtn(allBtn);
            shownOrders = allOrders;
            showOrderList();
        });

        deliveryBtn.setOnClickListener(view -> {
            resetBtns();
            highlightBtn(deliveryBtn);
            shownOrders = new ArrayList<>();
            for (Order order : allOrders) {
                if (order.getType() == 0) {
                    shownOrders.add(order);
                }
            }
            showOrderList();
        });

        appointBtn.setOnClickListener(view -> {
            resetBtns();
            highlightBtn(appointBtn);
            shownOrders = new ArrayList<>();
            for (Order order : allOrders) {
                if (order.getType() == 1) {
                    shownOrders.add(order);
                }
            }
            showOrderList();
        });
    }


    private void handleAPICall() {
        // TODO: get all orders

        loadingWrapper.setVisibility(View.VISIBLE);

        new Handler().postDelayed((Runnable) () -> {
            // TODO: replace later
            // just demo
            allOrders = new ArrayList<>();
            allOrders.add(new Order(1L, 0, 2, 1000000L, stringToDate("10/12/2022"), true));
            allOrders.add(new Order(2L, 0, 1, 300000L, stringToDate("12/12/2022"), false));
            allOrders.add(new Order(3L, 1, 1, 500000L, stringToDate("16/12/2022"), true));
            allOrders.add(new Order(4L, 0, 3, 2000000L, stringToDate("20/12/2022"), false));
            allOrders.add(new Order(5L, 0, 2, 1000000L, stringToDate("21/12/2022"), false));
            allOrders.add(new Order(6L, 1, 1, 860000L, stringToDate("31/12/2022"), false));

            shownOrders = allOrders;
            showOrderList();

            loadingWrapper.setVisibility(View.GONE);
        }, 1000);

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