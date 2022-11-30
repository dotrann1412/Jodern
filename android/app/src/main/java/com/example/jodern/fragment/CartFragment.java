package com.example.jodern.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jodern.R;
import com.example.jodern.cart.CartController;
import com.example.jodern.cart.cartitem.CartItem;

import java.util.List;

public class CartFragment extends Fragment {
    private ImageButton navbarBtn;
    private RecyclerView cartRecyclerView;
    private CartController cartController;
    private LinearLayout cartLayout, cartEmptyWrapper, cartLoadingWrapper;
    TextView subTotalText, shippingText, totalText;

    public CartFragment() {
        // Required empty public constructor
        super(R.layout.fragment_cart);
    }

    public CartFragment(ImageButton navbarBtn) {
        super(R.layout.fragment_home);
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
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (navbarBtn != null)
            navbarBtn.setImageResource(R.drawable.ic_cart_filled);

        initViews();
        handleAddToCartSignal();
        showCartItems();
//        initCartLists();
    }

    private void showCartItems() {
        List<CartItem> cartItems = cartController.getCartList();


    }

    private void handleAddToCartSignal() {
        Bundle args = getArguments();
        if (args == null)
            return;

        Long productId = args.getLong("productId", -1);
        if (productId == -1)
            return;

        Integer quantity = args.getInt("quantity", -1);
        String size = args.getString("size", "");
        CartItem newItem = new CartItem(productId, quantity, size);
        cartController.addToCart(newItem);
    }

    private void initViews() {
        cartRecyclerView = getView().findViewById(R.id.cartRecyclerView);
        cartLayout = getView().findViewById(R.id.cartLayout);
        subTotalText = getView().findViewById(R.id.cartSubTotalText);
        shippingText = getView().findViewById(R.id.cartShippingText);
        totalText = getView().findViewById(R.id.cartTotalText);
        cartEmptyWrapper = getView().findViewById(R.id.cartEmptyWrapper);
        // TODO: card loading effect (maybe it's very hard to implement now, because we are call API inside adapter)
        cartLoadingWrapper = getView().findViewById(R.id.cartLoadingWrapper);
    }

    @Override
    public void onDestroyView() {
        if (navbarBtn != null)
            navbarBtn.setImageResource(R.drawable.ic_cart);
        super.onDestroyView();
    }
}