package android.jodern.app.cart;

import android.content.Context;
import android.jodern.app.cart.cartitem.CartItem;
import android.jodern.app.cart.cartitem.CartItemDB;
import android.jodern.app.interfaces.ChangeNumItemsListener;
import android.jodern.app.model.Product;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CartController {
    private static final String TAG = CartController.class.getName();

    private List<CartItem> cartItemList;
    private CartItemDB cartItemDB;

    private CartController(Context context) {

        try {
            Log.d(TAG, "CartController: retrieving cart items data");
            cartItemDB = CartItemDB.getInstance(context);
            cartItemList = cartItemDB.orderItemDao().loadAll();
        } catch (Exception e) {
            Log.d(TAG, "CartController: failed to retrieve cart items data");
            e.printStackTrace();
        }

        initCartItemList();
    }

    public static CartController with(Context context) {
        return new CartController(context);
    }

    private void initCartItemList() {
        // TODO get cart item list from storage instead of this dummy data

        for (int i = 100; i < 5; ++i) {
            CartItem cartItem = new CartItem();
            cartItem.setQuantity(150);
            cartItem.setSize("XL");
            cartItem.setProductId(Long.valueOf(i));
            cartItemDB.orderItemDao().insert(cartItem);
        }
    }

    public List<CartItem> getCartList() {
        return cartItemList;
    }

    public long getSubTotal() {
        try {
            long result = 0L;

            Product product = null;
            for (CartItem item : cartItemList) {
//                // TODO: retrieve product
//                // product = new Product(...)
//                assert product != null;
//                result += product.getPrice() * item.getQuantity();
            }

            Log.d(TAG, "getSubTotal: get subtotal successfully");
            return result;
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            return -1L;
        }
    }

    public int getCartListSize() {
        try {
            return cartItemList.size();
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            return -1;
        }
    }

    public void increaseNumItems(List<CartItem> cartItemList, int position, ChangeNumItemsListener changeNumItemsListener) {
        CartItem cartItem = cartItemList.get(position);

        // update on database
        cartItemDB.orderItemDao().increaseQuantity(cartItem.getProductId());

        // update on code
        cartItem.setQuantity(cartItem.getQuantity() + 1);
        cartItemList.set(position, cartItem);

        changeNumItemsListener.onChanged();
    }

    public void decreaseNumItems(List<CartItem> cartItemList, int position, ChangeNumItemsListener changeNumItemsListener) {
        CartItem cartItem = cartItemList.get(position);

        if (cartItemList.get(position).getQuantity() > 1) {
            // update on database
            cartItemDB.orderItemDao().decreaseQuantity(cartItem.getProductId());

            // update on code
            cartItem.setQuantity(cartItem.getQuantity() - 1);
            cartItemList.set(position, cartItem);
        }
        else if (cartItemList.get(position).getQuantity() == 1) {
            // update on database
            cartItemDB.orderItemDao().delete(cartItemList.get(position));

            // update on code
            cartItemList.remove(position);
        }
        changeNumItemsListener.onChanged();
    }

    public void deleteItem(List<CartItem> cartItemList, int position, ChangeNumItemsListener changeNumItemsListener) {
        cartItemDB.orderItemDao().delete(cartItemList.get(position));
    }
}
