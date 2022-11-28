package android.jodern.app.model;

import java.util.List;

public class Product {
    private Long id;
    private String name;
    private String sex;
    private String category;
    private String color;
    private String description;
    private Long price;
    private List<String> images;
    private List<Integer> inventories;

    public Product() {}

    public Product(Long id, String name, String sex, String category, String color, String description, Long price, List<String> images, List<Integer> inventories) {
        this.id = id;
        this.name = name;
        this.sex = sex;
        this.category = category;
        this.color = color;
        this.description = description;
        this.price = price;
        this.images = images;
        this.inventories = inventories;
    }

    public Product(Long id, List<Integer> inventories) {
        this.id = id;
        this.inventories = inventories;
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

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<Integer> getInventories() {
        return inventories;
    }

    public void setInventories(List<Integer> inventories) {
        this.inventories = inventories;
    }
}
