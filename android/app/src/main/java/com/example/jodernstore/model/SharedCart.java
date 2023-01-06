package com.example.jodernstore.model;

import androidx.annotation.NonNull;

import com.example.jodernstore.interfaces.ChangeNumItemsListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SharedCart extends Cart {
    private String id;
    private String name;
    private Long total;
    private int numItems;
    private int numMember;
    private String holderName;
    private String holderAvatar;
    private List<CartItem> items;
    private List<String> history;

    public SharedCart(String id, String name, Long total, int numItems, int numMember) {
        this.id = id;
        this.name = name;
        this.total = total;
        this.numItems = numItems;
        this.numMember = numMember;
        this.items = new ArrayList<>();
        this.history = new ArrayList<>();
    }

    public SharedCart(String id, String name, Long total, int numItems, int numMember, String holderName, String holderAvatar, List<CartItem> items, List<String> history) {
        this.id = id;
        this.name = name;
        this.total = total;
        this.numItems = numItems;
        this.numMember = numMember;
        this.holderName = holderName;
        this.holderAvatar = holderAvatar;
        this.items = items;
        this.history = history;
    }

    public static SharedCart parseBasicJson(JSONObject response) {
        String id = response.optString("id");
        String name = response.optString("cartname");
        Long total = response.optLong("totalprice");
        int numItems = response.optInt("totalitems");
        int numMember = response.optInt("members");
        return new SharedCart(id, name, total, numItems, numMember);
    }

    public static SharedCart parseFullJson(JSONObject response) {
        // TODO: parse full info of shared cart
        return null;
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

    public int getNoMembers() { return numMember; }

    public Long getTotal() {
        return total;
    }

//    public void increaseQuantity(int position, ChangeNumItemsListener changeNumItemsListener) {
//        if (position == -1)
//            return;
//
//        if (items.size() > position) {
//            items.get(position).setQuantity(items.get(position).getQuantity() + 1);
//            total += items.get(position).getProduct().getPrice();
//        }
//
//        changeNumItemsListener.onChanged();
//    }
//
//    public void decreaseQuantity(int position, ChangeNumItemsListener changeNumItemsListener) {
//        if (position == -1)
//            return;
//
//        if (items.size() > position) {
//            if (items.get(position).getQuantity() > 1) {
//                items.get(position).setQuantity(items.get(position).getQuantity() - 1);
//                total -= items.get(position).getProduct().getPrice();
//            }
//        }
//
//        changeNumItemsListener.onChanged();
//    }

    public String getId() {
        return id;
    }

    public String getHolderName() {
        return holderName;
    }

    public String getHolderAvatar() {
        return holderAvatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShareCode() {
        return id;
    }
}
