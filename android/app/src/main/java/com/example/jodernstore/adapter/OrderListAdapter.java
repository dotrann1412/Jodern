package com.example.jodernstore.adapter;

import static com.example.jodernstore.Utils.localDateToString;
import static com.example.jodernstore.Utils.vndFormatPrice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jodernstore.R;
import com.example.jodernstore.activity.OrderDetailActivity;
import com.example.jodernstore.model.Order;

import java.util.List;

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.ViewHolder> {
    private final Context context;
    private List<Order> orderList;

    public OrderListAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflater = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
        return new ViewHolder(inflater);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Order order = orderList.get(position);
        holder.orderID.setText(order.getId());
        holder.orderDate.setText(order.getCheckoutDate());
        holder.orderTotalPrice.setText(vndFormatPrice(order.getTotalPrice()));
        holder.orderCount.setText(order.getNumItems().toString());
        holder.orderType.setText(order.getType() == 0 ? "Đặt giao hàng" : "Hẹn thử đồ");
        holder.orderStatus.setText(!order.getStatus() ? "Chưa nhận hàng" : "Đã nhận hàng");
        holder.orderStatus.setTextColor(!order.getStatus() ? context.getResources().getColor(R.color.light_red) : context.getResources().getColor(R.color.light_green));
    }

    public void setOrderList(List<Order> orderList) {
        this.orderList = orderList;
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView orderID, orderDate, orderType, orderCount, orderTotalPrice, orderStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
            setEvents();
        }

        private void initViews() {
            orderID = itemView.findViewById(R.id.orderID);
            orderDate = itemView.findViewById(R.id.orderDate);
            orderType = itemView.findViewById(R.id.orderType);
            orderCount = itemView.findViewById(R.id.orderCount);
            orderTotalPrice = itemView.findViewById(R.id.orderTotalPrice);
            orderStatus = itemView.findViewById(R.id.orderStatus);
        }

        private void setEvents() {
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, OrderDetailActivity.class);
                intent.putExtra("orderID", orderID.getText().toString());
                context.startActivity(intent);
            });
        }
    }
}
