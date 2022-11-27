package android.jodern.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.jodern.app.model.Product;
import android.jodern.app.provider.Provider;
import android.jodern.app.util.Utils;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProductDetailActivity extends AppCompatActivity {
    private TextView detailName, detailPrice, detailPrice2, detailInventory, detailDescription;
    private ImageView detailImage;
    private Product currentProduct;
    private final LinearLayout[] detailSizes = new LinearLayout[5];
    private static final String[] sizes = new String[]{"S", "M", "L", "XL", "XXL"};
    private LinearLayout currentSizeWrapper;
    private String currentSize;
    private LinearLayout loadingWrapper;

    private MaterialButton detailAddToCartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        initViews();
        setEvents();

        // get intent data
        Intent intent = getIntent();
        String params = parseSearchParams(intent);
        // TODO: hide API KEY
        String url = "http://joderm.store:8000/api/" + params;
        // start loading effect
        loadingWrapper.setVisibility(View.VISIBLE);
        JsonObjectRequest stringRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response);
                        loadingWrapper.setVisibility(View.GONE);
                        parseProductDetailResponse(response);
                        showProductDetail();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("Error");
                        loadingWrapper.setVisibility(View.GONE);
                        onDetailBackBtnClicked(null);
                        Toast.makeText(ProductDetailActivity.this, "Đã có lỗi xảy ra. Bạn vui lòng thử lại sau nhé", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        Provider.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void showProductDetail() {
        String priceFormatted = Utils.vndFormatPrice(currentProduct.getPrice());

        detailName.setText(currentProduct.getName());
        detailPrice.setText(priceFormatted);
        detailPrice2.setText(priceFormatted);
        detailDescription.setText(currentProduct.getDescription());
        detailInventory.setText(String.valueOf(currentProduct.getInventory(0)));

        // TODO: update after add image select
        Glide.with(this)
                .load(currentProduct.getImageURL())
                .diskCacheStrategy(DiskCacheStrategy.ALL) // It will cache your image after loaded for first time
                .override(detailImage.getWidth(),detailImage.getHeight()) // Overrides size of downloaded image and converts it's bitmaps to your desired image size;
                .into(detailImage);
    }

    private void parseProductDetailResponse(JSONObject response) {
        try {
            int id = response.getInt("id");
            String name = response.getString("title");
            String description = response.getString("description");

            // images
            JSONArray imageURLs = response.getJSONArray("images");
            // TODO: image select feature, currently just use the first image
            String imageURL = imageURLs.get(0).toString();

            // inventory quantities
            int price = response.getInt("price");
            JSONObject inventories = response.getJSONObject("inventory");
            int[] inventory = new int[5];
            for (int i = 0; i < sizes.length; i++) {
                inventory[i] = inventories.getInt(sizes[i]);
            }

            String category = response.getString("category");

            // TODO: constructor with imageURLs
            currentProduct = new Product(id, name, imageURL, price, description, category, inventory);
        }
        catch (Exception e) {
            System.out.println("[ERROR]");
            e.printStackTrace();
        }
    }

    private String parseSearchParams(Intent intent) {
        int id = intent.getIntExtra("productId", 0);
        String params = "product/" + Integer.toString(id);
        return params;
    }

    private void initViews() {
        detailName = findViewById(R.id.detailName);
        detailPrice = findViewById(R.id.detailPrice);
        detailPrice2 = findViewById(R.id.detailPrice2);
        detailInventory = findViewById(R.id.detailInventory);
        detailDescription = findViewById(R.id.detailDescription);
        detailImage = findViewById(R.id.detailImage);
        loadingWrapper = findViewById(R.id.detailLoadingWrapper);

        int[] sizeIds = {R.id.detailSizeS, R.id.detailSizeM, R.id.detailSizeL, R.id.detailSizeXL, R.id.detailSizeXXL};
        for (int i = 0; i < 5; i++) {
            detailSizes[i] = findViewById(sizeIds[i]);
        }
        currentSizeWrapper = detailSizes[0];
        currentSize = sizes[0];
        updateCurrentSizeView(true);
    }

    private void setEvents() {
        for (int i = 0; i < 5; i++) {
            int finalI = i;
            detailSizes[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateCurrentSizeView(false);
                    currentSizeWrapper = detailSizes[finalI];
                    currentSize = sizes[finalI];
                    updateCurrentSizeView(true);
                    detailInventory.setText(String.valueOf(currentProduct.getInventory(finalI)));
                }
            });
        }
    }

    private void updateCurrentSizeView(boolean isSelected) {
        TextView sizeText = currentSizeWrapper.findViewWithTag("sizeText");
        if (isSelected) {
            currentSizeWrapper.setBackgroundResource(R.drawable.size_item_selected);
            sizeText.setTextColor(getColor(R.color.white));
            sizeText.setText(currentSize);
        } else {
            currentSizeWrapper.setBackgroundResource(R.drawable.size_item);
            sizeText.setTextColor(getColor(R.color.text));
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