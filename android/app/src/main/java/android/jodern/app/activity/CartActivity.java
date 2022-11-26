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

import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class CartActivity extends AppCompatActivity {
    private RecyclerView.Adapter adapter;
    private RecyclerView cartRecyclerView;
    private CartController cartController;

    private static String API_URL = null;

    TextView subTotalTextView, shippingTextView, totalTextView;

    private long total;
    private LinearLayout cartLayout;

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
        cartLayout = findViewById(R.id.cartLayout);
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


        API_URL = "http://joderm.store:8000/api/test";
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, API_URL, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
//                            for (int i = 0; i < response.length(); ++i) {
//                                JSONObject responseObj = response.getJSONObject(i);
//                                Log.d("VOLLEY", responseObj.toString());
//                            }
                            Log.d("VOLLEY", response.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("VOLLEY", "Error " + error.getMessage());
                    }
                });
//        queue.add(jsonArrayRequest);
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