package com.example.jodernstore.model;

import androidx.annotation.NonNull;

import com.example.jodernstore.interfaces.ChangeNumItemsListener;

import java.util.List;

public class SharedCart extends Cart {
    private Long id;
    private String holderId;
    private List<CartItem> items;
    private Long total;
    private List<Long> memberId;
    private String name;

    public SharedCart() {
        holderId = "";
        name = "";
    }

    public SharedCart(Long id, String holderId, List<CartItem> items, String name, List<Long> memberId) {
        this.id = id;
        this.holderId = holderId;
        setItems(items);
        this.name = name;
        this.memberId = memberId;
    }

    public List<CartItem> getItems() {
        return items;
    }


    void setItems(@NonNull List<CartItem> items) {
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
        if (items == null)
            return -1;
        return items.size();
    }

    public int getNoMembers() { return memberId.size(); }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHolderId() {
        return holderId;
    }

    public void setHolderId(String holderId) {
        this.holderId = holderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShareCode() {
        return "NEED SHARE CODE"; // TODO
    }
}
