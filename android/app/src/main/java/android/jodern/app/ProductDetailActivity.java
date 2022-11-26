package android.jodern.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.jodern.app.model.Product;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

public class ProductDetailActivity extends AppCompatActivity {
    private TextView detailName, detailPrice, detailPrice2, detailInventory, detailDescription;
    private ImageView detailImage;
    private Product product;

//    private LinearLayout detailSizeS, detailSizeM, detailSizeL, detailSizeXL, detailSizeXXL;
    private LinearLayout[] detailSizes = new LinearLayout[5];
    private LinearLayout currentSize;

    private MaterialButton detailAddToCartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        initViews();
        setEvents();

        // get intent data
        Intent intent = getIntent();
        String id = intent.getStringExtra("productId");
        // TODO: call API
    }

    private void initViews() {
        detailName = findViewById(R.id.detailName);
        detailPrice = findViewById(R.id.detailPrice);
        detailPrice2 = findViewById(R.id.detailPrice2);
        detailInventory = findViewById(R.id.detailInventory);
        detailDescription = findViewById(R.id.detailDescription);
        detailImage = findViewById(R.id.detailImage);

        int[] sizeIds = {R.id.detailSizeS, R.id.detailSizeM, R.id.detailSizeL, R.id.detailSizeXL, R.id.detailSizeXXL};
        for (int i = 0; i < 5; i++) {
            detailSizes[i] = findViewById(sizeIds[i]);
        }
        currentSize = detailSizes[0];
        updateCurrentSizeStyle(true);
    }

    private void setEvents() {
        for (int i = 0; i < 5; i++) {
            int finalI = i;
            detailSizes[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateCurrentSizeStyle(false);
//                    currentSize.setBackgroundResource(R.drawable.size_item);
//                    currentSize.findViewWithTag("sizeText").setSelected(false);
                    currentSize = detailSizes[finalI];
                    updateCurrentSizeStyle(true);
//                    currentSize.setBackgroundResource(R.drawable.size_item_selected);
                    // TODO: update inventory (get from attribute "product")
                }
            });
        }
    }

    private void updateCurrentSizeStyle(boolean isSelected) {
        if (isSelected) {
            currentSize.setBackgroundResource(R.drawable.size_item_selected);
            ((TextView)(currentSize.findViewWithTag("sizeText"))).setTextColor(getColor(R.color.white));
        }
        else {
            currentSize.setBackgroundResource(R.drawable.size_item);
            ((TextView)(currentSize.findViewWithTag("sizeText"))).setTextColor(getColor(R.color.text));
        }
    }

    public void onDetailBackBtnClicked(View view) {
        onBackPressed();
        finish();
    }

    public void onDetailCartBtnClicked(View view) {
        // TODO: Go to cart activity
    }
}