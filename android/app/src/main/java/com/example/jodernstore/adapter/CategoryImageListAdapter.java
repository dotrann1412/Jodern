package com.example.jodernstore.adapter;

import android.content.Intent;
//import com.example.jodern.ProductListActivity;
import com.example.jodernstore.R;
import com.example.jodernstore.fragment.ProductListFragment;
import com.example.jodernstore.model.Category;
import com.example.jodernstore.provider.GeneralProvider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

public class CategoryImageListAdapter extends RecyclerView.Adapter<CategoryImageListAdapter.ViewHolder> {
    private ArrayList<Category> categoryList;
    private Fragment currentFragment;

    public CategoryImageListAdapter(Fragment fragment) {
        currentFragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_image_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);

        holder.name.setText(category.getName());
        holder.image.setImageResource(category.getImageId());
    }

    @Override
    public int getItemCount() {
        return (categoryList != null) ? categoryList.size() : 0;
    }

    public void setCategoryList(ArrayList<Category> categoryList) {
        this.categoryList = categoryList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView image;
        private final TextView name;
        private final LinearLayout wrapper;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.categoryImageView);
            name = itemView.findViewById(R.id.categoryImageName);
            wrapper = itemView.findViewById(R.id.categoryImageWrapper);

            setEvents();
        }

        public void setEvents() {
            wrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Category category = categoryList.get(getAdapterPosition());
                    Fragment fragment = new ProductListFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("entry", "product-list");
                    bundle.putString("categoryRaw", category.getRaw());
                    bundle.putString("categoryName", category.getName());
                    fragment.setArguments(bundle);

                    // Back pressed handling
                    Intent searchIntent = new Intent(currentFragment.getActivity(), ProductListFragment.class);
                    searchIntent.putExtra("entry", "product-list");
                    searchIntent.putExtra("categoryRaw", category.getRaw());
                    searchIntent.putExtra("categoryName", category.getName());
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
