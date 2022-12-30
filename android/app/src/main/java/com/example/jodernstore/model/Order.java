package com.example.jodernstore.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class Order {
    private Long id;
    private int type;   // 0: delivery, 1: appointment
    private Integer numItems;
    private Long totalPrice;
    private LocalDate checkoutDate;
    private HashMap<String, String> customerInfor;
    private ArrayList<CartItem> items;
    private boolean status;

    public Order(Long id, int type, Integer numItems, Long totalPrice, LocalDate checkoutDate, boolean status) {
        this.id = id;
        this.type = type;
        this.numItems = numItems;
        this.totalPrice = totalPrice;
        this.checkoutDate = checkoutDate;
        this.status = status;
    }

    public Order(Long id, int type, Integer numItems, Long totalPrice, LocalDate checkoutDate, boolean status, HashMap<String, String> customerInfor, ArrayList<CartItem> items) {
        this.id = id;
        this.type = type;
        this.numItems = numItems;
        this.totalPrice = totalPrice;
        this.checkoutDate = checkoutDate;
        this.status = status;
        this.customerInfor = customerInfor;
        this.items = items;
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

    public void setCustomerInfor(HashMap<String, String> customerInfor) {
        this.customerInfor = customerInfor;
    }

    public void setItems(ArrayList<CartItem> items) {
        this.items = items;
    }
}
