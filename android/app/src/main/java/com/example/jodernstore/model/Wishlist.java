package com.example.jodernstore.model;

import com.example.jodernstore.interfaces.ChangeNumItemsListener;

import java.util.ArrayList;

public class Wishlist {
    private ArrayList<Product> items;

    public Wishlist() {
        items = new ArrayList<>();
    }

    public Wishlist(ArrayList<Product> items) {
        this.items = items;
    }

    public ArrayList<Product> getItems() {
        return items;
    }

//    public void setItems(ArrayList<Product> items) {
//        this.items = items;
//    }

    public void removeItem(int position, ChangeNumItemsListener changeNumItemsListener) {
        if (position == -1)
            return;

        if (items.size() > position) {
            items.remove(position);
        }

        changeNumItemsListener.onChanged();
    }
}
