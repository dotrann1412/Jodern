package com.example.jodern.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.jodern.MainActivity;
import com.example.jodern.R;
import com.example.jodern.activity.ProductDetailActivity;
import com.example.jodern.activity.SearchActivity;
import com.example.jodern.adapter.CategoryImageListAdapter;
import com.example.jodern.adapter.ProductListAdapter;
import com.example.jodern.adapter.TrendingAdapter;
import com.example.jodern.customwidget.MyToast;
import com.example.jodern.model.Product;
import com.example.jodern.provider.Provider;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class HomeFragment extends Fragment {
    public static final String TAG = "HomeFragment";

    private LinearLayout homeSearchBar;
    private ImageButton navbarBtn;
    private MaterialButton maleSeeAllBtn;
    private MaterialButton femaleSeeAllBtn;
    private RecyclerView trendingView;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    public HomeFragment(ImageButton navbarBtn) {
        super(R.layout.fragment_home);
        this.navbarBtn = navbarBtn;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Provider.with(this.getContext()).setCurrentFragment(TAG);
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (navbarBtn != null)
            navbarBtn.setImageResource(R.drawable.ic_home_filled);
        initViews();
        setEvents();
        setupCategoryLists();
        getTrendingProducts();
    }

    private void initViews() {
        homeSearchBar = getView().findViewById(R.id.homeSearchBar);
        maleSeeAllBtn = getView().findViewById(R.id.homeMaleSeeAllBtn);
        femaleSeeAllBtn = getView().findViewById(R.id.homeFemaleSeeAllBtn);
        trendingView = getView().findViewById(R.id.homeTrendingWrapper);
    }

    private void setEvents() {
        homeSearchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        maleSeeAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new ProductListFragment();
                Bundle bundle = new Bundle();
                bundle.putString("entry", "product-list");
                bundle.putString("sex", "nam");
                bundle.putString("categoryName", "Thời trang nam");
                fragment.setArguments(bundle);

                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.mainFragmentContainer, fragment);
                fragmentTransaction.addToBackStack("productList");
                fragmentTransaction.commit();
            }
        });

        femaleSeeAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new ProductListFragment();
                Bundle bundle = new Bundle();
                bundle.putString("entry", "product-list");
                bundle.putString("sex", "nu");
                bundle.putString("categoryName", "Thời trang nữ");
                fragment.setArguments(bundle);

                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.mainFragmentContainer, fragment);
                fragmentTransaction.addToBackStack("productList");
                fragmentTransaction.commit();
            }
        });
    }

    private void getTrendingProducts() {
        // demo API
//        String entry = "";
//        String url = "http://jodern.store:8000/api/" + entry;
        String url = "http://jodern.store:8000/api/product-list?category=ao-khoac-nam";
        JsonObjectRequest postRequest = new JsonObjectRequest (
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do something
                        ArrayList<Product> trendingProducts = Product.parseProductListFromResponse(response);
                        setupTrendingProducts(trendingProducts);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        return;
                    }
                }
        );
        Provider.with(this.getContext()).addToRequestQueue(postRequest);


    }

    private void setupTrendingProducts(ArrayList<Product> trendingProducts) {
        TrendingAdapter trendingAdapter = new TrendingAdapter(this.getContext());
        trendingAdapter.setProductList(trendingProducts);
        trendingView.setAdapter(trendingAdapter);
        trendingView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false));
    }



    private void setupCategoryLists() {
        // find views
        RecyclerView maleView = getView().findViewById(R.id.mainMaleCategoryImageList);
        RecyclerView femaleView = getView().findViewById(R.id.mainFemaleCategoryImageList);

        // category list for male
        CategoryImageListAdapter maleAdapter = new CategoryImageListAdapter(this);
        maleAdapter.setCategoryList(Provider.with(this.getContext()).getCategoryList("nam"));
        maleView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false));
        maleView.setAdapter(maleAdapter);

        // category list for female
        CategoryImageListAdapter femaleAdapter = new CategoryImageListAdapter(this);
        femaleAdapter.setCategoryList(Provider.with(this.getContext()).getCategoryList("nu"));
        femaleView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false));
        femaleView.setAdapter(femaleAdapter);
    }

    @Override
    public void onDestroyView() {
        if (navbarBtn != null)
            navbarBtn.setImageResource(R.drawable.ic_home);
        super.onDestroyView();
    }
}