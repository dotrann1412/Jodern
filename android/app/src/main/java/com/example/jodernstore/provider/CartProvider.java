package com.example.jodernstore.provider;

import com.example.jodernstore.interfaces.ChangeNumItemsListener;
import com.example.jodernstore.model.Cart;

import java.util.ArrayList;

public class CartProvider {
    private static CartProvider instance = null;
    private Cart myCart;
    private ArrayList<Cart> sharedCarts;


    public static CartProvider getInstance() {
        if (instance == null) {
            synchronized (CartProvider.class) {
                if (instance == null) {
                    instance = new CartProvider();
                }
            }
        }
        return instance;
    }

    private CartProvider() {
        myCart = new Cart();
        sharedCarts = new ArrayList<>();
    }

    // TODO: cart
}
