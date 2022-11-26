package android.jodern.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.jodern.app.ProductDetailActivity;
import android.jodern.app.R;
import android.jodern.app.model.Product;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolder> {
//    private ArrayList<ProductListItem> productList;
    private ArrayList<Product> productList;

    private final Context mContext;

    public ProductListAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.name.setText("Áo đẹp nha nha nha áo rất là đẹp");
        holder.image.setImageResource(R.drawable.demo); // just demo
        holder.price.setText("1.000.000 VNĐ");
        // TODO: get image from URL

    }

    @Override
    public int getItemCount() {
        return (productList != null) ? productList.size() : 0;
    }

    public void setProductList(ArrayList<Product> productList) {
        this.productList = productList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView image;
        private final TextView name;
        private final TextView price;
        private final LinearLayout wrapper;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.productListItemImage);
            name = itemView.findViewById(R.id.productListItemName);
            price = itemView.findViewById(R.id.productListItemPrice);
            wrapper = itemView.findViewById(R.id.productListItemWrapper);

            setEvents();
        }

        public void setEvents() {
            wrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Product productItem = productList.get(getAdapterPosition());
                    Intent intent = new Intent(mContext, ProductDetailActivity.class);
                    intent.putExtra("productId", productItem.getId());
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
