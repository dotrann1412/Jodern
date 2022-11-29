package android.jodern.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.jodern.app.adapter.ProductListAdapter;
import android.jodern.app.customwidget.MyToast;
import android.jodern.app.activity.CartActivity;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class ProductDetailActivity extends AppCompatActivity {
    private TextView detailName, detailPrice, detailPrice2, detailInventory, detailDescription;
    private ImageView detailImage;
    private Product currentProduct;
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
        // TODO: other products in the same category
//        getOtherProducts(intent);
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
                        loadingWrapper.setVisibility(View.GONE);
                        parseProductFromResponse(response);
                        setupMainProduct();
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
        String params = "products-top-k?category=" + categoryRaw + "&id=" + id;
        String url = "http://jodern.store:8000/api/" + params;
        JsonObjectRequest stringRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadingWrapper.setVisibility(View.GONE);
                        ArrayList<Product> otherProducts = parseOtherProductsFromResponse(response);
                        setupOtherProducts(otherProducts);
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
//                    JSONObject product = products.getJSONObject(j);
//                    Long id = product.getInt("id");
//                    String name = product.getString("title");
//                    int price = product.getInt("price");
//                    String imageURL = ((JSONArray)product.get("images")).get(0).toString(); // first image
//                    productList.add(new Product(id, name, imageURL, price));
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

//            Long id = response.getLong("id");
//            String name = response.getString("title");
//            String description = response.getString("description");
//
//            // images
//            JSONArray imageURLs = response.getJSONArray("images");
//            // TODO: image select feature, currently just use the first image
//            ArrayList<String> images = new ArrayList<>();
//            for (int i = 0; i < imageURLs.length(); i++) {
//                images.add(imageURLs.get(i).toString());
//            }
//
//            // inventory quantities
//            Long price = response.getLong("price");
//            JSONObject inventories = response.getJSONObject("inventory");
//            Integer[] inventory = new Integer[5];
//            for (int i = 0; i < sizes.length; i++) {
//                inventory[i] = inventories.getInt(sizes[i]);
//            }
//
//            String category = response.getString("category");
//            currentProduct = new Product(id, name, images, price, description, category, inventory);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupMainProduct() {
        String priceFormatted = Utils.vndFormatPrice(currentProduct.getPrice());

        detailName.setText(currentProduct.getName());
        detailPrice.setText(priceFormatted);
        detailPrice2.setText(priceFormatted);
        detailDescription.setText(currentProduct.getDescription());
        detailInventory.setText(String.valueOf(currentProduct.getInventory(0)));

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

        ProductListAdapter adapter = new ProductListAdapter(this);
        adapter.setProductList(otherProducts);
        otherProductsView.setAdapter(adapter);
    }

    private String parseSearchParams(Intent intent) {
        Long id = intent.getLongExtra("productId", 0);
        return "product/" + id.toString();
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
        Intent intent = new Intent(this, CartActivity.class);
        startActivity(intent);
    }

    public void onDetailAddToCartBtnClicked(View view) {
        // Intent intent = new Intent(this, CartActivity.class);
        // intent.putExtra("productID", currentProduct.getId());
        // intent.putExtra("size", currentSize);
        // startActivity(intent);
    }
}