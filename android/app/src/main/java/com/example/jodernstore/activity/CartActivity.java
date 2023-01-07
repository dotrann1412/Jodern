package com.example.jodernstore.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jodernstore.MainActivity;
import com.example.jodernstore.R;
import com.example.jodernstore.fragment.JoinedCartFragment;
import com.example.jodernstore.fragment.MyCartFragment;
import com.example.jodernstore.fragment.HomeFragment;
import com.example.jodernstore.fragment.SharedCartFragment;

public class CartActivity extends AppCompatActivity {
    private LinearLayout parentView;
    private LinearLayout myCartBtn, mySharedCartBtn, myJoinedCartBtn;
    private ImageButton backBtn;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_cart);
        initViews();
        setEvents();
        setupInitialFragments();
    }

    private void initViews() {
        parentView = findViewById(R.id.cartParentView);
        myCartBtn = findViewById(R.id.cartMyCartBtn);
        mySharedCartBtn = findViewById(R.id.cartMySharedCartBtn);
        myJoinedCartBtn = findViewById(R.id.cartMyJoinedCartBtn);
        backBtn = findViewById(R.id.cartBackBtn);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void highlightBtn(LinearLayout btn) {
        btn.findViewWithTag("image").setBackground(getDrawable(R.drawable.card_image_selected));
        ((TextView)btn.findViewWithTag("text")).setTextColor(getColor(R.color.primary));
        ((TextView)btn.findViewWithTag("text")).setTypeface(null, Typeface.BOLD);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void resetBtn(LinearLayout btn) {
        btn.findViewWithTag("image").setBackground(getDrawable(R.drawable.card_image_shape));
        ((TextView)btn.findViewWithTag("text")).setTextColor(getColor(R.color.text));
        ((TextView)btn.findViewWithTag("text")).setTypeface(null, Typeface.NORMAL);
    }

    private void resetBtns() {
        resetBtn(myCartBtn);
        resetBtn(mySharedCartBtn);
        resetBtn(myJoinedCartBtn);
    }

    private void setEvents() {
        backBtn.setOnClickListener(view -> finish());

        myCartBtn.setOnClickListener(view -> {
            resetBtns();
            highlightBtn(myCartBtn);
            Fragment myCart = new MyCartFragment(myCartBtn);
            switchFragment(myCart, MyCartFragment.TAG);
        });

        mySharedCartBtn.setOnClickListener(view -> {
            resetBtns();
            highlightBtn(mySharedCartBtn);
            Fragment sharedCart = new SharedCartFragment(mySharedCartBtn);
            switchFragment(sharedCart, SharedCartFragment.TAG);
        });

        myJoinedCartBtn.setOnClickListener(view -> {
            resetBtns();
            highlightBtn(myJoinedCartBtn);
            Fragment joinedCart = new JoinedCartFragment(myJoinedCartBtn);
            switchFragment(joinedCart, JoinedCartFragment.TAG);
        });
    }

    private void setupInitialFragments() {
        Intent intent = getIntent();
        String prevFragment = intent.getStringExtra("previousFragment");
        String nextFragment = intent.getStringExtra("nextFragment");
        String message = intent.getStringExtra("message");

        // Forward to the next fragment
        if (nextFragment != null) {
            resetBtns();

            Fragment fragment = null;
            Bundle bundle = null;
            if (nextFragment.equals(MyCartFragment.TAG)) {
                fragment = new MyCartFragment(myCartBtn);
                highlightBtn(myCartBtn);
            } else if (nextFragment.equals(SharedCartFragment.TAG)) {
                fragment = new SharedCartFragment(mySharedCartBtn);
                highlightBtn(mySharedCartBtn);
            } else if (nextFragment.equals(JoinedCartFragment.TAG)) {
                fragment = new JoinedCartFragment(myJoinedCartBtn);
                highlightBtn(myJoinedCartBtn);
            }

            if (fragment == null)
                return;
            if (message != null) {
                if (bundle == null)
                    bundle = new Bundle();
                bundle.putString("message", message);
            }
            fragment.setArguments(bundle);
            switchFragmentWithoutPushingToBackStack(fragment);
            return;
        }

        // Back to previous fragment
        if (prevFragment == null) {
            prevFragment = MyCartFragment.TAG;
        }
        Fragment fragment = null;
        resetBtns();
        if (prevFragment.equals(MyCartFragment.TAG)) {
            highlightBtn(myCartBtn);
            fragment = new MyCartFragment(myCartBtn);
        } else if (prevFragment.equals(SharedCartFragment.TAG)) {
            highlightBtn(mySharedCartBtn);
            fragment = new SharedCartFragment(mySharedCartBtn);
        } else if (prevFragment.equals(JoinedCartFragment.TAG)) {
            highlightBtn(myJoinedCartBtn);
            fragment = new JoinedCartFragment(myJoinedCartBtn);
        }
        if (fragment == null)
            return;
        switchFragmentWithoutPushingToBackStack(fragment);
    }

    private void switchFragment(Fragment fragmentObject, String name) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.cartFragmentContainer, fragmentObject)
                .addToBackStack(name)
                .commit();
    }

    private void switchFragmentWithoutPushingToBackStack(Fragment fragmentObject) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.cartFragmentContainer, fragmentObject)
                .commit();
    }
}