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

import java.util.ArrayList;

public class CategoryTagListAdapter extends RecyclerView.Adapter<CategoryTagListAdapter.ViewHolder> {
    private ArrayList<Category> categoryList;
    private final AppCompatActivity mContext;
    private final RecyclerView recyclerView;
    private final String currentCategory;

    public CategoryTagListAdapter(AppCompatActivity mContext, RecyclerView view, String currentCategory) {
        this.mContext = mContext;
        this.recyclerView = view;
        this.currentCategory = currentCategory;
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
        holder.name.setText(category.getName());
        if (category.getRaw().equals(currentCategory)) {
            recyclerView.scrollToPosition(holder.getAdapterPosition());
            holder.wrapper.setBackgroundResource(R.drawable.shape_cate_tag_selected);
            holder.name.setTextColor(mContext.getColor(R.color.white));
        }
    }

    @Override
    public int getItemCount() {
        return (categoryList != null) ? categoryList.size() : 0;
    }

    public void setCategoryList(ArrayList<Category> categoryList) {
        this.categoryList = categoryList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final LinearLayout wrapper;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.categoryTagName);
            wrapper = itemView.findViewById(R.id.categoryTagWrapper);

            setEvents();
        }

        public void setEvents() {
            wrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Category category = categoryList.get(getAdapterPosition());
                    Intent intent = new Intent(mContext, ProductListActivity.class);
                    intent.putExtra("category", category.getRaw());
                    mContext.startActivity(intent);
                    mContext.overridePendingTransition( 0, 0);
                }
            });
        }
    }
}
