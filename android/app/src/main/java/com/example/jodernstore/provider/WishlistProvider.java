package com.example.jodernstore.provider;

import com.example.jodernstore.interfaces.ChangeNumItemsListener;
import com.example.jodernstore.model.Product;

import java.util.ArrayList;

public class WishlistProvider {
    private static WishlistProvider instance = null;
    private ArrayList<Product> items;

    public static WishlistProvider getInstance() {
        if (instance == null) {
            synchronized (WishlistProvider.class) {
                if (instance == null) {
                    instance = new WishlistProvider();
                }
            }
        }
        return instance;
    }

    private WishlistProvider() {
        items = new ArrayList<>();
    }

    public ArrayList<Product> getItems() {
        return items;
    }

    public void setItems(ArrayList<Product> items) {
        this.items = items;
    }

    public void removeItem(int position, ChangeNumItemsListener changeNumItemsListener) {
        if (position == -1)
            return;

        if (items.size() > position) {
            items.remove(position);
        }

        changeNumItemsListener.onChanged();
    }
}
