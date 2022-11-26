package android.jodern.app.controller;

import android.content.Context;
import android.jodern.app.model.OrderItem;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CartController {
    private Context context;
    private List<OrderItem> orderItemList;

    public CartController(Context context) {
        this.context = context;

        // TODO get order item list from storage instead of this dummy data

        Long i = new Long(0);
        OrderItem orderItem = new OrderItem();
        orderItem.setQuantity(150);
        orderItem.setProductId(i++);
        List<OrderItem> orderItemList = new ArrayList<>();
        orderItemList.add(orderItem);
        orderItemList.add(orderItem);
        orderItemList.add(orderItem);
        orderItemList.add(orderItem);
        orderItemList.add(orderItem);
        this.orderItemList = orderItemList;
    }

    public List<OrderItem> getCartList() {
        return orderItemList;
    }

    public long getSubTotal() {
        try {
            long result = 0l;

            for (OrderItem item : orderItemList) {
    //            result += item.getCost();
            }

            return result;
        } catch (Exception e) {
            Log.d("Cart Controller", e.getMessage());
            return -1l;
        }
    }

    public int getOrderListSize() {
        try {
            return orderItemList.size();
        } catch (Exception e) {
            Log.d("Cart Controller", e.getMessage());
            return -1;
        }
    }
}
