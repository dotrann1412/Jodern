package android.jodern.app.model;

import java.util.ArrayList;

public class Product {
    private final int id;
    private final String name;
    private ArrayList<String> images = new ArrayList<>();
    private final int price;
    private String description;
    private String category;
    private int[] inventory;

    public Product(int id, String name, String imageURL, int price) {
        this.id = id;
        this.name = name;
        this.images.add(imageURL);
        this.price = price;
    }

    public Product(int id, String name, ArrayList<String> imageURLs, int price, String description, String category, int[] inventory) {
        this.id = id;
        this.name = name;
        this.images = imageURLs;
        this.price = price;
        this.description = description;
        this.category = category;
        this.inventory = inventory;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFirstImageURL() {
        return images.get(0);
    }

    public int getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public int getInventory(int i) {
        return inventory[i];
    }
}
