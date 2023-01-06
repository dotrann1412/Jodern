package com.example.jodernstore.adapter;

import static com.example.jodernstore.Utils.vndFormatPrice;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.jodernstore.BuildConfig;
import com.example.jodernstore.R;
import com.example.jodernstore.activity.ProductDetailActivity;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.fragment.MyCartFragment;

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
import com.example.jodernstore.interfaces.ChangeNumItemsListener;
import com.example.jodernstore.model.Cart;
import com.example.jodernstore.model.CartItem;
import com.example.jodernstore.model.Product;
import com.example.jodernstore.model.SharedCart;
import com.example.jodernstore.provider.GeneralProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SharedCartAdapter extends RecyclerView.Adapter<SharedCartAdapter.ViewHolder> {
    private static final String TAG = SharedCartAdapter.class.getName();
    private SharedCart currentCart;
    private final ChangeNumItemsListener changeNumItemsListener;


    private final Context context;


    public SharedCartAdapter(Context context, SharedCart cart, ChangeNumItemsListener changeNumItemsListener) {
        currentCart = cart;
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
        CartItem item = currentCart.getItems().get(position);
        Product product = item.getProduct();

        holder.itemName.setText(product.getName());
        holder.itemPrice.setText(vndFormatPrice(product.getPrice()));
        holder.itemQuantity.setText(String.valueOf(item.getQuantity()));
        holder.itemSize.setText("Size " + item.getSize());
        Glide.with(context)
                .load(product.getFirstImageURL())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.item_placeholder)
                .into(holder.itemImage);
    }

    @Override
    public int getItemCount() {
        return currentCart.getItems().size();
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

        private void handleCallAPI(CartItem item, int addedQuantity) {
            try {
                String entry = "add-to-cart";
                JSONObject params = new JSONObject();
                params.put("productid", item.getProduct().getId());
                params.put("quantity", addedQuantity);
                params.put("sizeid", item.getSize());
                JSONArray cartIds = new JSONArray();
                cartIds.put(currentCart.getId());
                params.put("cartids", cartIds);

                // selected carts
                String url = BuildConfig.SERVER_URL + entry + "/";
                JsonObjectRequest postRequest = new JsonObjectRequest(
                        url,
                        params,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    changeNumItemsListener.onChanged();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                System.out.println(error.toString());
                            }
                        }
                ) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("Access-token", GeneralProvider.with(context).getJWT());
                        return params;
                    }
                };
                GeneralProvider.with(context).addToRequestQueue(postRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void setEvents() {
            itemIncBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    CartItem item = currentCart.getItems().get(position);
                    handleCallAPI(item, 1);
                }
            });

            itemDecBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    CartItem item = currentCart.getItems().get(position);

                    if (item.getQuantity() == 1)
                        return;

                    handleCallAPI(item, -1);
                }
            });

            itemRemoveBtn.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    CartItem item = currentCart.getItems().get(position);

                    handleCallAPI(item, -1000);
                }
            });

            itemWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Product productItem = currentCart.getItems().get(getAdapterPosition()).getProduct();
                    Intent intent = new Intent(context, ProductDetailActivity.class);
                    intent.putExtra("productId", productItem.getId());
                    context.startActivity(intent);
                }
            });
        }
    }
}
