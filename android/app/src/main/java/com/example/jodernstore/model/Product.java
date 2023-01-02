package com.example.jodernstore.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class Product {
    private static final String TAG = Product.class.getName();
    private static final String[] sizes = new String[]{"S", "M", "L", "XL", "XXL"};

    private Long id;
    private String name;
    private ArrayList<String> imageURLs;
    private Long price;
    private String description;
    private String category;
    private String categoryName;
    private Integer[] inventories;

    public Product() {

    }

    public Product(Long id, String name, String imageURL, Long price) {
        this.id = id;
        this.name = name;
        this.imageURLs.add(imageURL);
        this.price = price;
    }

    public Product(Long id, String name, ArrayList<String> imageURLs, Long price, String description, String category, String categoryName, Integer[] inventories) {
        this.id = id;
        this.name = name;
        this.imageURLs = imageURLs;
        this.price = price;
        this.description = description;
        this.category = category;
        this.categoryName = categoryName;
        this.inventories = inventories;
    }

    public static Product parseJSON(JSONObject response) {
        try {
            Long id = response.getLong("id");

            String name = response.getString("title");
            String description = response.getString("description");
            Long price = response.getLong("price");

            // images
            JSONArray imageJsonArr = response.getJSONArray("images");
            ArrayList<String> images = new ArrayList<>();
            for (int i = 0; i < imageJsonArr.length(); i++) {
                images.add(imageJsonArr.get(i).toString());
            }

            // inventory quantities
            Integer[] inventory = new Integer[sizes.length];

            if (response.has("inventory")) {
                JSONObject inventories = response.getJSONObject("inventory");
                for (int i = 0; i < sizes.length; i++) {
                    inventory[i] = inventories.getInt(sizes[i]);
                }
            } else {
                for (int i = 0; i < sizes.length; ++i)
                    inventory[i] = 0;
            }

            String category = response.getString("category");
            String categoryName = "unknown";
            if (response.has("category_name"))
                categoryName = response.getString("category_name");

            Log.d(TAG, "parseJSON: parse JSON object to Product object successfully");
            return new Product(id, name, images, price, description, category, categoryName, inventory);
        }
        catch (Exception e) {
            Log.d(TAG, "parseJSON: failed to parse JSON object to Product object " + e.toString());
            return null;
        }
    }

    public static ArrayList<Product> parseProductListFromResponse(JSONObject response) {
        ArrayList<Product> productList = new ArrayList<>();

        try {
            JSONArray keys = response.names();
            for (int i = 0; i < Objects.requireNonNull(keys).length(); i++) {
                String key = keys.getString(i);
                JSONArray products = (JSONArray)response.get(key);
                for (int j = 0; j < products.length(); j++) {
                    productList.add(Product.parseJSON((JSONObject)products.get(j)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return productList;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getFirstImageURL() {
        return imageURLs.get(0);
    }

    public ArrayList<String> getImages() {
        return imageURLs;
    }

    public String getDescription() {
        return description;
    }

    public Long getPrice() {
        return price;
    }

    public Integer[] getInventories() {
        return inventories;
    }

    public Integer getInventory(int i) {
        return inventories[i];
    }

    public Integer getInventory(String size) {
        for (int i = 0; i < sizes.length; i++) {
            if (sizes[i].equals(size)) {
                return inventories[i];
            }
        }
        return null;
    }
}
