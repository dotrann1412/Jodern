package com.example.jodern.activity;

import static com.example.jodern.Utils.vndFormatPrice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;

import com.example.jodern.MainActivity;
import com.example.jodern.R;
import com.example.jodern.adapter.ProductListAdapter;
import com.example.jodern.adapter.TrendingAdapter;
import com.example.jodern.customwidget.MyToast;
//import com.example.jodern.activity.CartActivity;
import com.example.jodern.model.Product;
import com.example.jodern.provider.Provider;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class ProductDetailActivity extends AppCompatActivity {
    private Product currentProduct;

    private TextView detailName, detailPrice, detailInventoryQuantity, detailDescription;
    private ImageView detailImage;

    private int currentQuantity = 1;
    private TextView buyQuantityText;

    private final LinearLayout[] detailSizes = new LinearLayout[5];
    private static final String[] sizes = new String[]{"S", "M", "L", "XL", "XXL"};
    private LinearLayout currentSizeWrapper;
    private String currentSize;

    private LinearLayout loadingWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        initViews();
        setEvents();
        handleAPICalls();
    }

    private void handleAPICalls() {
        // start loading effect
        loadingWrapper.setVisibility(View.VISIBLE);
        Intent intent = getIntent();
        getMainProduct(intent);
    }

    private void getMainProduct(Intent intent) {
        String params = parseSearchParams(intent);
        // TODO: hide API KEY
        String url = "http://jodern.store:8000/api/" + params;
        JsonObjectRequest stringRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseProductFromResponse(response);
                        setupMainProduct();
                        // TODO: other products in the same category
                        getOtherProducts(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingWrapper.setVisibility(View.GONE);
                        MyToast.makeText(ProductDetailActivity.this, getString(R.string.error_message), Toast.LENGTH_SHORT);
                    }
                }
        );
        Provider.with(this).addToRequestQueue(stringRequest);
    }

    private void getOtherProducts(Intent intent) {
        String categoryRaw = currentProduct.getCategory();
        String id = String.valueOf(currentProduct.getId());
//        String params = "products-top-k?category=" + categoryRaw + "&id=" + id;
//        String url = "http://jodern.store:8000/api/" + params;
        // demo
        String url = "http://jodern.store:8000/api/product-list?category=ao-khoac-nam";
        JsonObjectRequest stringRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ArrayList<Product> otherProducts = parseOtherProductsFromResponse(response);
                        setupOtherProducts(otherProducts);
                        loadingWrapper.setVisibility(View.GONE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingWrapper.setVisibility(View.GONE);
                        MyToast.makeText(ProductDetailActivity.this, getString(R.string.error_message), Toast.LENGTH_SHORT);
                    }
                }
        );
        Provider.with(this).addToRequestQueue(stringRequest);

    }

    private ArrayList<Product> parseOtherProductsFromResponse(JSONObject response) {
        ArrayList<Product> productList = new ArrayList<>();

        try {
            JSONArray keys = response.names();
            for (int i = 0; i < Objects.requireNonNull(keys).length(); i++) {
                String key = keys.getString(i);
                JSONArray products = (JSONArray)response.get(key);
                for (int j = 0; j < products.length(); j++) {
                    Product newProduct = Product.parseJSON(products.getJSONObject(j));
                    productList.add(newProduct);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return productList;
    }

    private void parseProductFromResponse(JSONObject response) {
        try {
            currentProduct = Product.parseJSON(response);

            Long id = response.getLong("id");
            String name = response.getString("title");
            String description = response.getString("description");

            // images
            // TODO: image select feature, currently just use the first image
            JSONArray imageURLs = response.getJSONArray("images");
            ArrayList<String> images = new ArrayList<>();
            for (int i = 0; i < imageURLs.length(); i++) {
                images.add(imageURLs.get(i).toString());
            }

            // inventory quantities
            Long price = response.getLong("price");
            JSONObject inventories = response.getJSONObject("inventory");
            Integer[] inventory = new Integer[5];
            for (int i = 0; i < sizes.length; i++) {
                inventory[i] = inventories.getInt(sizes[i]);
            }

            String category = response.getString("category");
            currentProduct = new Product(id, name, images, price, description, category, inventory);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupMainProduct() {
        String priceFormatted = vndFormatPrice(currentProduct.getPrice());

        detailName.setText(currentProduct.getName());
        detailPrice.setText(priceFormatted);
        detailDescription.setText(currentProduct.getDescription());
        detailInventoryQuantity.setText(String.valueOf(currentProduct.getInventory(0)));

        currentQuantity = 1;
        buyQuantityText.setText("1" );

        // TODO: update after add image select
        Glide.with(this)
                .load(currentProduct.getFirstImageURL())
                .diskCacheStrategy(DiskCacheStrategy.ALL) // It will cache your image after loaded for first time
                .override(detailImage.getWidth(),detailImage.getHeight()) // Overrides size of downloaded image and converts it's bitmaps to your desired image size;
                .into(detailImage);
    }

    private void setupOtherProducts(ArrayList<Product> otherProducts) {
        LinearLayout otherProductsWrapper = findViewById(R.id.detailOtherProductsWrapper);
        if (otherProducts.size() == 0) {
            otherProductsWrapper.setVisibility(View.GONE);
            return;
        }

        RecyclerView otherProductsView = findViewById(R.id.detailOtherProductsView);
        LinearLayoutManager layout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        otherProductsView.setLayoutManager(layout);

        TrendingAdapter adapter = new TrendingAdapter(this);
        adapter.setProductList(otherProducts);
        otherProductsView.setAdapter(adapter);
    }

    private String parseSearchParams(Intent intent) {
        Long id = intent.getLongExtra("productId", 0);
        return "product/" + id.toString();
    }

    private void initViews() {
        detailImage = findViewById(R.id.detailImage);
        detailName = findViewById(R.id.detailName);
        detailPrice = findViewById(R.id.detailPrice);

        detailInventoryQuantity = findViewById(R.id.detailInventory);

        buyQuantityText = findViewById(R.id.detailQuantity);

        detailDescription = findViewById(R.id.detailDescription);

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
                    detailInventoryQuantity.setText(String.valueOf(currentProduct.getInventory(finalI)));
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

    public void onDetailIncBtnClicked(View view) {
        currentQuantity++;
        buyQuantityText.setText(String.valueOf(currentQuantity));
    }

    public void onDetailDecBtnClicked(View view) {
        if (currentQuantity == 1)
            return;
        currentQuantity--;
        buyQuantityText.setText(String.valueOf(currentQuantity));
    }

    public void onDetailBackBtnClicked(View view) {
        onBackPressed();
        finish();
    }

    public void onDetailAddToCartBtnClicked(View view) {
        Long productId = currentProduct.getId();
        Integer quantity = currentQuantity;
        String size = currentSize;
        int inventory = currentProduct.getInventory(currentSize);
        if (quantity > inventory) {
            Toast.makeText(this, "Số lượng sản phẩm không đủ", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: decrease inventory number of product

        Intent intent = new Intent(ProductDetailActivity.this, MainActivity.class);
        intent.putExtra("nextFragment", "cart");
        intent.putExtra("productId", productId);
        intent.putExtra("quantity", quantity);
        intent.putExtra("size", size);
        startActivity(intent);
    }

    public void onDetailAddToWishlistBtnClicked(View view) {

    }


}