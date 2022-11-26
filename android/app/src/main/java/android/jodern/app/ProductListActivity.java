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

import java.util.ArrayList;

public class ProductListActivity extends AppCompatActivity {
    private String currentCateRaw = "";
    private TextView searchBarText;
    private TextView currentCateText;
    private LinearLayout currentCateWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        initViews();

        // get intent data
        Intent intent = getIntent();
        String text = intent.getStringExtra("text");
        String categoryRaw = intent.getStringExtra("categoryRaw");
        if (text != null) {
            searchBarText.setText(text);
            searchBarText.setTextColor(getColor(R.color.primary));
            // TODO: Create params for API call with text query
        }
        else {
            searchBarText.setText("Bạn tìm gì hôm nay");
            searchBarText.setTextColor(getColor(R.color.text_light));

            if (categoryRaw != null) {
                currentCateRaw = categoryRaw;
                String name = intent.getStringExtra("categoryName");
                currentCateText.setText(name);
                // TODO: Create params for API call with category
            }
            else {
                String base64String = intent.getStringExtra("image");
                // TODO: Create params for API call with image query
            }
        }

        if (categoryRaw != null)
            currentCateWrapper.setVisibility(View.VISIBLE);
        else
            currentCateWrapper.setVisibility(View.GONE);


        // two category lists
        setupCategoryLists();

//        // TODO: Call API
////        String url = "https://joderm.store:8000/api/" + params;
//        String url = "https://www.g1oogle.com";
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//            new Response.Listener<String>() {
//                @Override
//                public void onResponse(String response) {
////                    System.out.println("Success");
//                    Log.d("Success", response);
//                }
//            },
//            new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    Log.d("Error", error.toString());
//                }
//            }
//        );
//        Provider.getInstance(this).addToRequestQueue(stringRequest);

        // after finishing the above call, this function show be called
        // now it's just a demo
        setupProductList();
    }

    private void initViews() {
        searchBarText = findViewById(R.id.productSearchBarText);
        currentCateText = findViewById(R.id.productCurrentCateText);
        currentCateWrapper = findViewById(R.id.productCurrentCateWrapper);
    }

    private void setupCategoryLists() {
        // find views
        RecyclerView maleView = findViewById(R.id.productMaleCategoryTagList);
        RecyclerView femaleView = findViewById(R.id.productFemaleCategoryTagList);

        // category list for male
        LinearLayoutManager maleLayout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        maleView.setLayoutManager(maleLayout);
        CategoryTagListAdapter maleAdapter = new CategoryTagListAdapter(this, currentCateRaw);
        maleAdapter = new CategoryTagListAdapter(this, currentCateRaw);
        maleAdapter.setCategoryList(Provider.getInstance(this).getCategoryList("nam"));
        maleView.setAdapter(maleAdapter);

        // category list for female
        LinearLayoutManager femaleLayout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        femaleView.setLayoutManager(femaleLayout);
        CategoryTagListAdapter femaleAdapter = new CategoryTagListAdapter(this, currentCateRaw);
        femaleAdapter = new CategoryTagListAdapter(this, currentCateRaw);
        femaleAdapter.setCategoryList(Provider.getInstance(this).getCategoryList("nu"));

        femaleView.setAdapter(femaleAdapter);
    }


    private void setupProductList() {
        RecyclerView productListView = findViewById(R.id.productListWrapper);
        GridLayoutManager layout = new GridLayoutManager(this, 2);
        productListView.setLayoutManager(layout);

        ProductListAdapter adapter = new ProductListAdapter(this);

        // fake data
        ArrayList<Product> fakeArr = new ArrayList<>();
        for (int i = 0; i < 15; i++)
            fakeArr.add(new Product(1, "name", "url", 10000));
        adapter.setProductList(fakeArr);
//        adapter.setProductList(new ArrayList<ProductListItem>(6) {});

        productListView.setAdapter(adapter);
    }

    public void onProductSearchBarClicked(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    public void onProductBackBtnClicked(View view) {
        onBackPressed();
        startActivity(new Intent(ProductListActivity.this, SearchActivity.class));
        finish();
    }

    public void onProductCartBtnClicked(View view) {
        // TODO: Go to Cart Activity
    }
}