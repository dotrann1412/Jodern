package android.jodern.app.cart.cartitem;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class CartItem {
    @PrimaryKey
    @ColumnInfo(name = "product_id")
    private Long productId;

    @ColumnInfo(name = "quantity")
    private Integer quantity;

    @ColumnInfo(name = "size")
    private String size;

    public CartItem() { }

    public CartItem(Long productId, Integer quantity, String size) {
        this.productId = productId;
        this.quantity = quantity;
        this.size = size;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
