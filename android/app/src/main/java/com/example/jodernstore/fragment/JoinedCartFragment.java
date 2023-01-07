package com.example.jodernstore.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
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
import com.example.jodernstore.adapter.CartListAdapter;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.model.SharedCart;
import com.example.jodernstore.provider.GeneralProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoinedCartFragment extends Fragment {

    public static final String TAG = "JoinedCartFragment";

    private List<SharedCart> joinedCartList;
    private boolean shouldCallUpdateAPI = false;
    private boolean shouldCallFetchAPI = false;

    private FrameLayout parentView;
    private RecyclerView cartRecyclerView;
    private LinearLayout cartEmptyWrapper, cartLoadingWrapper;
//    private MaterialButton cartGoToShop;

    private FloatingActionButton joinedCartFloatBtn;

    private LinearLayout navbarBtn;

    public JoinedCartFragment() {
        // Required empty public constructor
        super(R.layout.fragment_joined_cart);
    }

    public JoinedCartFragment(LinearLayout navbarBtn) {
        // Required empty public constructor
        super(R.layout.fragment_joined_cart);
        this.navbarBtn = navbarBtn;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_joined_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
        setEvents();
        showInitialMessage();
        getAndShowJoinedCarts();
    }

    @Override
    public void onResume() {
        super.onResume();

        navbarBtn.findViewWithTag("image").setBackground(getContext().getDrawable(R.drawable.card_image_selected));
        ((TextView) navbarBtn.findViewWithTag("text")).setTextColor(getContext().getColor(R.color.primary));
        ((TextView) navbarBtn.findViewWithTag("text")).setTypeface(null, Typeface.BOLD);

        if (!shouldCallFetchAPI) {
            shouldCallFetchAPI = true;
            return;
        }

        getAndShowJoinedCarts();
    }

    @Override
    public void onStop() {
        super.onStop();

        navbarBtn.findViewWithTag("image").setBackground(getContext().getDrawable(R.drawable.card_image_shape));
        ((TextView) navbarBtn.findViewWithTag("text")).setTextColor(getContext().getColor(R.color.text));
        ((TextView) navbarBtn.findViewWithTag("text")).setTypeface(null, Typeface.NORMAL);
    }

    private void initViews() {
        Log.d(TAG, "initViews: ");
        parentView = requireView().findViewById(R.id.joinedCartParentView);
        cartRecyclerView = requireView().findViewById(R.id.joinedCartRecyclerView);
        cartEmptyWrapper = requireView().findViewById(R.id.joinedCartEmptyWrapper);
        cartLoadingWrapper = requireView().findViewById(R.id.joinedCartLoadingWrapper);
//        cartGoToShop = requireView().findViewById(R.id.joinedCartGoToShop);
        joinedCartFloatBtn = requireView().findViewById(R.id.joinedCartFloatBtn);
    }

    private void setEvents() {
        Log.d(TAG, "setEvents: ");
//        cartGoToShop.setOnClickListener(view -> {
//            // Back pressed handling
//            Intent searchIntent = new Intent(JoinedCartFragment.this.getContext(), ProductListFragment.class);
//            searchIntent.putExtra("entry", "product-list");
//            searchIntent.putExtra("sex", "nam");
//            searchIntent.putExtra("categoryName", "Thời trang nam");
//            GeneralProvider.with(JoinedCartFragment.this.getContext()).setSearchIntent(searchIntent);
//
//            // Go to product list fragment of main activity
//            Intent intent = new Intent(JoinedCartFragment.this.getContext(), MainActivity.class);
//            intent.putExtra("nextFragment", ProductListFragment.TAG);
//            intent.putExtra("entry", "product-list");
//            intent.putExtra("sex", "nam");
//            intent.putExtra("categoryName", "Thời trang nam");
//            startActivity(intent);
//        });

        joinedCartFloatBtn.setOnClickListener(view -> {
            showJoinCartDialog();
        });
    }

    private void showJoinCartDialog() {
        final Dialog dialog = new Dialog(requireActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_join_cart);

        // Init views
        TextInputEditText codeInput = dialog.findViewById(R.id.joinedCartDialogInput);
        MaterialButton joinBtn = dialog.findViewById(R.id.joinedCartDialogSubmit);

        // Set events
        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                // Call API
                cartLoadingWrapper.setVisibility(View.VISIBLE);
                try {
                    String entry = "join-shared-cart";
                    String url = BuildConfig.SERVER_URL + entry + "/";
                    JSONObject params = new JSONObject();
                    params.put("cartid", codeInput.getText());
                    String jwt = GeneralProvider.with(getContext()).getJWT();
                    JsonObjectRequest postRequest = new JsonObjectRequest(
                            url,
                            params,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    cartLoadingWrapper.setVisibility(View.GONE);
                                    handleJoinResponse(response);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    System.out.println(error.toString());
                                    cartLoadingWrapper.setVisibility(View.GONE);
                                    showErrorMsg();
                                }
                            }
                    ) {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("Access-token", jwt);
                            return params;
                        }
                    };
                    GeneralProvider.with(getContext()).addToRequestQueue(postRequest);
                } catch (Exception e) {
                    showErrorMsg();
                }

            }
        });

        dialog.show();
    }

    private void handleJoinResponse(JSONObject response) {
        try {
            JSONObject info = response.getJSONObject("info");
            SharedCart newItem = SharedCart.parseBasicJson(info);
            joinedCartList.add(newItem);
            CartListAdapter adapter = new CartListAdapter(this.getContext(), joinedCartList, false);
            cartRecyclerView.setAdapter(adapter);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
            cartRecyclerView.setLayoutManager(layoutManager);
            showCartLayout(this.joinedCartList.isEmpty());
        } catch (Exception e) {
            showErrorMsg();
        }
    }

    private void showErrorMsg() {
        MySnackbar.inforSnackbar(getContext(), parentView, getString(R.string.error_message)).show();
    }

    private void showInitialMessage() {
        Log.d(TAG, "showInitialMessage: ");
        Bundle bundle = getArguments();
        if (bundle == null)
            return;
        String message = bundle.getString("message");
        if (message == null)
            return;
        MySnackbar.inforSnackbar(getContext(), parentView, message).show();
    }

    @SuppressWarnings("deprecation")
    private void getAndShowJoinedCarts() {
        joinedCartList = new ArrayList<>();

        cartLoadingWrapper.setVisibility(View.VISIBLE);

        String entry = "joined-carts";
        String url = BuildConfig.SERVER_URL + entry;
        String jwt = GeneralProvider.with(getContext()).getJWT();
        JsonObjectRequest getRequest = new JsonObjectRequest(
                url,
                this::handleGetJoinedCartResponse,
                error -> {
                    cartLoadingWrapper.setVisibility(View.GONE);
                    MySnackbar.inforSnackbar(getContext(), parentView, getString(R.string.error_message)).show();
                }
        ) {
            @NonNull
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Access-token", jwt);
                return params;
            }
        };

        GeneralProvider.with(this.getContext()).addToRequestQueue(getRequest);
    }

    private void handleGetJoinedCartResponse(JSONObject response) {
        Log.d(TAG, "handleResponse: ");
        cartLoadingWrapper.setVisibility(View.GONE);
        try {
            JSONArray joinedCarts = response.getJSONArray("joined-carts");
            for (int i = 0; i < joinedCarts.length(); i++) {
                JSONObject item = joinedCarts.getJSONObject(i);
                SharedCart joinedCart = SharedCart.parseBasicJson(item);
                joinedCartList.add(joinedCart);
            }
            CartListAdapter adapter = new CartListAdapter(this.getContext(), joinedCartList, true);
            cartRecyclerView.setAdapter(adapter);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
            cartRecyclerView.setLayoutManager(layoutManager);
            showCartLayout(this.joinedCartList.isEmpty());

        } catch (Exception e) {
            MySnackbar.inforSnackbar(getContext(), parentView, getString(R.string.error_message)).show();
            Log.e(TAG, "handleResponse: " + e.getMessage());
            cartRecyclerView.setAdapter(null);
        }
    }

    private void showCartLayout(boolean isEmpty) {
        if (isEmpty) {
            cartEmptyWrapper.setVisibility(View.VISIBLE);
            cartRecyclerView.setVisibility(View.GONE);
        } else {
            cartEmptyWrapper.setVisibility(View.GONE);
            cartRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}