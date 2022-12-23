package com.example.jodern.wishlist.wishlistitem;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WishlistItemDao {
    @Insert
    public void insert(WishlistItem... wishlistItems);

    @Delete
    public void delete(WishlistItem wishlistItem);

    @Query("SELECT * FROM wishlistitem")
    public List<WishlistItem> loadAll();
}
