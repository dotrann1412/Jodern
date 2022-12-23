package com.example.jodernstore.cart.cartitem;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CartItemDao {
    @Insert
    public void insert(CartItem... cartItems);

    @Update
    public void update(CartItem... cartItems);

    @Delete
    public void delete(CartItem cartItem);

    @Query("SELECT * FROM cartitem")
    public List<CartItem> loadAll();

    @Query("UPDATE cartitem SET quantity = quantity + 1 WHERE cartitem.product_id = :productId")
    public void increaseQuantity(Long productId);

    @Query("UPDATE cartitem SET quantity = quantity - 1 WHERE cartitem.product_id = :productId")
    public void decreaseQuantity(Long productId);
}
