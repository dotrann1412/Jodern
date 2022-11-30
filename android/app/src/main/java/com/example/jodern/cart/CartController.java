package com.example.jodern.cart;

import android.content.Context;
import com.example.jodern.cart.cartitem.CartItem;
import com.example.jodern.cart.cartitem.CartItemDB;
import com.example.jodern.interfaces.ChangeNumItemsListener;
import android.util.Log;

import com.example.jodern.cart.cartitem.CartItem;
import com.example.jodern.cart.cartitem.CartItemDB;
import com.example.jodern.interfaces.ChangeNumItemsListener;
import com.example.jodern.model.Product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class CartController {
    private static final String TAG = CartController.class.getName();

    private List<CartItem> cartItemList;
    private List<Product> productList = new ArrayList<>();
    private AtomicLong totalPrice;

    private CartItemDB cartItemDB;
    private final Context context;

    private CartController(Context context) {
        this.context = context;
        try {
            Log.d(TAG, "CartController: retrieving cart items data");
            cartItemDB = CartItemDB.with(context);
            cartItemList = cartItemDB.orderItemDao().loadAll();
            Log.d(TAG, "CartController: retrieving cart successfully");
            Log.d(TAG, "Length: " + cartItemList.size());
        } catch (Exception e) {
            Log.d(TAG, "CartController: failed to retrieve cart items data");
            e.printStackTrace();
        }
    }

    private CartController(Context context, AtomicLong totalPrice) {
        this.context = context;
        this.totalPrice = totalPrice;
        try {
            Log.d(TAG, "CartController: retrieving cart items data");
            cartItemDB = CartItemDB.with(context);
            cartItemList = cartItemDB.orderItemDao().loadAll();
            Log.d(TAG, "CartController: retrieving cart successfully");
        } catch (Exception e) {
            Log.d(TAG, "CartController: failed to retrieve cart items data");
            e.printStackTrace();
        }
    }

    public static CartController with(Context context) {
        return new CartController(context);
    }

    public static CartController with(Context context, AtomicLong totalPrice) {
        return new CartController(context, totalPrice);
    }

    public void addToCart(CartItem cartItem) {
        try {
            // update on database
            cartItemDB.orderItemDao().insert(cartItem);
            cartItemList.add(cartItem);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<CartItem> getCartList() {
        return cartItemList;
    }

    // this function is just used for demo
    public void setCartList(List<CartItem> cartItemList) {
        // please sort cartItemList by productId
        this.cartItemList = cartItemList;
        this.cartItemList.sort((o1, o2) -> (int) (o1.getProductId() - o2.getProductId()));
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
        this.productList.sort((o1, o2) -> (int) (o1.getId() - o2.getId()));
    }

    public List<Product> getProductList() {
        return productList;
    }

    public void increaseNumItems(int position, ChangeNumItemsListener changeNumItemsListener) {
        CartItem cartItem = cartItemList.get(position);

        // update on database
        cartItemDB.orderItemDao().increaseQuantity(cartItem.getProductId());

        // update on code
        cartItem.setQuantity(cartItem.getQuantity() + 1);
        cartItemList.set(position, cartItem);

        changeNumItemsListener.onChanged();
    }

    public void decreaseNumItems(int position, ChangeNumItemsListener changeNumItemsListener) {
        CartItem cartItem = cartItemList.get(position);

        if (cartItemList.get(position).getQuantity() > 1) {
            // update on database
            cartItemDB.orderItemDao().decreaseQuantity(cartItem.getProductId());

            // update on code
            cartItem.setQuantity(cartItem.getQuantity() - 1);
            cartItemList.set(position, cartItem);

            changeNumItemsListener.onChanged();
        }
    }

    public void deleteItem(int position, ChangeNumItemsListener changeNumItemsListener) {
        if (position == -1)
            return;

        // update on database
        cartItemDB.orderItemDao().delete(cartItemList.get(position));

        // update on code
        cartItemList.remove(position);
        productList.remove(position);

        changeNumItemsListener.onChanged();
    }
}
