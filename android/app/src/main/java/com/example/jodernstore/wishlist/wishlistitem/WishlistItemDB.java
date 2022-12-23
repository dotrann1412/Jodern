package com.example.jodernstore.wishlist.wishlistitem;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {WishlistItem.class}, version = 1)
public abstract class WishlistItemDB extends RoomDatabase {
    private static final String WISHLIST_ITEM_DB = "wishlist_item.db";

    public abstract WishlistItemDao wishlistItemDao();

    private static com.example.jodernstore.wishlist.wishlistitem.WishlistItemDB wishlistItemDB;

    public static com.example.jodernstore.wishlist.wishlistitem.WishlistItemDB with(Context context) {
        if (wishlistItemDB == null) {
            wishlistItemDB = Room.databaseBuilder(context.getApplicationContext(), com.example.jodernstore.wishlist.wishlistitem.WishlistItemDB.class, WISHLIST_ITEM_DB).allowMainThreadQueries().build();
        }
        return wishlistItemDB;
    }
}
