package com.example.jodern.wishlist;

import android.content.Context;
import android.util.Log;

import com.example.jodern.wishlist.wishlistitem.WishlistItem;
import com.example.jodern.wishlist.wishlistitem.WishlistItemDB;
import com.example.jodern.interfaces.ChangeNumItemsListener;
import com.example.jodern.model.Product;

import java.util.ArrayList;
import java.util.List;

public class WishlistController {
    private static final String TAG = com.example.jodern.wishlist.WishlistController.class.getName();

    private List<WishlistItem> wishlistItemList;
    private List<Product> productList = new ArrayList<>();

    private WishlistItemDB wishlistItemDB;
    private final Context context;

    private WishlistController(Context context) {
        this.context = context;
        try {
            Log.d(TAG, "retrieving wishlist items data");
            wishlistItemDB = WishlistItemDB.with(context);
            wishlistItemList = wishlistItemDB.wishlistItemDao().loadAll();
            Log.d(TAG, "retrieving wishlist successfully");
            Log.d(TAG, "Length: " + wishlistItemList.size());
        } catch (Exception e) {
            Log.d(TAG, "failed to retrieve cart items data");
            e.printStackTrace();
        }
    }
    
    
    public static com.example.jodern.wishlist.WishlistController with(Context context) {
        return new com.example.jodern.wishlist.WishlistController(context);
    }

    public void addToWishlist(WishlistItem wishlistItem) {
        try {
            // update on database
            wishlistItemDB.wishlistItemDao().insert(wishlistItem);
            wishlistItemList.add(wishlistItem);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<WishlistItem> getWishlistItemList() {
        return wishlistItemList;
    }

    public void setProductList(List<Product> productList) {
            this.productList.clear();
            for (int i = 0; i < wishlistItemList.size(); i++) {
                for (int j = 0; j < productList.size(); j++) {
                    if (wishlistItemList.get(i).getProductId() == productList.get(j).getId()) {
                        this.productList.add(productList.get(j));
                        break;
                    }
                }
            }
//        this.productList = productList;
//        this.productList.sort((o1, o2) -> (int) (o1.getId() - o2.getId()));
    }

    public List<Product> getProductList() {
        return productList;
    }

    public void deleteItem(int position, ChangeNumItemsListener changeNumItemsListener) {
        if (position == -1)
            return;

        // update on database
        wishlistItemDB.wishlistItemDao().delete(wishlistItemList.get(position));

        // update on code
        wishlistItemList.remove(position);
        if (productList.size() > position)
            productList.remove(position);

        changeNumItemsListener.onChanged();
    }

    public void deleteItem(Long id, ChangeNumItemsListener changeNumItemsListener) {
        int position = -1;
        for (int i = 0; i < wishlistItemList.size(); i++) {
            if (wishlistItemList.get(i).getProductId().equals(id)) {
                position = i;
                break;
            }
        }

        deleteItem((int)position, changeNumItemsListener);
    }
}
