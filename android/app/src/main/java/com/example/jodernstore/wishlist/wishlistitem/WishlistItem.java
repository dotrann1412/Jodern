package com.example.jodern.wishlist.wishlistitem;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class WishlistItem {
    @PrimaryKey
    @ColumnInfo(name = "product_id")
    private Long productId;

    public WishlistItem() { }

    public WishlistItem(Long productId) {
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
