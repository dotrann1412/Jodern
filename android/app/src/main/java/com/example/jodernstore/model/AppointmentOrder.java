package com.example.jodernstore.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class AppointmentOrder extends Order {
    private final LocalDate appointmentDate;
    // customerInfor = {name, phone}
    // type: 1
    public AppointmentOrder(String id, Integer numItems, Long totalPrice, String checkoutDate, boolean status, LocalDate appointmentDate, HashMap<String, String> customerInfor, ArrayList<CartItem> items, BranchInfo branchInfo) {
        super(id, 1, numItems, totalPrice, checkoutDate, status, customerInfor, items);
        super.setBranchInfo(branchInfo);
        this.appointmentDate = appointmentDate;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }
}
