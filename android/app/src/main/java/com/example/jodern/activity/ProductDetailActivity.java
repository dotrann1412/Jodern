package com.example.jodern.activity;

import static com.example.jodern.Utils.vndFormatPrice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;

import com.example.jodern.MainActivity;
import com.example.jodern.R;
import com.example.jodern.adapter.ProductSliderAdapter;
import com.example.jodern.adapter.TrendingAdapter;
import com.example.jodern.cart.CartController;
import com.example.jodern.cart.cartitem.CartItem;
import com.example.jodern.customwidget.MySnackbar;
import com.example.jodern.fragment.CartFragment;
import com.example.jodern.fragment.ProductListFragment;
import com.example.jodern.fragment.WishlistFragment;
import com.example.jodern.interfaces.ChangeNumItemsListener;
import com.example.jodern.model.Product;
import com.example.jodern.provider.Provider;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jodern.wishlist.WishlistController;
import com.example.jodern.wishlist.wishlistitem.WishlistItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProductDetailActivity extends AppCompatActivity {
    private RelativeLayout parentView;

    private Product currentProduct;

    private SliderView sliderView;
    private TextView detailName, detailPrice, detailInventoryQuantity, detailDescription;

    private int currentQuantity = 1;
    private TextView buyQuantityText;

    private final LinearLayout[] detailSizes = new LinearLayout[5];
    private static final String[] sizes = new String[]{"S", "M", "L", "XL", "XXL"};
    private LinearLayout currentSizeWrapper;
    private String currentSize;

    private MaterialButton seeAllBtn;

    private boolean isInWishlist = false;
    private boolean hasRemovedFromWishlist = false;
    private ImageButton addWishlistBtn;
    private LinearLayout loadingWrapper;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
                        getOtherProducts(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingWrapper.setVisibility(View.GONE);
                        MySnackbar.inforSnackar(ProductDetailActivity.this, parentView, getString(R.string.error_message)).show();
                    }
                }
        );
        Provider.with(this).addToRequestQueue(stringRequest);
    }

    private void getOtherProducts(Intent intent) {
        String categoryRaw = currentProduct.getCategory();
        String id = String.valueOf(currentProduct.getId());
        String entry = "related";
        String params = "id=" + id + "&top_k=" + String.valueOf(5);
        String url = "http://jodern.store:8000/api/" + entry + "?" + params;
        JsonObjectRequest stringRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ArrayList<Product> otherProducts = Product.parseProductListFromResponse(response);
                        setupOtherProducts(otherProducts);
                        loadingWrapper.setVisibility(View.GONE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingWrapper.setVisibility(View.GONE);
                        MySnackbar.inforSnackar(ProductDetailActivity.this, parentView, getString(R.string.error_message)).show();
                    }
                }
        );
        Provider.with(this).addToRequestQueue(stringRequest);
    }

    private void parseProductFromResponse(JSONObject response) {
        try {
            currentProduct = Product.parseJSON(response);
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

        currentQuantity = 1;
        buyQuantityText.setText("1" );

        // image slider
        ArrayList<String> images = currentProduct.getImages();
        ProductSliderAdapter adapter = new ProductSliderAdapter(this);
        adapter.setItems(images);
        if (images.size() > 1)
            sliderView.setSliderAdapter(adapter);
        else
            sliderView.setSliderAdapter(adapter, false);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.startAutoCycle();

        // inventory
        Integer[] inventories = currentProduct.getInventories();
        boolean flag = false;
        for (int i = 0; i < inventories.length; i++) {
            if (inventories[i] == 0) {
                setSizeStatus(detailSizes[i], false);
            } else {
                setSizeStatus(detailSizes[i], true);
                if (!flag) {
                    currentSizeWrapper = detailSizes[0];
                    currentSize = sizes[0];
                    updateCurrentSizeView(true);
                    detailInventoryQuantity.setText(String.valueOf(currentProduct.getInventory(i)));
                    flag = true;
                }
            }
        }

        specifyProductInWishlist();
    }

    private void setSizeStatus(LinearLayout detailSize, boolean isEnabled) {
        if (!isEnabled)
            detailSize.setAlpha(0.25f);
        else
            detailSize.setAlpha(1f);
    }

    private void specifyProductInWishlist() {
        List<WishlistItem> wishlist = WishlistController.with(this).getWishlistItemList();
        for (WishlistItem item : wishlist) {
            if (item.getProductId().equals(currentProduct.getId())) {
                isInWishlist = true;
                addWishlistBtn.setImageResource(R.drawable.ic_wishlist_filled_full);
                return;
            }
        }
        isInWishlist = false;
        addWishlistBtn.setImageResource(R.drawable.ic_wishlist);
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
        parentView = findViewById(R.id.detailParentView);

        sliderView = findViewById(R.id.detailImageSlider);
        detailName = findViewById(R.id.detailName);
        detailPrice = findViewById(R.id.detailPrice);
        detailDescription = findViewById(R.id.detailDescription);

        detailInventoryQuantity = findViewById(R.id.detailInventory);
        buyQuantityText = findViewById(R.id.detailQuantity);

        loadingWrapper = findViewById(R.id.detailLoadingWrapper);

        int[] sizeIds = {R.id.detailSizeS, R.id.detailSizeM, R.id.detailSizeL, R.id.detailSizeXL, R.id.detailSizeXXL};
        for (int i = 0; i < 5; i++) {
            detailSizes[i] = findViewById(sizeIds[i]);
        }
        currentSizeWrapper = detailSizes[0];
        currentSize = sizes[0];
        updateCurrentSizeView(true);

        seeAllBtn = findViewById(R.id.detailSeeAllBtn);
        addWishlistBtn = findViewById(R.id.detailAddToWishlistBtn);
    }

    private void setEvents() {
        for (int i = 0; i < 5; i++) {
            int finalI = i;
            detailSizes[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int quantity = currentProduct.getInventory(finalI);
                    if (quantity == 0) {
                        MySnackbar.inforSnackar(ProductDetailActivity.this, parentView, "Sản phẩm này hiện đã hết size " + sizes[finalI] + ". Mong bạn thông cảm nhé").show();
                        return;
                    }
                    updateCurrentSizeView(false);
                    currentSizeWrapper = detailSizes[finalI];
                    currentSize = sizes[finalI];
                    updateCurrentSizeView(true);
                    detailInventoryQuantity.setText(String.valueOf(currentProduct.getInventory(finalI)));
                }
            });
        }

        seeAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductDetailActivity.this, MainActivity.class);
                intent.putExtra("entry", "product-list");
                intent.putExtra("categoryRaw", currentProduct.getCategory());
                intent.putExtra("categoryName", currentProduct.getCategoryName());
                intent.putExtra("nextFragment", ProductListFragment.TAG);
                startActivity(intent);
            }
        });
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
        Intent intent = getIntent();
        String previousFragment = intent.getStringExtra("previousFragment");

        if (previousFragment == null || (!previousFragment.equals(CartFragment.TAG) && !previousFragment.equals(WishlistFragment.TAG))) {
            onBackPressed();
            finish();
            return;
        }

        // This product is access from cart or wishlist
        Intent newIntent = new Intent(this, MainActivity.class);
        if (hasRemovedFromWishlist) {
            // reload destination (for example, this product has just been removed from wishlist, at this activity)
            newIntent.putExtra("nextFragment", previousFragment);
        } else {
            // do not reload destination
            newIntent.putExtra("previousFragment", previousFragment);
        }
        startActivity(newIntent);
    }

    public void onDetailAddToCartBtnClicked(View view) {
        Long productId = currentProduct.getId();
        Integer quantity = currentQuantity;
        String size = currentSize;
        int inventory = currentProduct.getInventory(currentSize);
        if (quantity > inventory) {
            MySnackbar.inforSnackar(this, parentView, "Số lượng sản phẩm không đủ").show();
            return;
        }

        Snackbar snackbar = Snackbar.make(parentView, "Sản phẩm đã được thêm vào giỏ hàng", Snackbar.LENGTH_SHORT);
        snackbar.setAction(getString(R.string.go_to_cart), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductDetailActivity.this, MainActivity.class);
                intent.putExtra("nextFragment", CartFragment.TAG);
                startActivity(intent);
            }
        });
        TextView textView = (TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_action);
        textView.setAllCaps(false);
        snackbar.show();

        CartController.with(this).addToCart(new CartItem(productId, quantity, size));
    }

    public void onDetailAddToWishlistBtnClicked(View view) {
        if (isInWishlist) {
            WishlistController.with(this).deleteItem(currentProduct.getId(), new ChangeNumItemsListener() {
                @Override
                public void onChanged() {
                    isInWishlist = false;
                    hasRemovedFromWishlist = true;
                    addWishlistBtn.setImageResource(R.drawable.ic_wishlist);
                    Snackbar snackbar = Snackbar.make(parentView, "Sản phẩm đã bị xóa khỏi danh sách yêu thích", Snackbar.LENGTH_SHORT);
                    snackbar.setAction(getString(R.string.go_to_wishlist), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(ProductDetailActivity.this, MainActivity.class);
                            intent.putExtra("nextFragment", WishlistFragment.TAG);
                            startActivity(intent);
                        }
                    });
                    TextView textView = (TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_action);
                    textView.setAllCaps(false);
                    snackbar.show();
                }
            });
            return;
        }

        WishlistController.with(this).addToWishlist(new WishlistItem(currentProduct.getId()));
        isInWishlist = true;
        addWishlistBtn.setImageResource(R.drawable.ic_wishlist_filled_full);
        Snackbar snackbar = Snackbar.make(parentView, "Sản phẩm đã được thêm vào danh sách yêu thích", Snackbar.LENGTH_SHORT);
        snackbar.setAction(getString(R.string.go_to_wishlist), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductDetailActivity.this, MainActivity.class);
                intent.putExtra("nextFragment", WishlistFragment.TAG);
                startActivity(intent);
            }
        });
        TextView textView = (TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_action);
        textView.setAllCaps(false);
        snackbar.show();
    }
}