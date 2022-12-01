package com.example.jodern.cart.cartitem;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(primaryKeys = {"product_id", "size"})
public class CartItem {
    @NonNull
    @ColumnInfo(name = "product_id")
    private Long productId;

    @ColumnInfo(name = "quantity")
    private Integer quantity;

    @NonNull
    @ColumnInfo(name = "size")
    private String size;

    public CartItem() { }

    public CartItem(Long productId, Integer quantity, String size) {
        this.productId = productId;
        this.quantity = quantity;
        this.size = size;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
