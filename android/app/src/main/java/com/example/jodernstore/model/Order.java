package com.example.jodernstore.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Order {
    private HashMap<String, String> customerInfo;
    private String id;
    private int type;   // 0: delivery, 1: appointment
    private Integer numItems;
    private Long totalPrice;
    private String checkoutDate;
    private boolean status;
    private BranchInfo branchInfo;
    private ArrayList<CartItem> cartItems;

    public Order(String id, int type, Integer numItems, Long totalPrice, String checkoutDate, boolean status) {
        this.id = id;
        this.type = type;
        this.numItems = numItems;
        this.totalPrice = totalPrice;
        this.checkoutDate = checkoutDate;
        this.status = status;
        this.cartItems = new ArrayList<>();
        this.customerInfo = new HashMap<>();
    }

    public Order(String id, int type, Integer numItems, Long totalPrice, String checkoutDate, boolean status, HashMap<String, String> customerInfo, ArrayList<CartItem> items) {
        this.id = id;
        this.type = type;
        this.numItems = numItems;
        this.totalPrice = totalPrice;
        this.checkoutDate = checkoutDate;
        this.status = status;
        this.customerInfo = customerInfo;
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

    public static Order parseBasicJSON(JSONObject response, String id) {
        try {
            int type = response.getInt("ordertype");
            int numItems = 10;
            if (response.has("totalitems")) {
                numItems = response.getInt("totalitems");
            }
            Long totalPrice = response.getLong("totalprice");
            String checkoutDate = response.getString("createdat");
            boolean status = response.getBoolean("deliverstatus");
            return new Order(id, type, numItems, totalPrice, checkoutDate, status);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Order parseFullJSON(JSONObject response, String id) {
        try {
            System.out.println(response.toString());
            Order order = parseBasicJSON(response, id);
            if (order == null) {
                throw new Exception("Parsed order is null");
            }

            int numItems = 0;
            JSONArray items = (JSONArray)response.get("details");
            for (int j = 0; j < items.length(); j++) {
                CartItem item = CartItem.parseJSON((JSONObject)items.get(j));
                if (item != null) {
                    order.cartItems.add(item);
                    numItems += item.getQuantity();
                }
            }
            order.numItems = numItems;

            HashMap<String, String> infor = new HashMap<>();
            JSONObject inforJson = null;
            if (response.has("infor"))
                inforJson = response.getJSONArray("infor").getJSONObject(0);
            else
                inforJson = response.getJSONObject("customer");
            infor.put("name", inforJson.getString("customer_name"));
            infor.put("phone", inforJson.getString("phone_number"));
            infor.put("email", inforJson.getString("email"));
            if (order.type == 0) {
                infor.put("address", inforJson.getString("location"));
            } else {
                infor.put("appointmentDate", response.getString("date"));
            }

            BranchInfo branch = null;
            if (response.has("branch")) {
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

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setBranchInfo(BranchInfo branchInfo) {
        this.branchInfo = branchInfo;
    }

    public void setCustomerInfo(HashMap<String, String> customerInfo) {
        this.customerInfo = customerInfo;
    }

    public void setItems(ArrayList<CartItem> items) {
        this.cartItems = items;
    }

    public BranchInfo getBranchInfo() {
        return branchInfo;
    }
}
