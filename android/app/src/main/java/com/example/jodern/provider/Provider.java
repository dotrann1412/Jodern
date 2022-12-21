package com.example.jodern.provider;

import android.content.Context;
import android.content.Intent;

import com.example.jodern.R;
import com.example.jodern.fragment.HomeFragment;
import com.example.jodern.model.Category;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;

public class Provider {
    private static Provider instance = null;
    private String jwtToken;
    private String currentFragment;
    private RequestQueue requestQueue;
    private static Context context;
    private HashMap<String, ArrayList<Category>> categoryListMapping;
    private Intent searchIntent;
//    private byte[] imageBytes = null;
    private String imageBase64 = null;

    public static Provider with(Context context) {
        if (instance == null) {
            synchronized (Provider.class) {
                if (instance == null) {
                    instance = new Provider(context);
                }
            }
        }
        return instance;
    }

    private Provider(Context context) {
        Provider.context = context;
        currentFragment = HomeFragment.TAG;

        if (categoryListMapping == null)
            categoryListMapping = new HashMap<>();

        requestQueue = getRequestQueue();
        initData();
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public void clearJwtToken() {
        this.jwtToken = null;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // context.getApplicationContext() keeps us from leaking the Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public void setCurrentFragment(String currentFragment) {
        this.currentFragment = currentFragment;
    }

    public String getCurrentFragment() {
        return currentFragment;
    }

    public void setSearchIntent(Intent searchIntent) {
        this.searchIntent = searchIntent;
    }

    public Intent getSearchIntent() {
        return searchIntent;
    }

//    public void setImageBytes(byte[] imageBytes) {
//        this.imageBytes = imageBytes;
//    }
//
//    public byte[] getImageBytes() {
//        return imageBytes;
//    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    private void initData() {
        initCategories();
    }

    private void initCategories() {
        String[] maleCategories = new String[]{"Áo khoác nam", "Áo thun nam", "Áo polo nam", "Áo sơ mi nam", "Quần jean nam", "Quần tây nam", "Quần kaki nam", "Quần short nam"};
        int[] maleImageIds = new int[]{R.drawable.ao_khoac_nam, R.drawable.ao_thun_nam, R.drawable.ao_polo_nam, R.drawable.ao_so_mi_nam, R.drawable.quan_jean_nam, R.drawable.quan_tay_nam, R.drawable.quan_kaki_nam, R.drawable.quan_short_nam};
        String[] maleRaws = new String[]{"ao-khoac-nam", "ao-thun-nam", "ao-polo-nam", "ao-so-mi-nam", "quan-jean-nam", "quan-tay-nam", "quan-kaki", "quan-short-nam"};
        String[] femaleCategories = new String[]{"Áo khoác nữ", "Áo thun nữ", "Áo sơ mi nữ", "Quần jean nữ", "Quần tây nữ", "Quần short nữ", "Chân váy", "Váy đầm"};
        int[] femaleImageIds = new int[]{R.drawable.ao_khoac_nu, R.drawable.ao_thun_nu, R.drawable.ao_so_mi_nu, R.drawable.quan_jean_nu, R.drawable.quan_tay_nu, R.drawable.quan_short_nu, R.drawable.chan_vay, R.drawable.vay_dam};
        String[] femaleRaws = new String[] {"ao-khoac-nu", "ao-thun-nu", "ao-so-mi-nu", "quan-jean-nu", "quan-tay-nu", "quan-short-nu", "chan-vay", "vay-dam"};

        categoryListMapping.put("nam", new ArrayList<>());
        for (int i = 0; i < maleCategories.length; i++) {
            categoryListMapping.get("nam").add(new Category(maleCategories[i], maleImageIds[i], maleRaws[i]));
        }

        categoryListMapping.put("nu", new ArrayList<>());
        for (int i = 0; i < femaleCategories.length; i++) {
            categoryListMapping.get("nu").add(new Category(femaleCategories[i], femaleImageIds[i], femaleRaws[i]));
        }

    }

    public ArrayList<Category> getCategoryList(String gender) {
        return categoryListMapping.get(gender);
    }

    public ArrayList<Category> getCategoryList(String gender, boolean addShowAll) {
        ArrayList<Category> cates = categoryListMapping.get(gender);
        if (!addShowAll)
            return cates;

        ArrayList<Category> tags = new ArrayList<>();
        tags.add(new Category("Tất cả", 0, gender, true));
        tags.addAll(cates);
        return tags;
    }
}
