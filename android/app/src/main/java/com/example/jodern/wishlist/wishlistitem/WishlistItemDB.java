package com.example.jodern.wishlist.wishlistitem;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.jodern.wishlist.wishlistitem.WishlistItem;
import com.example.jodern.wishlist.wishlistitem.WishlistItemDao;

@Database(entities = {WishlistItem.class}, version = 1)
public abstract class WishlistItemDB extends RoomDatabase {
    private static final String WISHLIST_ITEM_DB = "wishlist_item.db";

    public abstract WishlistItemDao wishlistItemDao();

    private static com.example.jodern.wishlist.wishlistitem.WishlistItemDB wishlistItemDB;

    public static com.example.jodern.wishlist.wishlistitem.WishlistItemDB with(Context context) {
        if (wishlistItemDB == null) {
            wishlistItemDB = Room.databaseBuilder(context.getApplicationContext(), com.example.jodern.wishlist.wishlistitem.WishlistItemDB.class, WISHLIST_ITEM_DB).allowMainThreadQueries().build();
        }
        return wishlistItemDB;
    }
}
