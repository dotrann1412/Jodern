package android.jodern.app.adapter;

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
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CategoryImageListAdapter extends RecyclerView.Adapter<CategoryImageListAdapter.ViewHolder> {
    private ArrayList<Category> categoryList;
    private final Context mContext;

    public CategoryImageListAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_image_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);

        holder.name.setText(category.getName());
        holder.image.setImageResource(category.getImageId());
    }

    @Override
    public int getItemCount() {
        return (categoryList != null) ? categoryList.size() : 0;
    }

    public void setCategoryList(ArrayList<Category> categoryList) {
        this.categoryList = categoryList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView image;
        private final TextView name;
        private final LinearLayout wrapper;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.categoryImageView);
            name = itemView.findViewById(R.id.categoryImageName);
            wrapper = itemView.findViewById(R.id.categoryImageWrapper);

            setEvents();
        }

        public void setEvents() {
            wrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Category category = categoryList.get(getAdapterPosition());
                    Intent intent = new Intent(mContext, ProductListActivity.class);
                    intent.putExtra("categoryRaw", category.getRaw());
                    intent.putExtra("categoryName", category.getName());
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
