package android.jodern.app.controller;

import android.content.Context;
import android.jodern.app.model.Garment;
import android.jodern.app.model.OrderItem;

import java.util.List;

public class CartController {
    private Context context;
    private List<OrderItem> orderItemList;

    public CartController(Context context) {
        this.context = context;

    }

    public List<Garment> getCartList() {
        return null;
    }

    public double getSubTotal() {
        double result = 0f;

        for (OrderItem item : orderItemList) {
//            result += item.getCost();
        }


        return result;
    }

    public int getOrderListSize() {
        return orderItemList.size();
    }
}
