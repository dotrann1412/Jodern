package com.example.jodernstore.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.jodernstore.BuildConfig;
import com.example.jodernstore.R;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.fragment.MyCartFragment;
import com.example.jodernstore.provider.CartProvider;
import com.example.jodernstore.provider.GeneralProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class OrderFormActivity extends AppCompatActivity {
    private RelativeLayout orderParentView;
    private LinearLayout orderLoadingWrapper;
    private TextInputEditText orderName, orderEmail, orderPhone, orderAddress;
    private MaterialButton orderCheckout;
    private ImageButton orderBackBtn;

    private int orderType;

    private static final int FORM_VALIDATED = 0;
    private static final int BLANK_INPUT = 1;
    private static final int EMAIL_INVALID = 2;
    private static final int PHONE_INVALID = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_form);

        Intent intent = getIntent();
        orderType = intent.getIntExtra("orderType", 0);

        initViews();
        setEvents();
    }

    private void initViews() {
        orderParentView = findViewById(R.id.orderParentView);
        orderLoadingWrapper = findViewById(R.id.orderLoadingWrapper);
        orderName = findViewById(R.id.orderName);
        orderEmail = findViewById(R.id.orderEmail);
        orderPhone = findViewById(R.id.orderPhone);
        orderAddress = findViewById(R.id.orderAddress);
        orderCheckout = findViewById(R.id.orderCheckout);
        orderBackBtn = findViewById(R.id.orderBackBtn);

        if (orderType == 1) {
            TextInputLayout orderAddressLayout = findViewById(R.id.orderAddressLayout);
            orderAddressLayout.setVisibility(View.GONE);
        }
    }
    private void setEvents() {
        orderCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Hide the keyboard
                View focusView = OrderFormActivity.this.getCurrentFocus();
                if (focusView != null) {
                    InputMethodManager inputManager = (InputMethodManager) OrderFormActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }

                Intent intent = OrderFormActivity.this.getIntent();

                // ID of cart to be ordered
                //      If id is null: self cart
                //      Otherwise: a shared cart
                String cartId = null;
                if (intent.getStringExtra("cartId") != null)
                    cartId = intent.getStringExtra("cartId");

                // Type of order
                //      0: delivery
                //      1: appointment
                // If type is 1, we also have branchId and date info
                int branchId = -1;
                String date = null;
                if (orderType == 1) {
                    branchId = intent.getIntExtra("branchId", -1);
                    date = intent.getStringExtra("date");
                }

                // user info
                String customer_name = String.valueOf(orderName.getText());
                String email = String.valueOf(orderEmail.getText());
                String phone = String.valueOf(orderPhone.getText());
                String location = null;
                if (orderType == 0)
                    location = String.valueOf(orderAddress.getText());

                try {
                    // validate form info
                    int formValidation = formValidator(customer_name, email, phone, location);
                    switch (formValidation) {
                        case BLANK_INPUT: {
                            MySnackbar.inforSnackbar(OrderFormActivity.this, orderParentView, "Bạn vui lòng cung cấp đầy đủ thông tin nhé").show();
                            return;
                        }
                        case EMAIL_INVALID: {
                            MySnackbar.inforSnackbar(OrderFormActivity.this, orderParentView, "Địa chỉ email không hợp lệ. Bạn vui lòng thử lại nhé").show();
                            return;
                        }
                        case PHONE_INVALID: {
                            MySnackbar.inforSnackbar(OrderFormActivity.this, orderParentView, "Số điện thoại không hợp lệ. Bạn vui lòng thử lại nhé").show();
                            return;
                        }
                        case FORM_VALIDATED: {
                            break;
                        }
                    }
                    phone = "+84" + phone.substring(1);

                    // params object
                    JSONObject params = new JSONObject();
                    params.put("type", orderType);
                    params.put("customer_name", customer_name);
                    params.put("phone_number", phone);
                    params.put("email", email);
                    if (location != null)
                        params.put("location", location);
                    if (orderType == 1) {
                        params.put("branchid", branchId);
                        params.put("date", date);
                    }
                    if (cartId != null)
                        params.put("cartid", cartId);

                    JSONObject finalParams = new JSONObject();
                    finalParams.put("order-info", params);

                    orderLoadingWrapper.setVisibility(View.VISIBLE);
                    String entry = "process-order";
                    String url = BuildConfig.SERVER_URL + entry + "/";
                    String jwt = GeneralProvider.with(OrderFormActivity.this).getJWT();
                    JsonObjectRequest postRequest = new JsonObjectRequest(
                            url,
                            finalParams,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    orderLoadingWrapper.setVisibility(View.GONE);
                                    handleSuccess(response);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    System.out.println(error.toString());
                                    orderLoadingWrapper.setVisibility(View.GONE);
                                    handleError(error);
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
                    // increase timeout
//                    postRequest.setRetryPolicy(new DefaultRetryPolicy(
//                            3000,
//                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    GeneralProvider.with(OrderFormActivity.this).addToRequestQueue(postRequest);
                } catch (JSONException e) {
                    System.out.println(e.getStackTrace());
                }
            }
        });

        orderBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });
    }

    private void handleError(VolleyError error) {
        MySnackbar.inforSnackbar(OrderFormActivity.this, orderParentView, getString(R.string.error_message)).show();
    }

    private void handleSuccess(JSONObject response) {
        try {
            if (response.getString("message").equals("Done!")) {
                CartProvider.getInstance().getMyCart().clear();

                // Move to cart activity (with empty cart) and show success message
                Intent intent = new Intent(this, CartActivity.class);
                intent.putExtra("nextFragment", MyCartFragment.TAG);
                intent.putExtra("message", "Đặt hàng thành công. Bạn vui lòng kiểm tra email nhé!");
                startActivity(intent);
            } else {
                MySnackbar.inforSnackbar(OrderFormActivity.this, orderParentView, getString(R.string.error_message)).show();
            }
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
    }

    private int formValidator(String name, String email, String phone, String address) {
        if (name.trim().length() == 0) return BLANK_INPUT;
        if (email.trim().length() == 0) return BLANK_INPUT;
        if (phone.trim().length() == 0) return BLANK_INPUT;
        if (address != null && address.trim().length() == 0) return BLANK_INPUT;
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return EMAIL_INVALID;
        if (!Pattern.compile("(84|0[3|5|7|8|9])+([0-9]{8})\\b", Pattern.CASE_INSENSITIVE).matcher(phone).find()) return PHONE_INVALID;

        return FORM_VALIDATED;
    }
}