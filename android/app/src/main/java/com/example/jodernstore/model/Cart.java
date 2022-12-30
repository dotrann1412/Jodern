package com.example.jodernstore.model;

import com.example.jodernstore.interfaces.ChangeNumItemsListener;

import java.util.ArrayList;

public class Cart {
    private Long id;
    private String holderId;
    private ArrayList<CartItem> items;
    private Long total;

    public Cart() {
        id = 0L;
        holderId = "";
        items = new ArrayList<>();
        total = 0L;
    }

    public Cart(Long id, String holderId, ArrayList<CartItem> items) {
        this.id = id;
        this.holderId = holderId;
        setItems(items);
    }

    public ArrayList<CartItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<CartItem> items) {
        this.items = items;
        total = 0L;
        for (CartItem item : items) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
    }

    public void addItem(CartItem item) {
        total += item.getProduct().getPrice() * item.getQuantity();
        for (CartItem cartItem : items) {
            if (cartItem.getProduct().getId().equals(item.getProduct().getId()) && cartItem.getSize().equals(item.getSize())) {
                cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
                return;
            }
        }
        items.add(item);
    }

    public void removeItem(int position, ChangeNumItemsListener changeNumItemsListener) {
        if (position == -1)
            return;

        if (items.size() > position) {
            items.remove(position);
            total -= items.get(position).getProduct().getPrice() * items.get(position).getQuantity();
        }

        changeNumItemsListener.onChanged();
    }

    public void clear() {
        items.clear();
    }

    public int getNumItems() {
        return items.size();
    }

    public Long getTotal() {
        return total;
    }

    public void increaseQuantity(int position, ChangeNumItemsListener changeNumItemsListener) {
        if (position == -1)
            return;

        if (items.size() > position) {
            items.get(position).setQuantity(items.get(position).getQuantity() + 1);
            total += items.get(position).getProduct().getPrice();
        }

        changeNumItemsListener.onChanged();
    }

    public void decreaseQuantity(int position, ChangeNumItemsListener changeNumItemsListener) {
        if (position == -1)
            return;

        if (items.size() > position) {
            if (items.get(position).getQuantity() > 1) {
                items.get(position).setQuantity(items.get(position).getQuantity() - 1);
                total -= items.get(position).getProduct().getPrice();
            }
        }

        changeNumItemsListener.onChanged();
    }
}
