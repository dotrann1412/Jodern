package com.example.jodernstore.fragment;

import android.content.Intent;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.android.volley.toolbox.JsonObjectRequest;
import com.example.jodernstore.BuildConfig;
import com.example.jodernstore.MainActivity;
import com.example.jodernstore.R;
import com.example.jodernstore.adapter.CartListAdapter;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.model.CartItem;
import com.example.jodernstore.model.Product;
import com.example.jodernstore.model.SharedCart;
import com.example.jodernstore.provider.GeneralProvider;
import com.example.jodernstore.provider.SharedCartProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    private MaterialButton cartGoToShop;

    private FloatingActionButton joinedCartFloatBtn;

    public JoinedCartFragment() {
        // Required empty public constructor
        super(R.layout.fragment_joined_cart);
        Log.d(TAG, "constructed");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: ");
        return inflater.inflate(R.layout.fragment_joined_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ");
        initViews();
        setEvents();
        showInitialMessage();
//        getAndShowSharedCarts();    // TODO: uncomment later
        handleResponse(null);
    }

    private void initViews() {
        Log.d(TAG, "initViews: ");
        parentView = requireView().findViewById(R.id.joinedCartParentView);
        cartRecyclerView = requireView().findViewById(R.id.joinedCartRecyclerView);
        cartEmptyWrapper = requireView().findViewById(R.id.joinedCartEmptyWrapper);
        cartLoadingWrapper = requireView().findViewById(R.id.joinedCartLoadingWrapper);
        cartGoToShop = requireView().findViewById(R.id.joinedCartGoToShop);
        joinedCartFloatBtn = requireView().findViewById(R.id.joinedCartFloatBtn);
    }

    private void setEvents() {
        Log.d(TAG, "setEvents: ");
        cartGoToShop.setOnClickListener(view -> {
            // Back pressed handling
            Intent searchIntent = new Intent(JoinedCartFragment.this.getContext(), ProductListFragment.class);
            searchIntent.putExtra("entry", "product-list");
            searchIntent.putExtra("sex", "nam");
            searchIntent.putExtra("categoryName", "Thời trang nam");
            GeneralProvider.with(JoinedCartFragment.this.getContext()).setSearchIntent(searchIntent);

            // Go to product list fragment of main activity
            Intent intent = new Intent(JoinedCartFragment.this.getContext(), MainActivity.class);
            intent.putExtra("nextFragment", ProductListFragment.TAG);
            intent.putExtra("entry", "product-list");
            intent.putExtra("sex", "nam");
            intent.putExtra("categoryName", "Thời trang nam");
            startActivity(intent);
        });

        joinedCartFloatBtn.setOnClickListener(view -> {
            // TODO: show dialog to add new cart
        });
    }

    private void showInitialMessage() {
        Log.d(TAG, "showInitialMessage: ");
        Bundle bundle = getArguments();
        if (bundle == null)
            return;
        String message = bundle.getString("message");
        if (message == null)
            return;
        MySnackbar.inforSnackbar(getContext(), parentView, message).setAnchorView(R.id.mainNavBarSearchBtn).show();
    }

    @SuppressWarnings("deprecation")
    private void getAndShowSharedCarts() {
        cartLoadingWrapper.setVisibility(View.VISIBLE);

        // TODO: call shared cart API
        String entry = "";
        String url = BuildConfig.SERVER_URL + entry;
        String jwt = GeneralProvider.with(getContext()).getJWT();
        JsonObjectRequest postRequest = new JsonObjectRequest(
                url,
                new JSONObject(),
                this::handleResponse,
                error -> {
                    cartLoadingWrapper.setVisibility(View.GONE);
                    MySnackbar.inforSnackbar(getContext(), parentView, getString(R.string.error_message)).setAnchorView(R.id.mainNavBarSearchBtn).show();
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

//        GeneralProvider.with(this.getContext()).addToRequestQueue(postRequest);
    }

    private void handleResponse(JSONObject response) {
        Log.d(TAG, "handleResponse: ");
        cartLoadingWrapper.setVisibility(View.GONE);
        try {
            Log.d(TAG, "handleResponse: in try-catch block");
//            JSONArray sharedCarts = (JSONArray) response.get("carts");

            // TODO: extra steps to init `sharedCartList`
            List<SharedCart> parsedSharedCartList = new ArrayList<>();
            // parsing...
            Log.d(TAG, "handleResponse: check point 1");

            List<Long> pseudoId = new ArrayList<>();
            pseudoId.add(123L); pseudoId.add(125L);

            Log.d(TAG, "handleResponse: check point 2");

            ArrayList<String> urls = new ArrayList<>();
            urls.add("https://bizweb.sapocdn.net/100/438/408/products/vnk5274-hog-5.jpg?v=1663816469000");
            Product pseudoProd = new Product(
                    141L,
                    "Đầm Bé Gái In Thỏ Cột Nơ",
                    urls,
                    174300L,
                    "",
                    "vay-nu",
                    "vay-nu",
                    new Integer[] { 1, 1, 2, 2, 1}
            );
            Log.d(TAG, "handleResponse: check point 3");

            List<CartItem> items = new ArrayList<>();
            items.add(new CartItem(pseudoProd, 1, "XL"));
            items.add(new CartItem(pseudoProd, 2, "L"));
            items.add(new CartItem(pseudoProd, 2, "XL"));
            items.add(new CartItem(pseudoProd, 1, "M"));
            items.add(new CartItem(pseudoProd, 1, "S"));

            Log.d(TAG, "handleResponse: " + new SharedCart(0L, "holder1", items, "Cart 1", pseudoId));
            // pseudo
            parsedSharedCartList.add(new SharedCart(0L, "holder1", items, "Cart 1", pseudoId));
            parsedSharedCartList.add(new SharedCart(1L, "holder2", items, "Cart 2", pseudoId));
            parsedSharedCartList.add(new SharedCart(2L, "holder3", items, "Cart 3", pseudoId));
            parsedSharedCartList.add(new SharedCart(3L, "holder4", items, "Cart 4", pseudoId));
            parsedSharedCartList.add(new SharedCart(4L, "holder5", items, "Cart 5", pseudoId));
            parsedSharedCartList.add(new SharedCart(5L, "holder6", items, "Cart 6", pseudoId));

            Log.d(TAG, "handleResponse: " + parsedSharedCartList);
            SharedCartProvider.getInstance().setSharedCartList(parsedSharedCartList);
            joinedCartList = SharedCartProvider.getInstance().getSharedCartList(); // TODO: idk if the provider is differ, I use it now and may change later

            Log.d(TAG, "handleResponse: parsed successfully, initializing recycler view");
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