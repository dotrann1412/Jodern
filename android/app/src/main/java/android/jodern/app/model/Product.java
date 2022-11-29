package android.jodern.app.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private static final String TAG = Product.class.getName();
    private static final String[] sizes = new String[]{"S", "M", "L", "XL", "XXL"};

    private Long id;
    private String name;
    private ArrayList<String> imageURLs;
    private Long price;
    private String description;
    private String category;
    private Integer[] inventories;

    public Product() {

    }

    public Product(Long id, String name, String imageURL, Long price) {
        this.id = id;
        this.name = name;
        this.imageURLs.add(imageURL);
        this.price = price;
    }

    public Product(Long id, String name, ArrayList<String> imageURLs, Long price, String description, String category, Integer[] inventories) {
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
            Long price = response.getLong("price");

            // images
            JSONArray imageJsonArr = response.getJSONArray("images");
            ArrayList<String> images = new ArrayList<>();
            for (int i = 0; i < imageJsonArr.length(); i++) {
                images.add(imageJsonArr.get(i).toString());
            }

            // inventory quantities
            JSONObject inventories = response.getJSONObject("inventory");
            Integer[] inventory = new Integer[5];
            for (int i = 0; i < sizes.length; i++) {
                inventory[i] = inventories.getInt(sizes[i]);
            }

            String category = response.getString("category");

            Log.d(TAG, "parseJSON: parse JSON object to Product object successfully");
            return new Product(id, name, images, price, description, category, inventory);
        }
        catch (Exception e) {
            Log.d(TAG, "parseJSON: failed to parse JSON object to Product object");
            return null;
        }
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
}
