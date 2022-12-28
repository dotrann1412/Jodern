package com.example.jodernstore.model;

import com.example.jodernstore.cart.cartitem.CartItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class AppointmentOrder extends Order {
    private final LocalDate appointmentDate;
    // customerInfor = {name, phone}
    // type: 1
    public AppointmentOrder(Long id, Integer numItems, Long totalPrice, LocalDate checkoutDate, boolean status, LocalDate appointmentDate, HashMap<String, String> customerInfor, ArrayList<CartItem> items, BranchInfo branchInfo) {
        super(id, 1, numItems, totalPrice, checkoutDate, customerInfor, items, status);
        super.setBranchInfo(branchInfo);
        this.appointmentDate = appointmentDate;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }
}
