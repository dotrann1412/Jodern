package android.jodern.app.cart.cartitem;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {CartItem.class}, version = 1)
public abstract class CartItemDB extends RoomDatabase {
    private static final String ORDER_ITEM_DB = "order_item.db";

    public abstract CartItemDao orderItemDao();

    private static CartItemDB cartItemDB;

    public static CartItemDB getInstance(Context context) {
        if (cartItemDB == null) {
            cartItemDB = Room.databaseBuilder(context.getApplicationContext(), CartItemDB.class, ORDER_ITEM_DB).allowMainThreadQueries().build();
        }
        return cartItemDB;
    }
}
