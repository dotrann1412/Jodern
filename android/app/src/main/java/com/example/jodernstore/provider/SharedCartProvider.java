package com.example.jodernstore.provider;

import com.example.jodernstore.model.SharedCart;

import java.util.ArrayList;
import java.util.List;

public class SharedCartProvider {
    private static SharedCartProvider instance = null;
    private List<SharedCart> sharedCartList;

    public static SharedCartProvider getInstance() {
        if (instance == null) {
            synchronized (SharedCartProvider.class) {
                if (instance == null) {
                    instance = new SharedCartProvider();
                }
            }
        }
        return instance;
    }

    private SharedCartProvider() { sharedCartList = new ArrayList<>(); }

    public List<SharedCart> getSharedCartList() { return sharedCartList; }

    public void setSharedCartList(List<SharedCart> sharedCartList) {
        this.sharedCartList = sharedCartList;
    }

    public SharedCart getSharedCartItem(int position) {
        if (position < 0 || position >= this.sharedCartList.size()) {
            return null;
        }

        return sharedCartList.get(position);
    }
}
