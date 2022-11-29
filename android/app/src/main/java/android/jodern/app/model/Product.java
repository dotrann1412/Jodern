package android.jodern.app.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//public class Product {
//    private Long id;
//    private String name;
//    private String sex;
//    private String category;
//    private String color;
//    private String description;
//    private Long price;
//    private List<String> images;
//    private List<Integer> inventories;
//
//    public Product() {}
//
//    public Product(Long id, String name, String sex, String category, String color, String description, Long price, List<String> images, List<Integer> inventories) {
//        this.id = id;
//        this.name = name;
//        this.sex = sex;
//        this.category = category;
//        this.color = color;
//        this.description = description;
//        this.price = price;
//        this.images = images;
//        this.inventories = inventories;
//    }
//
//    public Product(Long id, List<Integer> inventories) {
//        this.id = id;
//        this.inventories = inventories;
public class Product {
    private static final String TAG = Product.class.getName();
    private static final String[] sizes = new String[]{"S", "M", "L", "XL", "XXL"};

    private Long id;
    private String name;
    private String imageURL;
    private String[] imageURLs;
    private Long price;
    private String description;
    private String category;
    private Integer[] inventories;

    public Product() {

    }

    public Product(Long id, String name, String imageURL, Long price) {
        this.id = id;
        this.name = name;
        this.imageURL = imageURL;
        this.price = price;
    }

    public Product(Long id, String name, String imageURL, Long price, String description, String category, Integer[] inventories) {
        this.id = id;
        this.name = name;
        this.imageURL = imageURL;
        this.price = price;
        this.description = description;
        this.category = category;
        this.inventories = inventories;
    }

    public Product(Long id, String name, String[] imageURLs, Long price, String description, String category, Integer[] inventories) {
        this.id = id;
        this.name = name;
        this.imageURLs = imageURLs;
        this.price = price;
        this.description = description;
        this.category = category;
        this.inventories = inventories;
    }

    public static Product parseJSON(JSONObject response) {
        try {
            Long id = response.getLong("id");
            String name = response.getString("title");
            String description = response.getString("description");

            // images
            JSONArray imageURLs = response.getJSONArray("images");
            String imageURL = imageURLs.get(0).toString();

            // inventory quantities
            Long price = response.getLong("price");
            JSONObject inventories = response.getJSONObject("inventory");
            Integer[] inventory = new Integer[5];
            for (int i = 0; i < sizes.length; i++) {
                inventory[i] = inventories.getInt(sizes[i]);
            }

            String category = response.getString("category");

            Log.d(TAG, "parseJSON: parse JSON object to Product object successfully");
            return new Product(id, name, imageURL, price, description, category, inventory);
        } catch (Exception e) {
            Log.d(TAG, "parseJSON: failed to parse JSON object to Product object");
            return null;
        }
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String[] getImages() {
        return imageURLs;
    }

    public void setImages(String[] imageURLs) {
        this.imageURLs = imageURLs;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public Integer[] getInventories() {
        return inventories;
    }

    public void setInventories(Integer[] inventories) {
        this.inventories = inventories;
    }

    public Integer getInventory(int i) {
        return inventories[i];
    }
}
