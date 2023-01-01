package com.example.jodernstore.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Order {
    private String id;
    private int type;   // 0: delivery, 1: appointment
    private Integer numItems;
    private Long totalPrice;
    private String checkoutDate;
    private boolean status;
    private ArrayList<CartItem> cartItems;
    private HashMap<String, String> customerInfo;
    private BranchInfo branchInfo;

    public Order(String id, int type, Integer numItems, Long totalPrice, String checkoutDate, boolean status) {
        this.id = id;
        this.type = type;
        this.numItems = numItems;
        this.totalPrice = totalPrice;
        this.checkoutDate = checkoutDate;
        this.status = status;
    }

    public Order(String id, int type, Integer numItems, Long totalPrice, String checkoutDate, boolean status, HashMap<String, String> customerInfor, ArrayList<CartItem> items) {
        this.id = id;
        this.type = type;
        this.numItems = numItems;
        this.totalPrice = totalPrice;
        this.checkoutDate = checkoutDate;
        this.status = status;
        this.customerInfo = customerInfor;
        this.cartItems = items;
    }

    public static Order parseBasicJSON(JSONObject response) {
        try {
            String id = response.getString("orderid");
            int type = response.getInt("ordertype");
            Integer numItems = response.getInt("totalitems");
            Long totalPrice = response.getLong("totalprice");
            String checkoutDate = response.getString("createdat");
            boolean status = response.getBoolean("deliverstatus");
            return new Order(id, type, numItems, totalPrice, checkoutDate, status);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Order parseFullJSON(JSONObject response) {
        try {
            Order order = parseBasicJSON(response);

            JSONArray items = (JSONArray)response.get("details");
            for (int j = 0; j < items.length(); j++) {
                CartItem item = CartItem.parseJSON((JSONObject)items.get(j));
                if (item != null) {
                    order.cartItems.add(item);
                }
            }

            HashMap<String, String> infor = new HashMap<>();
            JSONObject inforJson = (JSONObject)response.get("order-info");
            infor.put("name", inforJson.getString("customer_name"));
            infor.put("phone", inforJson.getString("phone_number"));
            infor.put("email", inforJson.getString("email"));
            if (order.type == 0) {
                infor.put("address", inforJson.getString("location"));
            } else {
                infor.put("appointmentDate", inforJson.getString("date"));
            }

            BranchInfo branch = null;
            if (response.getJSONObject("branch") != null) {
                branch = BranchInfo.parseJSON((JSONObject)response.get("branch"));
            }

            order.customerInfo = infor;
            order.branchInfo = branch;

            return order;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public Integer getNumItems() {
        return numItems;
    }

    public Long getTotalPrice() {
        return totalPrice;
    }

    public String getCheckoutDate() {
        return checkoutDate;
    }

    public boolean getStatus() {
        return status;
    }

    public HashMap<String, String> getCustomerInfo() {
        return customerInfo;
    }

    public ArrayList<CartItem> getItems() {
        return cartItems;
    }

    public void setCustomerInfo(HashMap<String, String> customerInfo) {
        this.customerInfo = customerInfo;
    }

    public void setItems(ArrayList<CartItem> items) {
        this.cartItems = items;
    }
}
