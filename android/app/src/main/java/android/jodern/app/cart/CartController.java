package android.jodern.app.cart;

import android.content.Context;
import android.jodern.app.cart.cartitem.CartItem;
import android.jodern.app.cart.cartitem.CartItemDB;
import android.jodern.app.interfaces.ChangeNumItemsListener;
import android.jodern.app.model.Product;
import android.jodern.app.provider.Provider;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CartController {
    private static final String TAG = CartController.class.getName();

    private List<CartItem> cartItemList;
    private CartItemDB cartItemDB;
    private final Context context;


    private CartController(Context context) {
        this.context = context;
        try {
            Log.d(TAG, "CartController: retrieving cart items data");
            cartItemDB = CartItemDB.with(context);
            cartItemList = cartItemDB.orderItemDao().loadAll();
            Log.d(TAG, "CartController: retrieving cart successfully");
        } catch (Exception e) {
            Log.d(TAG, "CartController: failed to retrieve cart items data");
            e.printStackTrace();
        }

        for (int i = 0; i < cartItemList.size(); ++i) {
            Log.d(TAG, "CartController: " + cartItemList.get(i));
        }
    }

    public static CartController with(Context context) {
        return new CartController(context);
    }

    public void addToCart(CartItem cartItem) {
        try {
            cartItemList.add(cartItem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<CartItem> getCartList() {
        return cartItemList;
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
        // update on database
        cartItemDB.orderItemDao().delete(cartItemList.get(position));

        // update on code
        cartItemList.remove(position);

        changeNumItemsListener.onChanged();
    }


}
