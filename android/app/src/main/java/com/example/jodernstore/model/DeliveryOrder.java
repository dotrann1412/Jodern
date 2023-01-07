package com.example.jodernstore.model;

import java.util.ArrayList;
import java.util.HashMap;

public class DeliveryOrder extends Order {
    // customerInfor = {name, phone, address}
    // type: 0
    public DeliveryOrder(String id, Integer numItems, Long totalPrice, String checkoutDate, boolean status, HashMap<String, String> customerInfor, ArrayList<CartItem> items) {
        super(id, 0, numItems, totalPrice, checkoutDate, status, customerInfor, items);
    }
}
