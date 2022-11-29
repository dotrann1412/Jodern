package android.jodern.app.model;

public class Product {
    private int id;
    private String name;
    private String imageURL;
    private String[] imageURLs;
    private int price;
    private String description;
    private String category;
    private int[] inventory;

    public Product(int id, String name, String imageURL, int price) {
        this.id = id;
        this.name = name;
        this.imageURL = imageURL;
        this.price = price;
    }

    public Product(int id, String name, String imageURL, int price, String description, String category, int[] inventory) {
        this.id = id;
        this.name = name;
        this.imageURL = imageURL;
        this.price = price;
        this.description = description;
        this.category = category;
        this.inventory = inventory;
    }

    public Product(int id, String name, String[] imageURLs, int price, String description, String category, int[] inventory) {
        this.id = id;
        this.name = name;
        this.imageURLs = imageURLs;
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

    public String getImageURL() {
        return imageURL;
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
