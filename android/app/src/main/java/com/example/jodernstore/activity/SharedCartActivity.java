package com.example.jodernstore.activity;

import static com.example.jodernstore.Utils.vndFormatPrice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.JsonObjectRequest;
import com.example.jodernstore.BuildConfig;
import com.bumptech.glide.Glide;
import com.example.jodernstore.MainActivity;
import com.example.jodernstore.R;
import com.example.jodernstore.adapter.SharedCartAdapter;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.fragment.ProductListFragment;
import com.example.jodernstore.interfaces.ChangeNumItemsListener;
import com.example.jodernstore.model.BranchInfo;
import com.example.jodernstore.model.SharedCart;
import com.example.jodernstore.provider.BranchesProvider;
import com.example.jodernstore.provider.GeneralProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedCartActivity extends AppCompatActivity {

    private static final String TAG = "SharedCartActivity";
    private static final long SHIPPING_FEE = 30000;
    private RecyclerView sharedCartInfoRecyclerView;
    private LinearLayout sharedCartLayoutParentView;
    private TextView sharedCartName, sharedCartNumItems, sharedCartNumMembers;
    private LinearLayout sharedCartSummaryWrapper;
    private MaterialButton sharedCartOrderBtn, sharedCartAppointBtn;
    private TextView sharedCartSubTotalText;
    private LinearLayout sharedCartEmptyWrapper;
    private MaterialButton sharedCartGoToShop;
    private ImageButton sharedCartBackBtn;
    private LinearLayout sharedCartLoadingWrapper;
    private LinearLayout sharedCartInfoParentView;
    private LinearLayout sharedCartLayout;
    private ImageButton shareCartBtn, sharedCartHistoryBtn;
    private RoundedImageView sharedCartHolderAvatar;
    private TextView sharedCartHolderName;

    private SharedCart sharedCart;
    private String subTotalStr, shippingStr, totalStr;

    private int selectedAppointBranchId = -1;
    private String selectedAppointDateStr = "";

    private boolean shouldFetchData = false;    // prevent fetching data twice when activity is started

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_cart);

        initViews();
        getAndShowSharedCartInfo();
        setEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!shouldFetchData) {
            shouldFetchData = true;
            return;
        }
        getAndShowSharedCartInfo();
    }

    private void initViews() {
        sharedCartInfoRecyclerView = findViewById(R.id.sharedCartInfoRecyclerView);
        sharedCartLayoutParentView = findViewById(R.id.sharedCartLayoutParentView);
        sharedCartName = findViewById(R.id.sharedCartName);
        sharedCartNumItems = findViewById(R.id.sharedCartNumItems);
        sharedCartNumMembers = findViewById(R.id.sharedCartNumMembers);
        sharedCartSummaryWrapper = findViewById(R.id.sharedCartSummaryWrapper);
        sharedCartBackBtn = findViewById(R.id.sharedCartBackBtn);
        sharedCartOrderBtn = findViewById(R.id.sharedCartOrderBtn);
        sharedCartAppointBtn = findViewById(R.id.sharedCartAppointBtn);
        sharedCartSubTotalText = findViewById(R.id.sharedCartSubTotalText);
        sharedCartEmptyWrapper = findViewById(R.id.sharedCartEmptyWrapper);
        sharedCartGoToShop = findViewById(R.id.sharedCartGoToShop);
        sharedCartLoadingWrapper = findViewById(R.id.sharedCartLoadingWrapper);
        sharedCartInfoParentView = findViewById(R.id.sharedCartInfoParentView);
        sharedCartLayout = findViewById(R.id.sharedCartLayout);
        shareCartBtn = findViewById(R.id.shareCartBtn);
        sharedCartHolderAvatar = findViewById(R.id.sharedCartHolderAvatar);
        sharedCartHolderName = findViewById(R.id.sharedCartHolderName);
        sharedCartHistoryBtn = findViewById(R.id.sharedCartHistoryBtn);
    }

    private void setEvents() {
        sharedCartBackBtn.setOnClickListener(view -> {
            onBackPressed();
            finish();
        });

        sharedCartOrderBtn.setOnClickListener(view -> {
            showSummaryDialog();
        });

        sharedCartAppointBtn.setOnClickListener(view -> {
            showAppointDialog();
        });

        sharedCartGoToShop.setOnClickListener(view -> {
            // Back pressed handling
            Intent searchIntent = new Intent(SharedCartActivity.this, ProductListFragment.class);
            searchIntent.putExtra("entry", "product-list");
            searchIntent.putExtra("sex", "nam");
            searchIntent.putExtra("categoryName", "Thời trang nam");
            GeneralProvider.with(SharedCartActivity.this).setSearchIntent(searchIntent);

            // Go to product list fragment of main activity
            Intent intent = new Intent(SharedCartActivity.this, MainActivity.class);
            intent.putExtra("nextFragment", ProductListFragment.TAG);
            intent.putExtra("entry", "product-list");
            intent.putExtra("sex", "nam");
            intent.putExtra("categoryName", "Thời trang nam");
            startActivity(intent);
        });

        shareCartBtn.setOnClickListener(view -> {
            Log.d(TAG, "setEvents: 1");
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_share_code);

            Log.d(TAG, "setEvents: 2");

            // Init views
            TextView shareCodeText = dialog.findViewById(R.id.shareCodeText);
            ImageButton copyToClipboardBtn = dialog.findViewById(R.id.copyToClipboardBtn);

            if (shareCodeText == null) {
                Log.e(TAG, "setEvents: -1");
                return;
            }

            // Set data
            shareCodeText.setText(sharedCart.getShareCode());

            // Set events
            copyToClipboardBtn.setOnClickListener(view2 -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("share-code", sharedCart.getShareCode());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Mã đã được sao chép vào bộ nhớ tạm", Toast.LENGTH_SHORT).show();
            });

            Log.d(TAG, "setEvents: 3");

            dialog.show();
        });

        sharedCartHistoryBtn.setOnClickListener(view -> {
            Log.d(TAG, "setEvents: sharedCartHistoryBtn");
            final Dialog dialog = new Dialog(SharedCartActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.dialog_cart_history);

            // Init views
            LinearLayout cartHistoryLayoutWrapper = dialog.findViewById(R.id.cartHistoryLayoutWrapper);

            List<String> logs = sharedCart.getHistory();

            if (logs.size() == 0) {
                TextView textView = (TextView) getLayoutInflater().inflate(R.layout.history_item, null);
                textView.setText("Giỏ hàng này chưa có thay đổi gì");
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                textView.setTypeface(null, Typeface.BOLD);
                cartHistoryLayoutWrapper.addView(textView);

                textView = (TextView) getLayoutInflater().inflate(R.layout.history_item, null);
                textView.setText("Bạn sẽ nhìn thấy lịch sử thay đổi \n của giỏ hàng này tại đây");
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                textView.setTypeface(null, Typeface.ITALIC);
                cartHistoryLayoutWrapper.addView(textView);

                dialog.show();
                return;
            }

            for (String log : logs) {
                TextView textView = (TextView) getLayoutInflater().inflate(R.layout.history_item, null);
                textView.setText("• " + log);
                cartHistoryLayoutWrapper.addView(textView);
            }

            dialog.show();
        });
    }

    @SuppressLint("SetTextI18n")
    private void getAndShowSharedCartInfo() {
        sharedCartLayoutParentView.setVisibility(View.GONE);
        sharedCartLoadingWrapper.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        String cartId = intent.getStringExtra("cartId");

        try {
            String entry = "shared-cart";
            JSONObject params = new JSONObject();
            params.put("cartid", cartId);
            String url = BuildConfig.SERVER_URL + entry + "/";
            String jwt = GeneralProvider.with(SharedCartActivity.this).getJWT();
            JsonObjectRequest postRequest = new JsonObjectRequest(
                    url,
                    params,
                    this::handleResponse,
                    error -> {
                        sharedCartLayoutParentView.setVisibility(View.VISIBLE);
                        sharedCartLoadingWrapper.setVisibility(View.GONE);
                        showCartLayout(sharedCart.getItems().isEmpty());
                        MySnackbar.inforSnackbar(SharedCartActivity.this, sharedCartInfoParentView, getString(R.string.error_message)).show();
                    }
            ) {
                @NonNull
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    params.put("Access-token", jwt);
                    return params;
                }
            };
            GeneralProvider.with(this).addToRequestQueue(postRequest);
        } catch (Exception e) {
            MySnackbar.inforSnackbar(SharedCartActivity.this, sharedCartInfoParentView, getString(R.string.error_message)).show();
        }

    }

    private void handleResponse(JSONObject response) {
        try {
            sharedCart = SharedCart.parseFullJson(response);

            // basic infor
            sharedCartName.setText(sharedCart.getName());
            sharedCartNumItems.setText(String.valueOf(sharedCart.getNumItems()));
            sharedCartNumMembers.setText(String.valueOf(sharedCart.getNumMembers()));

            // holder
            String avatar = sharedCart.getHolderAvatar();
            if (avatar != null && !avatar.equals("")) {
                Glide.with(this).load(avatar).into(sharedCartHolderAvatar);
            }
            sharedCartHolderName.setText(sharedCart.getHolderName());

            // items
            SharedCartAdapter adapter = new SharedCartAdapter(this, sharedCart, new ChangeNumItemsListener() {
                @Override
                public void onChanged() {
                    getAndShowSharedCartInfo();
                }
            });

            sharedCartInfoRecyclerView.setAdapter(adapter);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            sharedCartInfoRecyclerView.setLayoutManager(layoutManager);
            showCartLayout(sharedCart.getItems().isEmpty());

            // summary
            sharedCartSubTotalText.setText(vndFormatPrice(sharedCart.getTotal()));

            subTotalStr = vndFormatPrice(sharedCart.getTotal());
            shippingStr = vndFormatPrice(30000L);
            totalStr = vndFormatPrice(sharedCart.getTotal() + 30000L);

            sharedCartLayoutParentView.setVisibility(View.VISIBLE);
            sharedCartLoadingWrapper.setVisibility(View.GONE);
        } catch (Exception e) {
            sharedCartLayoutParentView.setVisibility(View.VISIBLE);
            sharedCartLoadingWrapper.setVisibility(View.GONE);
            MySnackbar.inforSnackbar(SharedCartActivity.this, sharedCartInfoParentView, getString(R.string.error_message)).show();
        }
    }

    private void showCartLayout(boolean isEmpty) {
        if (isEmpty) {
            sharedCartEmptyWrapper.setVisibility(View.VISIBLE);
            sharedCartLayout.setVisibility(View.GONE);
        } else {
            sharedCartEmptyWrapper.setVisibility(View.GONE);
            sharedCartLayout.setVisibility(View.VISIBLE);
        }
    }

    private void showAppointDialog() {
        final Dialog dialog = new Dialog(SharedCartActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_cart_appoint);

        // Init views
        MaterialButton chooseDateBtn = dialog.findViewById(R.id.cartAppointChooseDateBtn);
        MaterialButton checkoutBtn = dialog.findViewById(R.id.cartAppointCheckoutBtn);
        TextView selectedDate = dialog.findViewById(R.id.cartAppointSelectedDate);
        MaterialSpinner spinner = dialog.findViewById(R.id.cartAppointSpinner);
        LinearLayout dialogParentView = dialog.findViewById(R.id.cartSummaryDialogWrapper);

        // Set branches
        ArrayList<BranchInfo> branchInfos = BranchesProvider.getInstance().getBranches();
        ArrayList<String> branch_names = new ArrayList<>();
        for (BranchInfo branchInfo : branchInfos) {
            branch_names.add(branchInfo.getBranchName());
        }
        spinner.setItems(branch_names);
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                selectedAppointBranchId = position + 1;
            }
        });

        // Date
        MaterialDatePicker.Builder materialDateBuilder = MaterialDatePicker.Builder.datePicker();
        materialDateBuilder.setTitleText("Chọn ngày hẹn");
        MaterialDatePicker materialDatePicker = materialDateBuilder.build();

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            // selection to date object
            String dateString = DateFormat.format("dd-MM-yyyy", new Date((Long) selection)).toString();
            selectedDate.setText(dateString);
            selectedAppointDateStr = dateString;
        });

        // Set events
        checkoutBtn.setOnClickListener(v -> {
            // validate
            if (selectedAppointBranchId == -1) {
                MySnackbar.inforSnackbar(SharedCartActivity.this, dialog.getWindow().getDecorView(), "Bạn vui lòng chọn chi nhánh nhé!").show();
                return;
            }

            if (selectedAppointDateStr.length() == 0) {
                MySnackbar.inforSnackbar(SharedCartActivity.this, dialog.getWindow().getDecorView(), "Bạn vui lòng chọn ngày hẹn nhé!").show();
                return;
            }

            // appoints date must be after current date at least 3 day
            Date currentDate = new Date();
            SimpleDateFormat sourceFormat = new SimpleDateFormat("dd-MM-yyyy");
            try {
                Date appointDate = sourceFormat.parse(selectedAppointDateStr);
                long diff = appointDate.getTime() - currentDate.getTime();
                long diffDays = diff / (24 * 60 * 60 * 1000);
                if (diffDays < 1) {
                    MySnackbar.inforSnackbar(SharedCartActivity.this, dialog.getWindow().getDecorView(), "Ngày hẹn phải cách ngày hiện tại ít nhất\n2 ngày").show();
                    return;
                }
            } catch (ParseException e) {
                e.printStackTrace();
                return;
            }

            dialog.dismiss();
            Intent intent = new Intent(SharedCartActivity.this, OrderFormActivity.class);
            intent.putExtra("orderType", 1);
            intent.putExtra("cartId", sharedCart.getId());
            intent.putExtra("branchId", selectedAppointBranchId);
            intent.putExtra("date", selectedAppointDateStr);
            // this is self cart, so we don't need to pass cart id
            startActivity(intent);
            selectedAppointDateStr = "";
            selectedAppointBranchId = -1;
        });

        chooseDateBtn.setOnClickListener(v -> materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER"));

        dialog.show();
    }


    private void showSummaryDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_cart_summary);

        // Init views
        TextView subTotalText = dialog.findViewById(R.id.cartSummarySubTotalText);
        TextView shippingText = dialog.findViewById(R.id.cartSummaryShippingText);
        TextView totalText = dialog.findViewById(R.id.cartSummaryTotalText);
        MaterialButton checkoutBtn = dialog.findViewById(R.id.cartSummaryCheckoutBtn);

        // Set text
        subTotalText.setText(subTotalStr);
        shippingText.setText(shippingStr);
        totalText.setText(totalStr);

        // Set events
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(SharedCartActivity.this, OrderFormActivity.class);
                intent.putExtra("orderType", 0);
                intent.putExtra("cartId", sharedCart.getId());
                // this is self cart, so we don't need to pass cart id
                startActivity(intent);
            }
        });

        dialog.show();
    }

}