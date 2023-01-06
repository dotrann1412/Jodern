package com.example.jodernstore.activity;

import static com.example.jodernstore.Utils.vndFormatPrice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;

import com.bumptech.glide.request.FutureTarget;
import com.example.jodernstore.BuildConfig;
import com.example.jodernstore.MainActivity;
import com.example.jodernstore.R;
import com.example.jodernstore.ShareTask;
import com.example.jodernstore.adapter.ProductSliderAdapter;
import com.example.jodernstore.adapter.TrendingAdapter;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.fragment.MyCartFragment;
import com.example.jodernstore.fragment.ProductListFragment;
import com.example.jodernstore.model.Product;
import com.example.jodernstore.provider.GeneralProvider;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {
    private RelativeLayout parentView;

    private Product currentProduct;
    private boolean isInWishlist;

    private SliderView sliderView;
    private TextView detailName, detailPrice, detailInventoryQuantity, detailDescription;

    private int currentQuantity = 1;
    private TextView buyQuantityText;

    private final LinearLayout[] detailSizes = new LinearLayout[5];
    private static final String[] sizes = new String[]{"S", "M", "L", "XL", "XXL"};
    private LinearLayout currentSizeWrapper;
    private String currentSize;

    private MaterialButton seeAllBtn;

    private boolean hasRemovedFromWishlist = false;
    private ImageButton addWishlistBtn, shareBtn, goToHomeBtn;
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
        String url = BuildConfig.SERVER_URL + params;
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
                        MySnackbar.inforSnackbar(ProductDetailActivity.this, parentView, getString(R.string.error_message)).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Access-token", GeneralProvider.with(ProductDetailActivity.this).getJWT());
                return params;
            }
        };
        GeneralProvider.with(this).addToRequestQueue(stringRequest);
    }

    private void getOtherProducts(Intent intent) {
        String categoryRaw = currentProduct.getCategory();
        String id = String.valueOf(currentProduct.getId());
        String entry = "related";
        String params = "id=" + id + "&top_k=" + String.valueOf(5);
        String url = BuildConfig.SERVER_URL + entry + "?" + params;
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
                        MySnackbar.inforSnackbar(ProductDetailActivity.this, parentView, getString(R.string.error_message)).show();
                    }
                }
        );
        GeneralProvider.with(this).addToRequestQueue(stringRequest);
    }

    private void parseProductFromResponse(JSONObject response) {
        try {
            currentProduct = Product.parseJSON(response);
            isInWishlist = response.optBoolean("isInWishList", false);
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

        if (isInWishlist) {
            addWishlistBtn.setImageResource(R.drawable.ic_wishlist_filled_full);
        } else {
            addWishlistBtn.setImageResource(R.drawable.ic_wishlist);
        };
    }

    private void setSizeStatus(LinearLayout detailSize, boolean isEnabled) {
        if (!isEnabled)
            detailSize.setAlpha(0.25f);
        else
            detailSize.setAlpha(1f);
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
        shareBtn = findViewById(R.id.detailShareBtn);
        goToHomeBtn = findViewById(R.id.detailGoToHomeBtn);
    }

    private void setEvents() {
        for (int i = 0; i < 5; i++) {
            int finalI = i;
            detailSizes[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int quantity = currentProduct.getInventory(finalI);
                    if (quantity == 0) {
                        MySnackbar.inforSnackbar(ProductDetailActivity.this, parentView, "Sản phẩm này hiện đã hết size " + sizes[finalI] + ". Mong bạn thông cảm nhé").show();
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

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> imageUrls = currentProduct.getImages();
                ArrayList<FutureTarget<File>> futureTargets = new ArrayList<>();
                for (String url : imageUrls) {
                    FutureTarget<File> futureTarget = Glide.with(ProductDetailActivity.this)
                            .asFile()
                            .load(url)
                            .submit();
                    futureTargets.add(futureTarget);
                }
                new ShareTask(ProductDetailActivity.this, currentProduct.getName(), "image/*").execute(futureTargets.toArray(new FutureTarget[futureTargets.size()]));
            }
        });

        goToHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductDetailActivity.this, MainActivity.class);
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
        if (previousFragment == null) {
            onBackPressed();
            finish();
            return;
        }

        Intent newIntent = null;
        if (previousFragment.equals(MyCartFragment.TAG)) {
            // CartActivity
            newIntent = new Intent(this, CartActivity.class);
        } else {
            // MainActivity
            newIntent = new Intent(this, MainActivity.class);
            if (hasRemovedFromWishlist) {
                // reload destination (for example, this product has just been removed from wishlist, at this activity)
                newIntent.putExtra("nextFragment", previousFragment);
            } else {
                // do not reload destination
                newIntent.putExtra("previousFragment", previousFragment);
            }
        }

        // do not put to back stack
        newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(newIntent);
    }

    public void onDetailAddToCartBtnClicked(View view) {
        Long productId = currentProduct.getId();
        int quantity = currentQuantity;
        String size = currentSize;
        int inventory = currentProduct.getInventory(currentSize);
        if (quantity > inventory) {
            MySnackbar.inforSnackbar(this, parentView, "Số lượng sản phẩm không đủ").show();
            return;
        }

        // TODO: Show dialog of carts
        // demo below
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_add_to_cart);

        HashMap<String, Boolean> storeOptions = new HashMap<>(); // store checked options

        // Cart list
        ArrayList<HashMap<String, String>> sharedCarts = new ArrayList<>();
        ArrayList<HashMap<String, String>> joinedCarts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            sharedCarts.add(new HashMap<String, String>() {{
                put("name", "My Shared Cart " + String.valueOf(finalI));
                put("id", String.valueOf("shared_cart_" + String.valueOf(finalI)));
            }});
            joinedCarts.add(new HashMap<String, String>() {{
                put("name", "My Joined Cart " + String.valueOf(finalI));
                put("id", String.valueOf("joined_cart_" + String.valueOf(finalI)));
            }});
        }

        LinearLayout sharedCartsLayoutWrapper = dialog.findViewById(R.id.mySharedCartsLayoutWrapper);
        LinearLayout sharedCartsLayout = dialog.findViewById(R.id.mySharedCartsLayout);
        LinearLayout joinedCartsLayoutWrapper = dialog.findViewById(R.id.myJoinedCartsLayoutWrapper);
        LinearLayout joinedCartsLayout = dialog.findViewById(R.id.myJoinedCartsLayout);
        MaterialButton saveBtn = dialog.findViewById(R.id.saveBtn);

        CheckBox myCartCheckBox = dialog.findViewById(R.id.myCartCheckbox);
        CheckBox mySharedCartCheckAll = dialog.findViewById(R.id.mySharedCartCheckAll);
        CheckBox myJoinedCartCheckAll = dialog.findViewById(R.id.myJoinedCartCheckAll);

        myCartCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myCartCheckBox.isChecked()) {
                    storeOptions.put("myCart", true);
                } else {
                    storeOptions.remove("myCart");
                }
            }
        });

        setupCheckboxGroup(sharedCartsLayoutWrapper, sharedCartsLayout, mySharedCartCheckAll, sharedCarts, storeOptions);
        setupCheckboxGroup(joinedCartsLayoutWrapper, joinedCartsLayout, myJoinedCartCheckAll, joinedCarts, storeOptions);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (storeOptions.size() == 0) {
                    MySnackbar.inforSnackbar(ProductDetailActivity.this, dialog.getWindow().getDecorView(), "Bạn vui lòng chọn giỏ hàng nhé!").show();
                    return;
                }
                // print option
                for (Map.Entry<String, Boolean> entry : storeOptions.entrySet()) {
                    System.out.println(entry.getKey() + " : " + entry.getValue());
                }
                System.out.println("====================================");

                // TODO: Call API to add product to carts
                try {
                    String entry = "add-to-cart";
                    JSONObject params = new JSONObject();
//                    params.put("productid", currentProduct.getId());
//                    params.put("quantity", quantity);
//                    params.put("sizeid", currentSize);
                    String url = BuildConfig.SERVER_URL + entry + "/";
                    JsonObjectRequest postRequest = new JsonObjectRequest(
                            url,
                            params,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        MySnackbar.inforSnackbar(ProductDetailActivity.this, parentView, "Sản phẩm đã được thêm vào giỏ hàng").show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        showErrorMsg();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    System.out.println(error.toString());
                                    showErrorMsg();
                                }
                            }
                    ) {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String>  params = new HashMap<String, String>();
                            params.put("Access-token", GeneralProvider.with(ProductDetailActivity.this).getJWT());
                            return params;
                        }
                    };
                    GeneralProvider.with(ProductDetailActivity.this).addToRequestQueue(postRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorMsg();
                }
            }
        });

        dialog.show();
    }


    private void setupCheckboxGroup(LinearLayout cartsLayoutWrapper, LinearLayout cartsLayout, CheckBox checkAll, ArrayList<HashMap<String, String>> cartList, HashMap<String, Boolean> storeOptions) {
        if (cartList.size() > 0) {
            cartsLayoutWrapper.setVisibility(View.VISIBLE);
            for (HashMap<String, String> cart : cartList) {
                CheckBox checkbox = (CheckBox) getLayoutInflater().inflate(R.layout.checkbox_item, null);
                checkbox.setText(cart.get("name"));
                checkbox.setTag(cart.get("id"));
                checkbox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (checkbox.isChecked()) {
                            storeOptions.put(checkbox.getTag().toString(), true);
                        } else {
                            storeOptions.remove(checkbox.getTag().toString());
                            checkAll.setChecked(false);
                        }
                    }
                });
                cartsLayout.addView(checkbox);
            }
        } else {
            cartsLayoutWrapper.setVisibility(View.GONE);
            return;
        }

        checkAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAll.isChecked()) {
                    for (int i = 0; i < cartsLayout.getChildCount(); i++) {
                        CheckBox checkBox = (CheckBox) cartsLayout.getChildAt(i);
                        checkBox.setChecked(true);
                        storeOptions.put(checkBox.getTag().toString(), true);
                    }
                } else {
                    for (int i = 0; i < cartsLayout.getChildCount(); i++) {
                        CheckBox checkBox = (CheckBox) cartsLayout.getChildAt(i);
                        checkBox.setChecked(false);
                        storeOptions.remove(checkBox.getTag().toString());
                    }
                }
            }
        });
    }

    public void onDetailAddToWishlistBtnClicked(View view) {
        if (isInWishlist) {
            removeProductFromWishlist();
        } else {
            addProductToWishlist();
        }
    }

    public void addProductToWishlist() {
        try {
            String entry = "add-to-wishlist";
            JSONObject params = new JSONObject();
            params.put("product_id", currentProduct.getId());
            String url = BuildConfig.SERVER_URL + entry + "/";
            JsonObjectRequest postRequest = new JsonObjectRequest(
                    url,
                    params,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                isInWishlist = true;
                                addWishlistBtn.setImageResource(R.drawable.ic_wishlist_filled_full);
                                Snackbar snackbar = Snackbar.make(parentView, "Sản phẩm đã được thêm vào danh sách yêu thích", Snackbar.LENGTH_SHORT);
                                snackbar.setAction(getString(R.string.go_to_wishlist), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(ProductDetailActivity.this, WishlistActivity.class);
                                        startActivity(intent);
                                    }
                                });
                                TextView textView = (TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_action);
                                textView.setAllCaps(false);
                                snackbar.show();
                            } catch (Exception e) {
                                e.printStackTrace();
                                showErrorMsg();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println(error.toString());
                            showErrorMsg();
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("Access-token", GeneralProvider.with(ProductDetailActivity.this).getJWT());
                    return params;
                }
            };
            GeneralProvider.with(this).addToRequestQueue(postRequest);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMsg();
        }
    }

    public void removeProductFromWishlist() {
        try {
            String entry = "remove-from-wishlist";
            JSONObject params = new JSONObject();
            params.put("product_id", currentProduct.getId());
            String url = BuildConfig.SERVER_URL + entry + "/";
            JsonObjectRequest postRequest = new JsonObjectRequest(
                    url,
                    params,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                isInWishlist = false;
                                hasRemovedFromWishlist = true;
                                addWishlistBtn.setImageResource(R.drawable.ic_wishlist);
                                Snackbar snackbar = Snackbar.make(parentView, "Sản phẩm đã bị xóa khỏi danh sách yêu thích", Snackbar.LENGTH_SHORT);
                                snackbar.setAction(getString(R.string.go_to_wishlist), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(ProductDetailActivity.this, WishlistActivity.class);
                                        startActivity(intent);
                                    }
                                });
                                TextView textView = (TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_action);
                                textView.setAllCaps(false);
                                snackbar.show();
                            } catch (Exception e) {
                                e.printStackTrace();
                                showErrorMsg();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println(error.toString());
                            showErrorMsg();
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("Access-token", GeneralProvider.with(ProductDetailActivity.this).getJWT());
                    return params;
                }
            };
            GeneralProvider.with(this).addToRequestQueue(postRequest);
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMsg();
        }
    }

    private void showErrorMsg() {
        MySnackbar.inforSnackbar(this, parentView, getString(R.string.error_message)).show();
    }
}