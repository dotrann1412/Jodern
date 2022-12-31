package com.example.jodernstore.fragment;

import static com.example.jodernstore.Utils.vndFormatPrice;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.jodernstore.BuildConfig;
import com.example.jodernstore.MainActivity;
import com.example.jodernstore.R;
import com.example.jodernstore.activity.OrderFormActivity;
import com.example.jodernstore.adapter.CartAdapter;
import com.example.jodernstore.model.BranchInfo;
import com.example.jodernstore.model.CartItem;
import com.example.jodernstore.customwidget.MySnackbar;
import com.example.jodernstore.interfaces.ChangeNumItemsListener;
import com.example.jodernstore.model.Cart;
import com.example.jodernstore.provider.BranchesProvider;
import com.example.jodernstore.provider.CartProvider;
import com.example.jodernstore.provider.GeneralProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MyCartFragment extends Fragment {
    private final Long SHIPPING_FEE = 30000L;

    public static final String TAG = "MyCartFragment";
    private FrameLayout parentView;

    private int selectedAppointBranchId = -1;
    private String selectedAppointDateStr = "";

    private Cart currentCart;
    private boolean shouldCallUpdateAPI = false;
    private boolean shouldCallFetchAPI = false;

    private RecyclerView cartRecyclerView;
    private LinearLayout cartLayout, cartEmptyWrapper, cartLoadingWrapper;
    private TextView subTotalText;
    private String subTotalStr, shippingStr, totalStr;
    private MaterialButton cartOrderBtn, cartAppointBtn, cartGoToShop;

    public MyCartFragment() {
        // Required empty public constructor
        super(R.layout.fragment_my_cart);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
        setEvents();
        showInitialMessage();
        getAndShowCartItems();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shouldCallFetchAPI) {
            getAndShowCartItems();
        }
    }

    @Override
    public void onStop() {
        shouldCallFetchAPI = true;

        // Call API to update wishlist data (if necessary)
        if (shouldCallUpdateAPI) {
            System.out.println("updating cart");
            updateCartData();
        }
        super.onStop();
    }

    private void showInitialMessage() {
        Bundle bundle = getArguments();
        if (bundle == null)
            return;
        String message = bundle.getString("message");
        if (message == null)
            return;
        MySnackbar.inforSnackar(getContext(), parentView, message).show();
    }

    private void initViews() {
        parentView = getView().findViewById(R.id.cartParentView);
        cartRecyclerView = getView().findViewById(R.id.myCartRecyclerView);
        cartLayout = getView().findViewById(R.id.myCartLayout);
        subTotalText = getView().findViewById(R.id.myCartSubTotalText);
        cartEmptyWrapper = getView().findViewById(R.id.myCartEmptyWrapper);
        cartLoadingWrapper = getView().findViewById(R.id.myCartLoadingWrapper);
        cartOrderBtn = getView().findViewById(R.id.myCartOrderBtn);
        cartAppointBtn = getView().findViewById(R.id.myCartAppointBtn);
        cartGoToShop = getView().findViewById(R.id.myCartGoToShop);
    }


    private void setEvents() {
        cartOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSummaryDialog();
            }
        });

        cartAppointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAppointDialog();
            }
        });

        cartGoToShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Back pressed handling
                Intent searchIntent = new Intent(MyCartFragment.this.getContext(), ProductListFragment.class);
                searchIntent.putExtra("entry", "product-list");
                searchIntent.putExtra("sex", "nam");
                searchIntent.putExtra("categoryName", "Thời trang nam");
                GeneralProvider.with(MyCartFragment.this.getContext()).setSearchIntent(searchIntent);

                // Go to product list fragment of main activity
                Intent intent = new Intent(MyCartFragment.this.getContext(), MainActivity.class);
                intent.putExtra("nextFragment", ProductListFragment.TAG);
                intent.putExtra("entry", "product-list");
                intent.putExtra("sex", "nam");
                intent.putExtra("categoryName", "Thời trang nam");
                startActivity(intent);
            }
        });
    }

    private void showSummaryDialog() {
        final Dialog dialog = new Dialog(requireActivity());
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
                Intent intent = new Intent(requireActivity().getApplicationContext(), OrderFormActivity.class);
                intent.putExtra("orderType", 0);
                // this is self cart, so we don't need to pass cart id
                startActivity(intent);
            }
        });

        dialog.show();
    }


    private void showAppointDialog() {
        final Dialog dialog = new Dialog(getContext());
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
                selectedAppointBranchId = position;
            }
        });

        // Date
        MaterialDatePicker.Builder materialDateBuilder = MaterialDatePicker.Builder.datePicker();
        materialDateBuilder.setTitleText("Chọn ngày hẹn");
        MaterialDatePicker materialDatePicker = materialDateBuilder.build();

        materialDatePicker.addOnPositiveButtonClickListener(
                new MaterialPickerOnPositiveButtonClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onPositiveButtonClick(Object selection) {
                        // selection to date object
                        String dateString = DateFormat.format("dd-MM-yyyy", new Date((Long) selection)).toString();
                        selectedDate.setText(dateString);
                        selectedAppointDateStr = dateString;
                    }
                });

        // Set events
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // validate
                if (selectedAppointBranchId == -1) {
                    MySnackbar.inforSnackar(getContext(), dialog.getWindow().getDecorView(), "Bạn vui lòng chọn chi nhánh nhé!").show();
                    return;
                }

                if (selectedAppointDateStr.length() == 0) {
                    MySnackbar.inforSnackar(getContext(), dialog.getWindow().getDecorView(), "Bạn vui lòng chọn ngày hẹn nhé!").show();
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
                        MySnackbar.inforSnackar(getContext(), dialog.getWindow().getDecorView(), "Ngày hẹn phải cách ngày hiện tại ít nhất\n2 ngày").show();
                        return;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    return;
                }

                dialog.dismiss();
                Intent intent = new Intent(requireActivity().getApplicationContext(), OrderFormActivity.class);
                intent.putExtra("orderType", 1);
                intent.putExtra("branchid", selectedAppointBranchId);
                intent.putExtra("date", selectedAppointDateStr);
                // this is self cart, so we don't need to pass cart id
                startActivity(intent);
                selectedAppointDateStr = "";
                selectedAppointBranchId = -1;
            }
        });

        chooseDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDatePicker.show(getParentFragmentManager(), "MATERIAL_DATE_PICKER");
            }
        });

        dialog.show();
    }

    private void getAndShowCartItems() {
        currentCart = CartProvider.getInstance().getMyCart();
        cartLoadingWrapper.setVisibility(View.VISIBLE);

        // call API
        String entry = "cart";
        String url = BuildConfig.SERVER_URL + entry + "/";
        String jwt = GeneralProvider.with(getContext()).getJWT();
        JsonObjectRequest getRequest = new JsonObjectRequest (
                url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        handleResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        cartLoadingWrapper.setVisibility(View.GONE);
                        MySnackbar.inforSnackar(getContext(), parentView, getString(R.string.error_message)).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Access-token", jwt);
                return params;
            }
        };
        GeneralProvider.with(this.getContext()).addToRequestQueue(getRequest);
    }

    public void updateCartData() {
        try {
            String entry = "save-cart";
            JSONArray cartItemsJson = new JSONArray();
            for (CartItem item : currentCart.getItems()) {
                JSONObject cartItem = new JSONObject();
                cartItem.put("productid", item.getProduct().getId());
                cartItem.put("quantity", item.getQuantity());
                cartItem.put("size", item.getSize());
                cartItemsJson.put(cartItem);
            }
            JSONObject params = new JSONObject();
            params.put("data", cartItemsJson);
            String url = BuildConfig.SERVER_URL + entry + "/";
            String jwt = GeneralProvider.with(getContext()).getJWT();
            JsonObjectRequest postRequest = new JsonObjectRequest(
                    url,
                    params,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                System.out.println("Update cart successfully");
                            } catch (Exception e) {
                                showErrorMsg();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println(error.toString());
                            showErrorMsg();
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("Access-token", jwt);
                    return params;
                }
            };
            GeneralProvider.with(getContext()).addToRequestQueue(postRequest);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void handleResponse(JSONObject response) {
        shouldCallFetchAPI = false;
        cartLoadingWrapper.setVisibility(View.GONE);
        currentCart.clear();

        try {
            JSONArray items = (JSONArray)response.get("details");
            for (int j = 0; j < items.length(); j++) {
                CartItem item = CartItem.parseJSON((JSONObject)items.get(j));
                if (item != null) {
                    currentCart.addItem(item);
                }
            }
            updateTotalPrice();

            CartAdapter adapter = new CartAdapter(this.getContext(), currentCart, new ChangeNumItemsListener() {
                @Override
                public void onChanged() {
                    shouldCallUpdateAPI = true;
                    showCartLayout(currentCart.getItems().isEmpty());
                    updateTotalPrice();
                }
            });

            cartRecyclerView.setAdapter(adapter);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
            cartRecyclerView.setLayoutManager(layoutManager);
            showCartLayout(currentCart.getItems().isEmpty());
        }
        catch (Exception e) {
            showErrorMsg();
        }
    }

    private void updateTotalPrice() {
        Long subTotal = currentCart.getTotal();
        subTotalStr = vndFormatPrice(subTotal);
        totalStr = vndFormatPrice(subTotal + SHIPPING_FEE);
        shippingStr = vndFormatPrice(SHIPPING_FEE);

        subTotalText.setText(subTotalStr);

        if (subTotal.equals(0L)) {
            showCartLayout(true);
        }
    }

    private void showCartLayout(boolean isEmpty) {
        if (isEmpty) {
            cartEmptyWrapper.setVisibility(View.VISIBLE);
            cartLayout.setVisibility(View.GONE);
        } else {
            cartEmptyWrapper.setVisibility(View.GONE);
            cartLayout.setVisibility(View.VISIBLE);
        }
    }

    private void showErrorMsg() {
        MySnackbar.inforSnackar(getContext(), parentView, getString(R.string.error_message)).show();
    }
}