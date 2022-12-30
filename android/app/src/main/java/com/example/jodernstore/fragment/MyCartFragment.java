package com.example.jodernstore.fragment;

import static com.example.jodernstore.Utils.vndFormatPrice;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.jodernstore.BuildConfig;
import com.example.jodernstore.MainActivity;
import com.example.jodernstore.R;
import com.example.jodernstore.activity.OrderActivity;
import com.example.jodernstore.adapter.CartAdapter;
import com.example.jodernstore.model.CartItem;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.interfaces.ChangeNumItemsListener;
import com.example.jodernstore.model.Cart;
import com.example.jodernstore.provider.CartProvider;
import com.example.jodernstore.provider.GeneralProvider;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MyCartFragment extends Fragment {
    private final Long SHIPPING_FEE = 30000L;

    public static final String TAG = "MyCartFragment";
    private FrameLayout parentView;

    private Cart currentCart;
    private boolean shouldCallUpdateAPI = false;
    private boolean shouldCallFetchAPI = false;

    private RecyclerView cartRecyclerView;
    private LinearLayout cartLayout, cartEmptyWrapper, cartLoadingWrapper;
    private TextView subTotalText;
    private String subTotalStr, shippingStr, totalStr;
    private MaterialButton cartOrderBtn, cartAppointBtn, cartGoToShop;

    public MyCartFragment() {
        // Required empty public constructor
        super(R.layout.fragment_my_cart);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
        setEvents();
        showInitialMessage();
//        getAndShowCartItems();    // TODO: uncomment later
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shouldCallFetchAPI) {
            getAndShowCartItems();
        }
    }

    @Override
    public void onStop() {
        shouldCallFetchAPI = true;

        // Call API to update wishlist data (if necessary)
        if (shouldCallUpdateAPI) {
            System.out.println("updating cart");
            updateCartData();
        }
        super.onStop();
    }

    private void showInitialMessage() {
        Bundle bundle = getArguments();
        if (bundle == null)
            return;
        String message = bundle.getString("message");
        if (message == null)
            return;
        MySnackbar.inforSnackar(getContext(), parentView, message).setAnchorView(R.id.mainNavBarSearchBtn).show();
    }

    private void initViews() {
        parentView = getView().findViewById(R.id.cartParentView);
        cartRecyclerView = getView().findViewById(R.id.myCartRecyclerView);
        cartLayout = getView().findViewById(R.id.myCartLayout);
        subTotalText = getView().findViewById(R.id.myCartSubTotalText);
        cartEmptyWrapper = getView().findViewById(R.id.myCartEmptyWrapper);
        cartLoadingWrapper = getView().findViewById(R.id.myCartLoadingWrapper);
        cartOrderBtn = getView().findViewById(R.id.myCartOrderBtn);
        cartAppointBtn = getView().findViewById(R.id.myCartAppointBtn);
        cartGoToShop = getView().findViewById(R.id.myCartGoToShop);
    }


    private void setEvents() {
        cartOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSummaryDialog();
            }
        });

        cartAppointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAppointDialog();
            }
        });

        cartGoToShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Back pressed handling
                Intent searchIntent = new Intent(MyCartFragment.this.getContext(), ProductListFragment.class);
                searchIntent.putExtra("entry", "product-list");
                searchIntent.putExtra("sex", "nam");
                searchIntent.putExtra("categoryName", "Thời trang nam");
                GeneralProvider.with(MyCartFragment.this.getContext()).setSearchIntent(searchIntent);

                // Go to product list fragment of main activity
                Intent intent = new Intent(MyCartFragment.this.getContext(), MainActivity.class);
                intent.putExtra("nextFragment", ProductListFragment.TAG);
                intent.putExtra("entry", "product-list");
                intent.putExtra("sex", "nam");
                intent.putExtra("categoryName", "Thời trang nam");
                startActivity(intent);
            }
        });
    }

    private void showSummaryDialog() {
        final Dialog dialog = new Dialog(requireActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_cart_summary);

        // Init views
        TextView subTotalText = dialog.findViewById(R.id.cartSummarySubTotalText);
        TextView shippingText = dialog.findViewById(R.id.cartSummaryShippingText);
        TextView totalText = dialog.findViewById(R.id.cartSummaryTotalText);
        MaterialButton checkoutBtn = dialog.findViewById(R.id.cartSummaryCheckoutBtn);

        // Set text
        subTotalText.setText(subTotalStr);
        shippingText.setText(shippingStr);
        totalText.setText(totalStr);

        // Set events
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(requireActivity().getApplicationContext(), OrderActivity.class);
                startActivity(intent);
            }
        });

        dialog.show();
    }


    private void showAppointDialog() {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_cart_appoint);

        // Init views
        // ...

        // Set data
        // ...

        // Set events
        // ...

        dialog.show();
    }

    private void getAndShowCartItems() {
        currentCart = CartProvider.getInstance().getMyCart();
//        if (cartController.getCartList().size() == 0) {
//            cartEmptyWrapper.setVisibility(View.VISIBLE);
//            cartLoadingWrapper.setVisibility(View.GONE);
//            cartLayout.setVisibility(View.GONE);
//            return;
//        }
//
//        ArrayList<Long> productIds = new ArrayList<>();
//        for (CartItem cartItem : cartController.getCartList()) {
//            productIds.add(cartItem.getProductId());
//        }
        cartLoadingWrapper.setVisibility(View.VISIBLE);

        // call API
        String entry = "cart";
        String url = BuildConfig.SERVER_URL + entry + "/";
        String jwt = GeneralProvider.with(getContext()).getJWT();
        JsonObjectRequest postRequest = new JsonObjectRequest (
                url,
                new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        handleResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        cartLoadingWrapper.setVisibility(View.GONE);
                        MySnackbar.inforSnackar(getContext(), parentView, getString(R.string.error_message)).setAnchorView(R.id.mainNavBarSearchBtn).show();
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
        GeneralProvider.with(this.getContext()).addToRequestQueue(postRequest);
    }

    public void updateCartData() {
        // TODO: Update cart data
    }

    private void handleResponse(JSONObject response) {
        cartLoadingWrapper.setVisibility(View.GONE);
        try {
            JSONArray items = (JSONArray)response.get("details");
            for (int j = 0; j < items.length(); j++) {
                CartItem item = CartItem.parseJSON((JSONObject)items.get(j));
                if (item != null)
                    currentCart.addItem(item);
            }
            updateTotalPrice();

            CartAdapter adapter = new CartAdapter(this.getContext(), currentCart, new ChangeNumItemsListener() {
                @Override
                public void onChanged() {
                    updateTotalPrice();
                }
            });

            cartRecyclerView.setAdapter(adapter);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
            cartRecyclerView.setLayoutManager(layoutManager);
            showCartLayout(currentCart.getItems().isEmpty());
        }
        catch (Exception e) {
            showErrorMsg();
        }
    }

    private void updateTotalPrice() {
        Long subTotal = currentCart.getTotal();
        subTotalStr = vndFormatPrice(subTotal);
        totalStr = vndFormatPrice(subTotal + SHIPPING_FEE);
        shippingStr = vndFormatPrice(SHIPPING_FEE);

        subTotalText.setText(subTotalStr);

        if (subTotal.equals(0L)) {
            showCartLayout(true);
        }
    }

    private void showCartLayout(boolean isEmpty) {
        if (isEmpty) {
            cartEmptyWrapper.setVisibility(View.VISIBLE);
            cartLayout.setVisibility(View.GONE);
        } else {
            cartEmptyWrapper.setVisibility(View.GONE);
            cartLayout.setVisibility(View.VISIBLE);
        }
    }

    private void showErrorMsg() {
        MySnackbar.inforSnackar(getContext(), parentView, getString(R.string.error_message)).show();
    }
}