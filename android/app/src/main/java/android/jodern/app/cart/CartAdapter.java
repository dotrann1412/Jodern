package android.jodern.app.cart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.jodern.app.interfaces.ChangeNumItemsListener;
import android.jodern.app.R;
import android.jodern.app.model.Product;
import android.jodern.app.cart.cartitem.CartItem;
import android.jodern.app.utils.StringUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private List<CartItem> cartItemList;
    private CartController cartController;
    private ChangeNumItemsListener changeNumItemsListener;

    public CartAdapter(List<CartItem> cartItemList, Context context, ChangeNumItemsListener changeNumItemsListener) {
        this.cartItemList = cartItemList;
        // TODO: change the orderItemList to load from the stored data
        this.cartController = CartController.with(context);
        this.changeNumItemsListener = changeNumItemsListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflater = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_viewholder, parent, false);

        return new ViewHolder(inflater);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        try {
            CartItem cartItem = cartItemList.get(position);

            // TODO: play with the API and try to convert to id, name, uri, price
            // query id and get product
            // Product product = Provider.getProduct(cartItem.getId());

            Product product = new Product();

            product.setId(0L);
            List<String> images = new ArrayList<>();
            images.add("https://bizweb.sapocdn.net/100/438/408/products/akn5040-den-4.jpg?v=1668244848000");
            images.add("https://bizweb.sapocdn.net/100/438/408/products/akn5040-den-5-308a032a-f9a4-4fb3-b73b-348e31c695db.jpg?v=1669013097000");
            product.setImages(images);
            product.setSex("nu");
            product.setCategory("ao-khoac-nu");
            product.setPrice(499000L);
            product.setName("Áo quần");

            holder.itemName.setText(product.getName());
            holder.itemCost.setText(StringUtils.long2money(product.getPrice()));
            holder.numItems.setText(String.valueOf(cartItem.getQuantity()));
            holder.itemSize.setText("Size " + cartItem.getSize());
//            Context itemViewContext = holder.itemView.getContext();
//            int imageResource = itemViewContext.getResources()
//                    .getIdentifier(product.getImages().get(0), null, itemViewContext.getPackageName());

            String imageUri = product.getImages().get(0);

            Glide.with(holder.itemView.getContext())
                    .load(imageUri)
                    .centerCrop()
                    .placeholder(R.drawable.item_placeholder)
                    .into(holder.itemImageUri);

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
        } catch (NullPointerException e) {
            Log.d("Cart Adapter", "Product list is empty");
        } catch (Exception e) {
            Log.d("Cart Adapter", e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        try {
            return cartItemList.size();
        } catch (Exception e) {
            Log.d("Cart Adapter", e.getMessage());
            return -1;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemCost, numItems, itemSize;
        ImageView itemImageUri, incItem, decItem, removeItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.cartViewHolderName);
            itemCost = itemView.findViewById(R.id.itemsCost);
            itemImageUri = itemView.findViewById(R.id.cartViewHolderImage);
            numItems = itemView.findViewById(R.id.cartNumItemsTextView);
            incItem = itemView.findViewById(R.id.cartItemIncrease);
            decItem = itemView.findViewById(R.id.cartItemDecrease);
            removeItem = itemView.findViewById(R.id.cartItemRemoveBtn);
            itemSize = itemView.findViewById(R.id.itemSize);
        }
    }
}
