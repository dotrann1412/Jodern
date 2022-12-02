package com.example.jodern.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.jodern.R;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;

public class ProductSliderAdapter extends SliderViewAdapter<ProductSliderAdapter.SliderAdapterViewHolder> {
    private final Context context;
    private ArrayList<String> itemUrls;

    public ProductSliderAdapter(Context context) {
        this.context = context;
    }

    public void setItems(ArrayList<String> itemUrls) {
        this.itemUrls = itemUrls;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        this.itemUrls.remove(position);
        notifyDataSetChanged();
    }

    public void addItem(String url) {
        this.itemUrls.add(url);
        notifyDataSetChanged();
    }

    @Override
    public SliderAdapterViewHolder onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_detail_image_item, null);
        return new SliderAdapterViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterViewHolder viewHolder, final int position) {
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

    class SliderAdapterViewHolder extends SliderViewAdapter.ViewHolder {
        View itemView;
        ImageView imageView;

        public SliderAdapterViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.detailImage);
            this.itemView = itemView;
        }
    }

}