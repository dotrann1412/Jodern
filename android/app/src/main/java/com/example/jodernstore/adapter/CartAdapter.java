package com.example.jodernstore.adapter;

import static com.example.jodernstore.Utils.vndFormatPrice;

import android.annotation.SuppressLint;
import android.content.Context;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jodernstore.R;
import com.example.jodernstore.activity.ProductDetailActivity;
import com.example.jodernstore.fragment.CartFragment;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jodernstore.cart.CartController;
import com.example.jodernstore.cart.cartitem.CartItem;
import com.example.jodernstore.interfaces.ChangeNumItemsListener;
import com.example.jodernstore.model.Product;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private static final String TAG = CartAdapter.class.getName();

    private final List<CartItem> cartItemList;
    private final List<Product> productList;
    private final ChangeNumItemsListener changeNumItemsListener;

    private final CartController cartController;

    private final Context context;


    public CartAdapter(CartController cartController, Context context, ChangeNumItemsListener changeNumItemsListener) {
        this.cartItemList = cartController.getCartList();
        this.productList = cartController.getProductList();
        this.cartController = cartController;
        this.changeNumItemsListener = changeNumItemsListener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflater = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
        return new ViewHolder(inflater);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Product product = productList.get(position);
        CartItem item = cartItemList.get(position);

        holder.itemName.setText(product.getName());
        holder.itemPrice.setText(vndFormatPrice(product.getPrice()));
        holder.itemQuantity.setText(String.valueOf(item.getQuantity()));
        holder.itemSize.setText("Size " + item.getSize());
        Glide.with(context)
                .load(productList.get(position).getFirstImageURL())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.item_placeholder)
                .into(holder.itemImage);
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemPrice, itemSize, itemQuantity;
        ImageView itemImage, itemRemoveBtn;
        ImageButton itemIncBtn, itemDecBtn;
        LinearLayout itemWrapper;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.cartViewHolderName);
            itemPrice = itemView.findViewById(R.id.cartViewHolderPrice);
            itemSize = itemView.findViewById(R.id.cartViewHolderSize);
            itemQuantity = itemView.findViewById(R.id.cartViewHolderQuantity);
            itemImage = itemView.findViewById(R.id.cartViewHolderImage);
            itemRemoveBtn = itemView.findViewById(R.id.cartViewHolderRemoveBtn);
            itemIncBtn = itemView.findViewById(R.id.cartViewHolderIncBtn);
            itemDecBtn = itemView.findViewById(R.id.cartViewHolderDecBtn);
            itemWrapper = itemView.findViewById(R.id.cartViewHolderWrapper);
            setEvents();
        }

        private void setEvents() {
            itemIncBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // update on the database
                    cartController.increaseNumItems(getAdapterPosition(), new ChangeNumItemsListener() {
                        @Override
                        public void onChanged() {
                            notifyDataSetChanged();
                            changeNumItemsListener.onChanged();
                        }
                    });

                    // update UI
                    int quantity = Integer.parseInt(itemQuantity.getText().toString());
                    quantity++;
                    itemQuantity.setText(String.valueOf(quantity));
                }
            });

            itemDecBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cartController.decreaseNumItems(getAdapterPosition(), new ChangeNumItemsListener() {
                        @Override
                        public void onChanged() {
                            notifyDataSetChanged();
                            changeNumItemsListener.onChanged();
                        }
                    });

                    // update UI
                    int quantity = Integer.parseInt(itemQuantity.getText().toString());
                    if (quantity > 1) {
                        quantity--;
                        itemQuantity.setText(String.valueOf(quantity));
                    }
                }
            });

            itemRemoveBtn.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onClick(View view) {
                    cartController.deleteItem(getAdapterPosition(), new ChangeNumItemsListener() {
                        @Override
                        public void onChanged() {
                            notifyDataSetChanged();
                            changeNumItemsListener.onChanged();
                        }
                    });
                }
            });

            itemWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Product productItem = productList.get(getAdapterPosition());
                    Intent intent = new Intent(context, ProductDetailActivity.class);
                    intent.putExtra("productId", productItem.getId());
                    intent.putExtra("previousFragment", CartFragment.TAG);
                    context.startActivity(intent);
                }
            });
        }
    }
}
