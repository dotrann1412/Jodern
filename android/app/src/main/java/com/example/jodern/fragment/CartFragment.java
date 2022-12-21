package com.example.jodern.fragment;

import static com.example.jodern.Utils.vndFormatPrice;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.jodern.BuildConfig;
import com.example.jodern.MainActivity;
import com.example.jodern.R;
import com.example.jodern.activity.OrderActivity;
import com.example.jodern.adapter.CartAdapter;
import com.example.jodern.cart.CartController;
import com.example.jodern.cart.cartitem.CartItem;
import com.example.jodern.customwidget.MySnackbar;
import com.example.jodern.interfaces.ChangeNumItemsListener;
import com.example.jodern.model.Category;
import com.example.jodern.model.Product;
import com.example.jodern.provider.Provider;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {
    private final Long SHIPPING_FEE = 30000L;

    public static final String TAG = "CartFragment";
    private FrameLayout parentView;

    private ImageButton navbarBtn;
    private RecyclerView cartRecyclerView;
    private CartController cartController;
    private LinearLayout cartLayout, cartEmptyWrapper, cartLoadingWrapper;
    private ImageButton cartBackBtn;
    private TextView subTotalText;
    private String subTotalStr, shippingStr, totalStr;
    private MaterialButton cartOrderBtn, cartAppointBtn, cartGoToShop;

    public CartFragment() {
        // Required empty public constructor
        super(R.layout.fragment_cart);
    }

    public CartFragment(ImageButton navbarBtn) {
        super(R.layout.fragment_cart);
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
        Provider.with(this.getContext()).setCurrentFragment(TAG);
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (navbarBtn != null)
            navbarBtn.setImageResource(R.drawable.ic_cart_filled);
        cartController = CartController.with(this.getContext());

        initViews();
        setEvents();
        showInitialMessage();
        showCartItems();
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
        cartRecyclerView = getView().findViewById(R.id.cartRecyclerView);
        cartLayout = getView().findViewById(R.id.cartLayout);
        subTotalText = getView().findViewById(R.id.cartSubTotalText);
        cartEmptyWrapper = getView().findViewById(R.id.cartEmptyWrapper);
        cartLoadingWrapper = getView().findViewById(R.id.cartLoadingWrapper);
        cartBackBtn = getView().findViewById(R.id.cartBackBtn);
        cartOrderBtn = getView().findViewById(R.id.cartOrderBtn);
        cartAppointBtn = getView().findViewById(R.id.cartAppointBtn);
        cartGoToShop = getView().findViewById(R.id.cartGoToShop);
    }


    private void setEvents() {
        cartBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });

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
                Fragment fragment = new ProductListFragment();
                Bundle bundle = new Bundle();
                bundle.putString("entry", "product-list");
                bundle.putString("sex", "nam");
                bundle.putString("categoryName", "Thời trang nam");
                fragment.setArguments(bundle);

                // Back pressed handling
                FragmentActivity activity = getActivity();
                Intent searchIntent = new Intent(activity, ProductListFragment.class);
                searchIntent.putExtra("entry", "product-list");
                bundle.putString("sex", "nam");
                bundle.putString("categoryName", "Thời trang nam");
                Provider.with(activity).setSearchIntent(searchIntent);

                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.mainFragmentContainer, fragment);
                fragmentTransaction.addToBackStack("productList");
                fragmentTransaction.commit();
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

    private void showCartItems() {
        if (cartController.getCartList().size() == 0) {
            cartEmptyWrapper.setVisibility(View.VISIBLE);
            cartLoadingWrapper.setVisibility(View.GONE);
            cartLayout.setVisibility(View.GONE);
            return;
        }

        ArrayList<Long> productIds = new ArrayList<>();
        for (CartItem cartItem : cartController.getCartList()) {
            productIds.add(cartItem.getProductId());
        }

        // call API to get products
        cartLoadingWrapper.setVisibility(View.VISIBLE);
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
                        cartLoadingWrapper.setVisibility(View.GONE);
                        MySnackbar.inforSnackar(getContext(), parentView, getString(R.string.error_message)).setAnchorView(R.id.mainNavBarSearchBtn).show();
                    }
                }
        );
        Provider.with(this.getContext()).addToRequestQueue(getRequest);
    }

    private void handleResponse(JSONObject response) {
        cartLoadingWrapper.setVisibility(View.GONE);

        // Get the information of products in cart
        List<Product> cartProducts = Product.parseProductListFromResponse(response);
        cartController.setProductList(cartProducts);

        updateTotalPrice();

        CartAdapter adapter = new CartAdapter(cartController, this.getContext(), new ChangeNumItemsListener() {
            @Override
            public void onChanged() {
                updateTotalPrice();
            }
        });
        cartRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        cartRecyclerView.setLayoutManager(layoutManager);

        showCartLayout(cartProducts.isEmpty());
    }

    private void updateTotalPrice() {
        Long subTotal = 0L;
        List<CartItem> cartItems = cartController.getCartList();
        List<Product> cartProducts = cartController.getProductList();
        for (int i = 0; i < cartItems.size(); i++) {
            subTotal += cartItems.get(i).getQuantity() * cartProducts.get(i).getPrice();
        }
        subTotalStr = vndFormatPrice(subTotal);
        totalStr = vndFormatPrice(subTotal + SHIPPING_FEE);
        shippingStr = vndFormatPrice(SHIPPING_FEE);

        subTotalText.setText(subTotalStr);

        if (subTotal == 0L) {
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

    @Override
    public void onDestroyView() {
        if (navbarBtn != null)
            navbarBtn.setImageResource(R.drawable.ic_cart);
        super.onDestroyView();
    }
}