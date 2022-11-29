package android.jodern.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.jodern.app.adapter.CategoryTagListAdapter;
import android.jodern.app.adapter.ProductListAdapter;
import android.jodern.app.model.Product;
import android.jodern.app.provider.Provider;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

public class ProductListActivity extends AppCompatActivity {
    private String currentCateRaw = "";
    private TextView searchBarText;
    private TextView currentCateText;
    private LinearLayout currentCateWrapper;
    private LinearLayout loadingWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        initViews();

        // two category lists
        setupCategoryLists();

        // get search params from intent
        Intent intent = getIntent();
        String params = parseSearchParams(intent);
        // TODO: hide API KEY
        String url = "http://joderm.store:8000/api/" + params;
        // start loading effect
        loadingWrapper.setVisibility(View.VISIBLE);
        JsonObjectRequest stringRequest = new JsonObjectRequest (
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadingWrapper.setVisibility(View.GONE);
                        ArrayList<Product> productList = parseProductListResponse(response);
                        setupProductList(productList);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingWrapper.setVisibility(View.GONE);
                        Toast.makeText(ProductListActivity.this, "Đã có lỗi xảy ra. Bạn vui lòng thử lại sau nhé", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        Provider.getInstance(this).addToRequestQueue(stringRequest);

        String categoryRaw = intent.getStringExtra("categoryRaw");
        String categoryName = intent.getStringExtra("categoryName");
        if (categoryRaw != null) {
            currentCateWrapper.setVisibility(View.VISIBLE);
            currentCateText.setText(categoryName);
        } else
            currentCateWrapper.setVisibility(View.GONE);
    }

    private ArrayList<Product> parseProductListResponse(JSONObject response) {
        ArrayList<Product> productList = new ArrayList<>();

        try {
            JSONArray keys = response.names();
            for (int i = 0; i < Objects.requireNonNull(keys).length(); i++) {
                String key = keys.getString(i);
                JSONArray products = (JSONArray)response.get(key);
                for (int j = 0; j < products.length(); j++) {
                    JSONObject product = products.getJSONObject(j);
                    int id = product.getInt("id");
                    String name = product.getString("title");
                    int price = product.getInt("price");
                    String imageURL = ((JSONArray)product.get("images")).get(0).toString(); // first image
                    productList.add(new Product(id, name, imageURL, price));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return productList;
    }

    private String parseSearchParams(Intent intent) {
        HashMap<String, String> params = new HashMap<>();
        String entry = intent.getStringExtra("entry");

        if (entry.equals("search")) {
            String type = intent.getStringExtra("type");
            String query = intent.getStringExtra("query");
            params.put("type", type);
            params.put("query", query);
        }
        else if (entry.equals("product-list")) {
            String sex = intent.getStringExtra("sex");
            String categoryRaw = intent.getStringExtra("categoryRaw");
            if (sex != null)
                params.put("sex", sex);

            if (categoryRaw != null) {
                params.remove("sex");
                params.put("category", categoryRaw);
            }
        }

        String url = entry + "?";
        for (String key : params.keySet()) {
            url += key + "=" + params.get(key) + "&";
        }
        return url.substring(0, url.length() - 1);
    }

    private void initViews() {
        searchBarText = findViewById(R.id.productSearchBarText);
        currentCateText = findViewById(R.id.productCurrentCateText);
        currentCateWrapper = findViewById(R.id.productCurrentCateWrapper);
        loadingWrapper = findViewById(R.id.productLoadingWrapper);
    }

    private void setupCategoryLists() {
        // find views
        RecyclerView maleView = findViewById(R.id.productMaleCategoryTagList);
        RecyclerView femaleView = findViewById(R.id.productFemaleCategoryTagList);

        // category list for male
        LinearLayoutManager maleLayout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        maleView.setLayoutManager(maleLayout);
        CategoryTagListAdapter maleAdapter = new CategoryTagListAdapter(this);
        maleAdapter.setCategoryList(Provider.getInstance(this).getCategoryList("nam"));
        maleView.setAdapter(maleAdapter);

        // category list for female
        LinearLayoutManager femaleLayout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        femaleView.setLayoutManager(femaleLayout);
        CategoryTagListAdapter femaleAdapter = new CategoryTagListAdapter(this);
        femaleAdapter.setCategoryList(Provider.getInstance(this).getCategoryList("nu"));

        femaleView.setAdapter(femaleAdapter);
    }


    private void setupProductList(ArrayList<Product> productList) {
        RecyclerView productListView = findViewById(R.id.productListWrapper);
        GridLayoutManager layout = new GridLayoutManager(this, 2);
        productListView.setLayoutManager(layout);

        ProductListAdapter adapter = new ProductListAdapter(this);
        adapter.setProductList(productList);

        productListView.setAdapter(adapter);
    }

    public void onProductSearchBarClicked(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    public void onProductBackBtnClicked(View view) {
        onBackPressed();
        finish();
    }

    public void onProductCartBtnClicked(View view) {
        // TODO: Go to Cart Activity
    }
}