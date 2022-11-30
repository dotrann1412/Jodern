package com.example.jodern.cart.cartitem;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {CartItem.class}, version = 1)
public abstract class CartItemDB extends RoomDatabase {
    private static final String CART_ITEM_DB = "cart_item.db";

    public abstract CartItemDao orderItemDao();

    private static CartItemDB cartItemDB;

    public static CartItemDB with(Context context) {
        if (cartItemDB == null) {
            cartItemDB = Room.databaseBuilder(context.getApplicationContext(), CartItemDB.class, CART_ITEM_DB).allowMainThreadQueries().build();
        }
        return cartItemDB;
    }
}
