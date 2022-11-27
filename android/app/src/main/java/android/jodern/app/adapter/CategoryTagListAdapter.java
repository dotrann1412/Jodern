package android.jodern.app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.jodern.app.ProductListActivity;
import android.jodern.app.R;
import android.jodern.app.model.Category;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class CategoryTagListAdapter extends RecyclerView.Adapter<CategoryTagListAdapter.ViewHolder> {
    private ArrayList<Category> categoryList;
    private final AppCompatActivity mContext;

    public CategoryTagListAdapter(AppCompatActivity mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_tag_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.btn.setText(category.getName());
    }

    @Override
    public int getItemCount() {
        return (categoryList != null) ? categoryList.size() : 0;
    }

    public void setCategoryList(ArrayList<Category> categoryList) {
        this.categoryList = categoryList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialButton btn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            btn = itemView.findViewById(R.id.categoryTagWrapper);
            setEvents();
        }

        public void setEvents() {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Category category = categoryList.get(getAdapterPosition());
                    Intent intent = new Intent(mContext, ProductListActivity.class);
                    intent.putExtra("entry", "product-list");
                    intent.putExtra("categoryRaw", category.getRaw());
                    intent.putExtra("categoryName", category.getName());
                    intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // Adds the FLAG_ACTIVITY_NO_HISTORY flag
                    mContext.startActivity(intent);
                    mContext.overridePendingTransition( 0, 0);
                    mContext.finish();
                }
            });
        }
    }
}
