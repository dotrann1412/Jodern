package com.example.jodernstore.adapter;

import android.content.Intent;
import com.example.jodernstore.fragment.ProductListFragment;
import com.example.jodernstore.model.Category;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jodernstore.R;
import com.example.jodernstore.provider.GeneralProvider;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class CategoryTagListAdapter extends RecyclerView.Adapter<CategoryTagListAdapter.ViewHolder> {
    private ArrayList<Category> categoryList;
    private Fragment currentFragment;
    
    public CategoryTagListAdapter(Fragment fragment) {
        currentFragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_tag_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.btn.setText(category.getName());
    }

    @Override
    public int getItemCount() {
        return (categoryList != null) ? categoryList.size() : 0;
    }

    public void setCategoryList(ArrayList<Category> categoryList) {
        this.categoryList = categoryList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialButton btn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            btn = itemView.findViewById(R.id.categoryTagWrapper);
            setEvents();
        }

        public void setEvents() {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Category category = categoryList.get(getAdapterPosition());
                    Fragment fragment = new ProductListFragment();
                    Bundle bundle = new Bundle();

                    String rawKey = "categoryRaw";
                    if (category.isAll())
                        rawKey = "sex";

                    String name = category.getName();
                    if (category.isAll()) {
                        if (category.getRaw().equals("nam"))
                            name = "Thời trang nam";
                        else if (category.getRaw().equals("nu"))
                            name = "Thời trang nữ";
                    }

                    bundle.putString("entry", "product-list");
                    bundle.putString(rawKey, category.getRaw());
                    bundle.putString("categoryName",name);
                    fragment.setArguments(bundle);

                    Intent searchIntent = new Intent(currentFragment.getActivity(), ProductListFragment.class);
                    searchIntent.putExtra("entry", "product-list");
                    searchIntent.putExtra(rawKey, category.getRaw());
                    searchIntent.putExtra("categoryName", name);
                    GeneralProvider.with(currentFragment.getContext()).setSearchIntent(searchIntent);

                    FragmentManager fragmentManager = currentFragment.requireActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.mainFragmentContainer, fragment);
                    fragmentTransaction.addToBackStack("productList");
                    fragmentTransaction.commit();
                }
            });
        }
    }
}
