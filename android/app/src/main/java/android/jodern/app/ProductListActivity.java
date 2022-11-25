package android.jodern.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.jodern.app.adapter.CategoryTagListAdapter;
import android.jodern.app.provider.Provider;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ProductListActivity extends AppCompatActivity {
    private String currentRawCategory = "";
    private TextView searchBarText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        initViews();

        // get intent data
        Intent intent = getIntent();
        String text = intent.getStringExtra("text");
        String rawCategory = intent.getStringExtra("category");
        if (text != null) {
            searchBarText.setText(text);
            searchBarText.setTextColor(getColor(R.color.primary));
            // TODO: Call API with text query
        }
        else {
            searchBarText.setText("Bạn tìm gì hôm nay");
            searchBarText.setTextColor(getColor(R.color.text_light));

            if (rawCategory != null) {
                currentRawCategory = rawCategory;
                // TODO: Call API with category
            }
            else {
                String base64String = intent.getStringExtra("image");
                // TODO: Call API with image query
            }
        }

        setupCategoryLists();
    }

    private void initViews() {
        searchBarText = findViewById(R.id.productSearchBarText);
    }

    private void setupCategoryLists() {
        // find views
        RecyclerView maleView = findViewById(R.id.productMaleCategoryTagList);
        RecyclerView femaleView = findViewById(R.id.productFemaleCategoryTagList);

        // category list for male
        CategoryTagListAdapter maleAdapter = new CategoryTagListAdapter(this, maleView, currentRawCategory);
        maleAdapter.setCategoryList(Provider.getInstance().getCategoryList("nam"));
        LinearLayoutManager maleLayout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        maleView.setLayoutManager(maleLayout);
        maleView.setAdapter(maleAdapter);

        // category list for female
        CategoryTagListAdapter femaleAdapter = new CategoryTagListAdapter(this, femaleView, currentRawCategory);
        femaleAdapter.setCategoryList(Provider.getInstance().getCategoryList("nu"));
        LinearLayoutManager femaleLayout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        femaleView.setLayoutManager(femaleLayout);
        femaleView.setAdapter(femaleAdapter);

        // highlight current category
    }


    public void onProductSearchBarClicked(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }
}