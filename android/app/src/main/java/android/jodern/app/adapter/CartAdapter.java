package android.jodern.app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.jodern.app.controller.CartController;
import android.jodern.app.interfaces.ChangeNumItemsListener;
import android.jodern.app.R;
import android.jodern.app.model.Garment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    private List<Garment> garmentList;
    private CartController cartController;
    private ChangeNumItemsListener changeNumItemsListener;

    public CartAdapter(List<Garment> garmentList, Context context, ChangeNumItemsListener changeNumItemsListener) {
        this.garmentList = garmentList;
        this.cartController = new CartController(context);
        this.changeNumItemsListener = changeNumItemsListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflater = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_viewholder, parent, false);

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        try {
            Garment garment = garmentList.get(position);
            holder.itemName.setText(garment.getName());
            holder.itemCost.setText(String.valueOf(garment.getCost()));
            holder.numItems.setText(String.valueOf(cartController.getOrderListSize()));

            Context itemViewContext = holder.itemView.getContext();
            int drawableResource = itemViewContext.getResources()
                    .getIdentifier(garment.getImages().get(0), "drawable", holder.itemView.getContext().getPackageName());


            holder.incItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //                cartController.increaseNumItems(garmentList, position, new ChangeNumItemsListener() {
                    //                    @Override
                    //                    public void onChanged() {
                    //                        changeNumItemsListener.onChanged();
                    //                    }
                    //                });
                }
            });

            holder.decItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //                cartController.decreaseNumItems(garmentList, position, new ChangeNumItemsListener() {
                    //                    @Override
                    //                    public void onChanged() {
                    //                        changeNumItemsListener.onChanged();
                    //                    }
                    //                });
                }
            });

            holder.removeItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //                cartManager.removeItem(garmentList, position, new ChangeNumItemsListener() {
                    //                    @Override
                    //                    public void onChanged() {
                    //                        changeNumItemsListener.onChanged();
                    //                    }
                    //                });
                }
            });
        } catch (NullPointerException e) {
            System.out.println("Garment list is empty");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        try {
            return garmentList.size();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemCost, numItems;
        ImageView itemUri, incItem, decItem, removeItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.cartViewHolderName);
            itemCost = itemView.findViewById(R.id.itemsCost);
            itemUri = itemView.findViewById(R.id.cartViewHolderImage);
            numItems = itemView.findViewById(R.id.cartNumItemsTextView);
            incItem = itemView.findViewById(R.id.cartItemIncrease);
            decItem = itemView.findViewById(R.id.cartItemDecrease);
            removeItem = itemView.findViewById(R.id.cartItemRemoveBtn);
        }
    }
}
