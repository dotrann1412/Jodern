package com.example.jodern.adapter;

import static com.example.jodern.Utils.vndFormatPrice;

import android.annotation.SuppressLint;
import android.content.Context;
import com.example.jodern.R;
import com.example.jodern.provider.Provider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.example.jodern.cart.CartController;
import com.example.jodern.cart.cartitem.CartItem;
import com.example.jodern.interfaces.ChangeNumItemsListener;
import com.example.jodern.model.Product;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private static final String TAG = CartAdapter.class.getName();
    private final List<CartItem> cartItemList;
    private final CartController cartController;
    private final ChangeNumItemsListener changeNumItemsListener;
    private final Context context;
    private Product product = null;

    // TODO: total injection
    private final List<Long> prices;

    public CartAdapter(List<CartItem> cartItemList, Context context, ChangeNumItemsListener changeNumItemsListener) {
        this.cartItemList = cartItemList;
        this.cartController = CartController.with(context);
        this.changeNumItemsListener = changeNumItemsListener;
        this.context = context;

        this.prices = new ArrayList<>();
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
        try {
            Log.d(TAG, "onBindViewHolder: binding view holder");
            CartItem cartItem = cartItemList.get(position);

            String url = "http://jodern.store:8000/api/product/" + String.valueOf(cartItem.getProductId());
            JsonObjectRequest stringRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseProductResponse(response);
                        initCartItemView(holder, cartItem);
                        Log.d(TAG, "onResponse: successful");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, context.getString(R.string.error_message), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onErrorResponse: VolleyError: " + error);
                    }
                }
            );
            Provider.with(context).addToRequestQueue(stringRequest);

            holder.incItem.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onClick(View view) {
                    cartController.increaseNumItems(cartItemList, position, new ChangeNumItemsListener() {
                        @Override
                        public void onChanged() {
                            notifyDataSetChanged();
                            changeNumItemsListener.onChanged();
                        }
                    });
                }
            });

            holder.decItem.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onClick(View view) {
                    cartController.decreaseNumItems(cartItemList, position, new ChangeNumItemsListener() {
                        @Override
                        public void onChanged() {
                            notifyDataSetChanged();
                            changeNumItemsListener.onChanged();
                        }
                    });
                }
            });

            holder.removeItem.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onClick(View view) {
                    cartController.deleteItem(cartItemList, position, new ChangeNumItemsListener() {
                        @Override
                        public void onChanged() {
                            notifyDataSetChanged();
                            changeNumItemsListener.onChanged();
                        }
                    });
                }
            });
            Log.d(TAG, "onBindViewHolder: bind view holder successfully");
        } catch (NullPointerException e) {
            Log.d(TAG, "NullPointerException: " + e.getMessage());
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @SuppressLint("SetTextI18n")
    private void initCartItemView(ViewHolder holder, CartItem cartItem) {
        if (product == null) {
            throw new NullPointerException("product get from API response is null");
        }
        holder.itemName.setText(product.getName());
        holder.itemCost.setText(vndFormatPrice(product.getPrice()));
        holder.numItems.setText(String.valueOf(cartItem.getQuantity()));
        holder.itemSize.setText("Size " + cartItem.getSize());

        Glide.with(context)
                .load(product.getFirstImageURL())
                .centerCrop()
                .placeholder(R.drawable.item_placeholder)
                .into(holder.itemImageUri);

        prices.add(product.getPrice());
    }

    private void parseProductResponse(JSONObject response) {
        Log.d(TAG, "parseProductResponse: parsing from API call");
        product = Product.parseJSON(response);
    }

    public Long getCartSubTotal() {
        Long result = 0L;
        for (Long price : prices) {
            result += price;
        }
        return result;
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemCost, numItems, itemSize;
        ImageView itemImageUri, incItem, decItem, removeItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
//            itemName = itemView.findViewById(R.id.cartViewHolderName);
//            itemCost = itemView.findViewById(R.id.itemsCost);
//            itemImageUri = itemView.findViewById(R.id.cartViewHolderImage);
//            numItems = itemView.findViewById(R.id.cartNumItemsTextView);
//            incItem = itemView.findViewById(R.id.cartItemIncrease);
//            decItem = itemView.findViewById(R.id.cartItemDecrease);
//            removeItem = itemView.findViewById(R.id.cartItemRemoveBtn);
//            itemSize = itemView.findViewById(R.id.itemSize);
        }
    }
}
