package com.example.jodernstore.activity;

import static com.example.jodernstore.Utils.vndFormatPrice;

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
import android.widget.TextView;
import android.widget.Toast;

import com.example.jodernstore.MainActivity;
import com.example.jodernstore.R;
import com.example.jodernstore.adapter.CartAdapter;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.fragment.ProductListFragment;
import com.example.jodernstore.model.BranchInfo;
import com.example.jodernstore.model.SharedCart;
import com.example.jodernstore.provider.BranchesProvider;
import com.example.jodernstore.provider.GeneralProvider;
import com.example.jodernstore.provider.SharedCartProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
    private ImageButton shareCartBtn;

    private SharedCart sharedCart;
    private String subTotalStr, shippingStr, totalStr;

    private int selectedAppointBranchId = -1;
    private String selectedAppointDateStr = "";

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
    }

    private void setEvents() {
        sharedCartBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });

        sharedCartOrderBtn.setOnClickListener(view -> {
            // TODO
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
    }

    @SuppressLint("SetTextI18n")
    private void getAndShowSharedCartInfo() {
        sharedCartLoadingWrapper.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        int sharedCartIdx = intent.getIntExtra("index", -1);
        String cartName = intent.getStringExtra("cart-name");
        int nMembers = intent.getIntExtra("no-members", 0);
        int nItems = intent.getIntExtra("no-items", 0);
        long subTotal = intent.getLongExtra("sub-total", 0);

        if (sharedCartIdx == -1) {
            MySnackbar.inforSnackbar(this, sharedCartInfoParentView, getString(R.string.error_message)).show();
            sharedCartLoadingWrapper.setVisibility(View.GONE);
            return;
        }

        cartInfoName.setText(cartName);
        cartInfoQuantity.setText(Integer.toString(nItems));
        cartInfoNoMembers.setText(Integer.toString(nMembers));
        sharedCartSubTotalText.setText(vndFormatPrice(subTotal));

        sharedCart = SharedCartProvider.getInstance().getSharedCartItem(sharedCartIdx);

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