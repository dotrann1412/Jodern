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
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.jodernstore.BuildConfig;
import com.bumptech.glide.Glide;
import com.example.jodernstore.MainActivity;
import com.example.jodernstore.R;
import com.example.jodernstore.adapter.CartAdapter;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.fragment.ProductListFragment;
import com.example.jodernstore.model.BranchInfo;
import com.example.jodernstore.model.CartItem;
import com.example.jodernstore.model.Product;
import com.example.jodernstore.model.SharedCart;
import com.example.jodernstore.provider.BranchesProvider;
import com.example.jodernstore.provider.GeneralProvider;
import com.example.jodernstore.provider.SharedCartProvider;
import com.google.android.gms.common.internal.BaseGmsClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
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
    private TextView cartInfoName, cartInfoQuantity, cartInfoNoMembers;
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

    private List<String> logs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_cart);

        initViews();

        getAndShowSharedCartInfo();

        setEvents();
    }

    private void initViews() {
        sharedCartInfoRecyclerView = findViewById(R.id.sharedCartInfoRecyclerView);
        cartInfoName = findViewById(R.id.cartInfoName);
        cartInfoQuantity = findViewById(R.id.cartInfoQuantity);
        cartInfoNoMembers = findViewById(R.id.cartInfoNoMembers);
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

            // TODO history

            for (int i = 0; i < 30; ++i) {
                TextView tv = (TextView) getLayoutInflater().inflate(R.layout.history_item, null);
                tv.setText("Log " + i);
                cartHistoryLayoutWrapper.addView(tv);
            }


            dialog.show();
        });
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

    @SuppressLint("SetTextI18n")
    private void getAndShowSharedCartInfo() {
        sharedCartLoadingWrapper.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        String cartId = intent.getStringExtra("cartId");

        // TODO: call API
        // ...

//        List<Long> pseudoId = new ArrayList<>();
//        pseudoId.add(123L);
//        pseudoId.add(125L);
//
//        Log.d(TAG, "handleResponse: check point 2");
//
//        ArrayList<String> urls = new ArrayList<>();
//        urls.add("https://bizweb.sapocdn.net/100/438/408/products/vnk5274-hog-5.jpg?v=1663816469000");
//        Product pseudoProd = new Product(
//                141L,
//                "Đầm Bé Gái In Thỏ Cột Nơ",
//                urls,
//                174300L,
//                "",
//                "vay-nu",
//                "vay-nu",
//                new Integer[]{1, 1, 2, 2, 1}
//        );
//        Log.d(TAG, "handleResponse: check point 3");
//
//        List<CartItem> items = new ArrayList<>();
//        items.add(new CartItem(pseudoProd, 1, "XL"));
//        items.add(new CartItem(pseudoProd, 2, "L"));
//        items.add(new CartItem(pseudoProd, 2, "XL"));
//        items.add(new CartItem(pseudoProd, 1, "M"));
//        items.add(new CartItem(pseudoProd, 1, "S"));
//
//        ArrayList<String> history = new ArrayList<>();
//        history.add("Member 1 thêm sản phẩm 1");
//        history.add("Member 2 thêm sản phẩm 1");
//        history.add("Member 3 thêm sản phẩm 1");
//
//        sharedCart = new SharedCart("asd123", "Shared Cart 1", 100000L, items.size(), 5, "Hoàng Trọng Vũ", "https://i.pinimg.com/736x/89/90/48/899048ab0cc455154006fdb9676964b3.jpg", items, history);
        try {
            String entry = "shared-cart";
            JSONObject params = new JSONObject();
            params.put("cartid", cartId);
            Log.d(TAG, "getAndShowSharedCartInfo: " + cartId);
            String url = BuildConfig.SERVER_URL + entry;
            String jwt = GeneralProvider.with(SharedCartActivity.this).getJWT();
            JsonObjectRequest postRequest = new JsonObjectRequest(
                    url,
                    new JSONObject(),
                    this::handleResponse,
                    error -> {
                        sharedCartLoadingWrapper.setVisibility(View.GONE);
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
            Log.e(TAG, "getAndShowSharedCartInfo: " + e.getMessage());
            MySnackbar.inforSnackbar(SharedCartActivity.this, sharedCartInfoParentView, getString(R.string.error_message)).show();
        }

    }

    private void handleResponse(JSONObject response) {
        Log.d(TAG, "handleResponse: " + response.toString());
        sharedCart = null; // assign sharedCart

        // TODO: set holder name and avatar

        CartAdapter adapter = new CartAdapter(this, sharedCart, this::updateTotalPrice);
        sharedCartInfoRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        sharedCartInfoRecyclerView.setLayoutManager(layoutManager);
        showCartLayout(sharedCart.getItems().isEmpty());

        sharedCartLoadingWrapper.setVisibility(View.GONE);
    }

    private void updateTotalPrice() {
        Long subTotal = sharedCart.getTotal();
        subTotalStr = vndFormatPrice(subTotal);
        totalStr = vndFormatPrice(subTotal + SHIPPING_FEE);
        shippingStr = vndFormatPrice(SHIPPING_FEE);

        sharedCartSubTotalText.setText(subTotalStr);

        if (subTotal.equals(0L)) {
            showCartLayout(true);
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
                System.out.println("selected branch id: " + selectedAppointBranchId);
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

}