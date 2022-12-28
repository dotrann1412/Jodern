package com.example.jodernstore.model;

import com.example.jodernstore.cart.cartitem.CartItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class Order {
    private final Long id;
    private final int type;   // 0: delivery, 1: appointment
    private final Integer numItems;
    private final Long totalPrice;
    private final LocalDate checkoutDate;
    private HashMap<String, String> customerInfor;
    private ArrayList<CartItem> items;
    private boolean status;
    private BranchInfo branchInfo;

    public Order(Long id, int type, Integer numItems, Long totalPrice, LocalDate checkoutDate, boolean status) {
        this.id = id;
        this.type = type;
        this.numItems = numItems;
        this.totalPrice = totalPrice;
        this.checkoutDate = checkoutDate;
        this.status = status;
    }

    public Order(Long id, int type, Integer numItems, Long totalPrice, LocalDate checkoutDate, HashMap<String, String> customerInfor, ArrayList<CartItem> items, boolean status) {
        this.id = id;
        this.type = type;
        this.numItems = numItems;
        this.totalPrice = totalPrice;
        this.checkoutDate = checkoutDate;
        this.customerInfor = customerInfor;
        this.items = items;
        this.status = status;
    }

    public Long getId() {
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

    public LocalDate getCheckoutDate() {
        return checkoutDate;
    }

    public boolean getStatus() {
        return status;
    }

    public HashMap<String, String> getCustomerInfor() {
        return customerInfor;
    }

    public ArrayList<CartItem> getItems() {
        return items;
    }

    public BranchInfo getBranchInfo() {
        return branchInfo;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setBranchInfo(BranchInfo branchInfo) {
        this.branchInfo = branchInfo;
    }

    public void setCustomerInfor(HashMap<String, String> customerInfor) {
        this.customerInfor = customerInfor;
    }

    public void setItems(ArrayList<CartItem> items) {
        this.items = items;
    }
}
