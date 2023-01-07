package com.example.jodernstore.provider;

import com.example.jodernstore.model.BranchInfo;

import java.util.ArrayList;

public class BranchesProvider {
    private static BranchesProvider instance = null;
    private ArrayList<BranchInfo> branches;

    public static BranchesProvider getInstance() {
        if (instance == null) {
            synchronized (BranchesProvider.class) {
                if (instance == null) {
                    instance = new BranchesProvider();
                }
            }
        }
        return instance;
    }

    private BranchesProvider() {
        branches = new ArrayList();
    }

    public ArrayList<BranchInfo> getBranches() {
        return branches;
    }

    public void setBranches(ArrayList<BranchInfo> branches) {
        this.branches = branches;
    }
}
