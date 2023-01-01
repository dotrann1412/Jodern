package com.example.jodernstore.model;

import org.json.JSONObject;

public class CartItem {
    private Product product;
    private int quantity;
    private String size;

    public CartItem(Product product, int quantity, String size) {
        this.product = product;
        this.quantity = quantity;
        this.size = size;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getSize() {
        return size;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public static CartItem parseJSON(JSONObject response) {
        try {
            Product product = Product.parseJSON(response.getJSONObject("product"));
            int quantity = response.getInt("quantity");
            String size = response.getString("size");
            return new CartItem(product, quantity, size);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
