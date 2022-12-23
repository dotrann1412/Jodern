package com.example.jodernstore.model;

public class Category {
    private final String name;
    private final int imageId;    // ID on res/drawable
    private final String raw;     // use for API calling
    private final boolean isAll;  // use to specify the case show all products of 'nam' or 'nu'

    public Category(String name, int imageId, String raw) {
        this.name = name;
        this.imageId = imageId;
        this.raw = raw;
        this.isAll = false;
    }

    public Category(String name, int imageId, String raw, boolean isAll) {
        this.name = name;
        this.imageId = imageId;
        this.raw = raw;
        this.isAll = isAll;
    }

    public String getName() {
        return name;
    }

    public boolean isAll() {
        return isAll;
    }

    public int getImageId() {
        return imageId;
    }

    public String getRaw() {
        return raw;
    }
}
