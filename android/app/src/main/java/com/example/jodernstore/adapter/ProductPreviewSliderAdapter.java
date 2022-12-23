package com.example.jodern.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.jodern.R;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;

public class ProductPreviewSliderAdapter extends SliderViewAdapter<ProductPreviewSliderAdapter.ProductPreviewSliderAdapterViewHolder> {

    private final Context context;
    private ArrayList<String> itemUrls;

    public ProductPreviewSliderAdapter(Context context) {
        this.context = context;
    }

    public void setItems(ArrayList<String> itemUrls) {
        this.itemUrls = itemUrls;
        notifyDataSetChanged();
    }

    @Override
    public ProductPreviewSliderAdapter.ProductPreviewSliderAdapterViewHolder onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_preview_view_holder, null);
        return new ProductPreviewSliderAdapter.ProductPreviewSliderAdapterViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ProductPreviewSliderAdapter.ProductPreviewSliderAdapterViewHolder viewHolder, final int position) {
        String url =  itemUrls.get(position);
        Glide.with(context)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.item_placeholder)
                .into(viewHolder.imageView);
    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return itemUrls.size();
    }

    static class ProductPreviewSliderAdapterViewHolder extends SliderViewAdapter.ViewHolder {
        View itemView;
        ImageView imageView;

        public ProductPreviewSliderAdapterViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.productPreviewViewHolderImage);
            this.itemView = itemView;
        }
    }
}
