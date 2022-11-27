package android.jodern.app.model;

public class Category {
    private final String name;
    private final int imageId;    // ID on res/drawable
    private final String raw;     // use for API calling

    public Category(String name, int imageId, String raw) {
        this.name = name;
        this.imageId = imageId;
        this.raw = raw;
    }

    public String getName() {
        return name;
    }

    public int getImageId() {
        return imageId;
    }

    public String getRaw() {
        return raw;
    }
}
