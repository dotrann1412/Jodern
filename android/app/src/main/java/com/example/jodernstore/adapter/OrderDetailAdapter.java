package com.example.jodernstore.adapter;

import static com.example.jodernstore.Utils.vndFormatPrice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jodernstore.R;
import com.example.jodernstore.activity.ProductDetailActivity;
import com.example.jodernstore.model.CartItem;
import com.example.jodernstore.model.Product;

import java.util.List;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {
    private final Context context;
    private List<CartItem> cartItemList;

    public OrderDetailAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflater = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_detail_product_item, parent, false);
        return new ViewHolder(inflater);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        CartItem item = cartItemList.get(position);
        Product product = item.getProduct();

        holder.name.setText(product.getName());
        holder.price.setText(vndFormatPrice(product.getPrice()));
        holder.count.setText(String.valueOf(item.getQuantity()));
        holder.size.setText("Size " + item.getSize());
        Glide.with(context)
                .load(product.getFirstImageURL())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.item_placeholder)
                .into(holder.image);
    }

    public void setCartItems(List<CartItem> cartItemList) {
        this.cartItemList = cartItemList;
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout wrapper;
        private ImageView image;
        private TextView name;
        private TextView price;
        private TextView size;
        private TextView count;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
            setEvents();
        }

        private void initViews() {
            wrapper = itemView.findViewById(R.id.orderDetailItemWrapper);
            image = itemView.findViewById(R.id.orderDetailItemImage);
            name = itemView.findViewById(R.id.orderDetailItemName);
            price = itemView.findViewById(R.id.orderDetailItemPrice);
            size = itemView.findViewById(R.id.orderDetailItemSize);
            count = itemView.findViewById(R.id.orderDetailItemCount);
        }

        private void setEvents() {
            wrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Product productItem = cartItemList.get(getAdapterPosition()).getProduct();
                    Intent intent = new Intent(context, ProductDetailActivity.class);
                    intent.putExtra("productId", productItem.getId());
                    context.startActivity(intent);
                }
            });
        }
    }
}
