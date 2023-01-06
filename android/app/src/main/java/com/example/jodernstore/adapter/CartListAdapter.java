package com.example.jodernstore.adapter;

import static com.example.jodernstore.Utils.vndFormatPrice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jodernstore.R;
import com.example.jodernstore.activity.JoinedCartActivity;
import com.example.jodernstore.activity.SharedCartActivity;
import com.example.jodernstore.model.SharedCart;

import java.util.List;

public class CartListAdapter extends RecyclerView.Adapter<CartListAdapter.ViewHolder> {

    private static final String TAG = CartListAdapter.class.getName();
    private final List<SharedCart> sharedCartList;
    private final Context context;
    private boolean isJoined;

    public CartListAdapter (Context context, List<SharedCart> sharedCartList, boolean isJoined) {
        this.sharedCartList = sharedCartList;
        this.context = context;
        this.isJoined = isJoined;
    }


    @NonNull
    @Override
    public CartListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflater = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_cart_list, parent, false);
        return new CartListAdapter.ViewHolder(inflater);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CartListAdapter.ViewHolder holder, int position) {
        SharedCart sharedCart = sharedCartList.get(position);
        holder.cartName.setText(sharedCart.getName());
        holder.cartQuantity.setText(Integer.toString(sharedCart.getNumItems()));
        holder.cartMembers.setText(Integer.toString(sharedCart.getNoMembers()));
        holder.cartTotal.setText(vndFormatPrice(sharedCart.getTotal()));

        holder.cartWrapper.setOnClickListener(view -> {
            // TODO: this intent is to move to SharedCartActivity with extra params
            Intent intent;
            if (!isJoined) {
                intent = new Intent(context, SharedCartActivity.class);
            } else {
                intent = new Intent(context, JoinedCartActivity.class);
            }
            intent.putExtra("cartId", sharedCart.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return sharedCartList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView cartName, cartQuantity, cartTotal, cartMembers;
        LinearLayout cartWrapper;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cartWrapper = itemView.findViewById(R.id.sharedCartInfoWrapper);
            cartName = itemView.findViewById(R.id.cartListItemName);
            cartQuantity = itemView.findViewById(R.id.cartListItemQuantity);
            cartTotal = itemView.findViewById(R.id.cartListItemTotal);
            cartMembers = itemView.findViewById(R.id.cartListItemNoMembers);
        }
    }
}
