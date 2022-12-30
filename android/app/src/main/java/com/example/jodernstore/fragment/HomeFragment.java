package com.example.jodernstore.fragment;

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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.example.jodernstore.BuildConfig;
import com.example.jodernstore.R;
import com.example.jodernstore.activity.SearchActivity;
import com.example.jodernstore.adapter.CategoryImageListAdapter;
import com.example.jodernstore.adapter.TrendingAdapter;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.model.Product;
import com.example.jodernstore.provider.GeneralProvider;
import com.facebook.AccessToken;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONObject;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    public static final String TAG = "HomeFragment";

    private FrameLayout parentView;
    private LinearLayout homeSearchBar;
    private ImageButton navbarBtn;
    private MaterialButton maleSeeAllBtn;
    private MaterialButton femaleSeeAllBtn;
    private RecyclerView trendingView;
    private RoundedImageView avatar;
    private TextView userName;

    private FirebaseAuth mAuth;

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
        GeneralProvider.with(this.getContext()).setCurrentFragment(TAG);
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (navbarBtn != null)
            navbarBtn.setImageResource(R.drawable.ic_home_filled);
        initViews();
        setInfors();
        setEvents();
        setupCategoryLists();
        getTrendingProducts();
    }

    private void setInfors() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null)
            return;
        userName.setText(user.getDisplayName());
        // avatar
        String url = "";
        if (AccessToken.getCurrentAccessToken() != null) {
            url = user.getPhotoUrl() + "?access_token=" + AccessToken.getCurrentAccessToken().getToken() + "&type=large";
        } else
            url = user.getPhotoUrl() + "?type=large";
        Glide.with(this).load(url).into(avatar);
    }

    private void initViews() {
        parentView = getView().findViewById(R.id.homeParentView);
        homeSearchBar = getView().findViewById(R.id.homeSearchBar);
        maleSeeAllBtn = getView().findViewById(R.id.homeMaleSeeAllBtn);
        femaleSeeAllBtn = getView().findViewById(R.id.homeFemaleSeeAllBtn);
        trendingView = getView().findViewById(R.id.homeTrendingWrapper);
        avatar = getView().findViewById(R.id.homeAvatar);
        userName = getView().findViewById(R.id.homeUserName);
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
        String entry = "trending";
        String params = "8";
        String url = BuildConfig.SERVER_URL + entry + "/" + params;
        JsonObjectRequest postRequest = new JsonObjectRequest (
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ArrayList<Product> trendingProducts = Product.parseProductListFromResponse(response);
                        for (Product product : trendingProducts) {
                            System.out.println(product.getId());
                            System.out.println(product.getName());
                        }
                        setupTrendingProducts(trendingProducts);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        MySnackbar.inforSnackar(requireActivity().getApplicationContext(), parentView, getString(R.string.error_message)).show();
                    }
                }
        );
        GeneralProvider.with(this.getContext()).addToRequestQueue(postRequest);
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
        maleAdapter.setCategoryList(GeneralProvider.with(this.getContext()).getCategoryList("nam"));
        maleView.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false));
        maleView.setAdapter(maleAdapter);

        // category list for female
        CategoryImageListAdapter femaleAdapter = new CategoryImageListAdapter(this);
        femaleAdapter.setCategoryList(GeneralProvider.with(this.getContext()).getCategoryList("nu"));
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