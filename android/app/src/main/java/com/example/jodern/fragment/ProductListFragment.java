package com.example.jodern.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.jodern.R;
import com.example.jodern.activity.SearchActivity;
import com.example.jodern.adapter.CategoryTagListAdapter;
import com.example.jodern.adapter.ProductListAdapter;
import com.example.jodern.customwidget.MyToast;
import com.example.jodern.model.Product;
import com.example.jodern.provider.Provider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ProductListFragment extends Fragment {
    public static final String TAG = "ProductListFragment";
    private TextView currentCateText;
    private TextView productSearchBarText;
    private LinearLayout searchBar;
    private LinearLayout currentCateWrapper;
    private LinearLayout loadingWrapper;
    private LinearLayout emptyWrapper;
    private FloatingActionButton floatBtn;

    public ProductListFragment() {
        // Required empty public constructor
        super(R.layout.fragment_product_list);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Provider.with(this.getContext()).setCurrentFragment(TAG);
        return inflater.inflate(R.layout.fragment_product_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
        setEvents();
        setupCategoryLists();
        handleAPICalls();
        updateTextsBaseOnIntent();
    }

    private void initViews() {
        currentCateText = getView().findViewById(R.id.productCurrentCateText);
        productSearchBarText = getView().findViewById(R.id.productSearchBarText);
        searchBar = getView().findViewById(R.id.productSearchBar);
        currentCateWrapper = getView().findViewById(R.id.productCurrentCateWrapper);
        loadingWrapper = getView().findViewById(R.id.productLoadingWrapper);
        emptyWrapper = getView().findViewById(R.id.productEmptyWrapper);
        floatBtn = getView().findViewById(R.id.productFloatBtn);
    }

    private void setEvents() {
        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SearchActivity.class);
                intent.putExtra("previousFragment", TAG);
                startActivity(intent);
            }
        });

        floatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // scroll to top
                NestedScrollView layout = getView().findViewById(R.id.productBaseWrapper);
                layout.smoothScrollTo(0, 0);
            }
        });
    }


    private void setupCategoryLists() {
        // find views
        RecyclerView maleView = getView().findViewById(R.id.productMaleCategoryTagList);
        RecyclerView femaleView = getView().findViewById(R.id.productFemaleCategoryTagList);

        // category list for male
        LinearLayoutManager maleLayout = new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false);
        maleView.setLayoutManager(maleLayout);
        CategoryTagListAdapter maleAdapter = new CategoryTagListAdapter(this);
        maleAdapter.setCategoryList(Provider.with(this.getContext()).getCategoryList("nam"));
        maleView.setAdapter(maleAdapter);

        // category list for female
        LinearLayoutManager femaleLayout = new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false);
        femaleView.setLayoutManager(femaleLayout);
        CategoryTagListAdapter femaleAdapter = new CategoryTagListAdapter(this);
        femaleAdapter.setCategoryList(Provider.with(this.getContext()).getCategoryList("nu"));

        femaleView.setAdapter(femaleAdapter);
    }


    private void handleAPICalls() {
        // start loading effect
        loadingWrapper.setVisibility(View.VISIBLE);

        Bundle args = getArguments();
        String method = args.getString("method");
        if (method == null) {
            // GET requests
            String searchParams = parseSearchParams(args);
            String url = "http://jodern.store:8000/api/" + searchParams;
            System.out.println("URL: " + url);
            JsonObjectRequest getRequest = new JsonObjectRequest (
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println("Success");
                            handleResponse(response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println("Error");
                            handleError(error);
                        }
                    }
            );
            Provider.with(this.getContext()).addToRequestQueue(getRequest);
        }
        else if (method.equals("post")) {
            // POST requests
            String entry = args.getString("entry");
            String query = args.getString("query");
            HashMap<String, String> params = new HashMap<>();
            params.put("query", query);

            String url = "http://jodern.store:8000/api/" + entry + "/";
            JsonObjectRequest postRequest = new JsonObjectRequest (
                    Request.Method.POST,
                    url,
                    new JSONObject(params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println("POST success");
                            handleResponse(response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println("POST error");
                            handleError(error);
                        }
                    }
            );
            Provider.with(this.getContext()).addToRequestQueue(postRequest);
        }
    }

    private String parseSearchParams(Bundle args) {
        HashMap<String, String> params = new HashMap<>();
        String entry = args.getString("entry");

        if (entry.equals("search")) {
            String query = args.getString("query");
            params.put("query", query);
        }
        else if (entry.equals("product-list")) {
            String sex = args.getString("sex");
            String categoryRaw = args.getString("categoryRaw");
            if (sex != null)
                params.put("sex", sex);

            if (categoryRaw != null) {
                params.remove("sex");
                params.put("category", categoryRaw);
            }
        }

        StringBuilder url = new StringBuilder(entry + "?");
        for (String key : params.keySet()) {
            url.append(key).append("=").append(params.get(key)).append("&");
        }
        return url.substring(0, url.length() - 1);
    }

    private void handleError(VolleyError error) {
        loadingWrapper.setVisibility(View.GONE);
        MyToast.makeText(this.getContext(), getString(R.string.error_message), Toast.LENGTH_SHORT);
    }

    private void handleResponse(JSONObject response) {
        loadingWrapper.setVisibility(View.GONE);
        ArrayList<Product> productList = parseProductListFromResponse(response);
        setupProductList(productList);
    }

    private ArrayList<Product> parseProductListFromResponse(JSONObject response) {
        ArrayList<Product> productList = new ArrayList<>();

        try {
            JSONArray keys = response.names();
            for (int i = 0; i < Objects.requireNonNull(keys).length(); i++) {
                String key = keys.getString(i);
                JSONArray products = (JSONArray)response.get(key);
                for (int j = 0; j < products.length(); j++) {
                    productList.add(Product.parseJSON((JSONObject)products.get(j)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return productList;
    }

    private void setupProductList(ArrayList<Product> productList) {
        RecyclerView productListView = getView().findViewById(R.id.productListWrapper);
        GridLayoutManager layout = new GridLayoutManager(this.getContext(), 2);
        productListView.setLayoutManager(layout);

        ProductListAdapter adapter = new ProductListAdapter(this.getContext());
        adapter.setProductList(productList);

        if (productList.size() == 0) {
            emptyWrapper.setVisibility(View.VISIBLE);
        } else {
            emptyWrapper.setVisibility(View.GONE);
            productListView.setAdapter(adapter);
        }
    }

    private void updateTextsBaseOnIntent() {
        Bundle args = getArguments();
        String categoryName = args.getString("categoryName");
        if (categoryName != null) {
            currentCateWrapper.setVisibility(View.VISIBLE);
            currentCateText.setText(categoryName);
        } else
            currentCateWrapper.setVisibility(View.GONE);

        String entry = args.getString("entry");
        if (entry.equals("search")) {
            String method = args.getString("method");
            if (method == null) {
                String query = args.getString("query");
                productSearchBarText.setText(query);
                productSearchBarText.setTextColor(this.getContext().getColor(R.color.primary));
            } else {
                productSearchBarText.setText(getString(R.string.search_hint));
                productSearchBarText.setTextColor(this.getContext().getColor(R.color.text_light));
            }
        }
    }

}