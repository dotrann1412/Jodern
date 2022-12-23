package com.example.jodernstore.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.jodernstore.R;
import com.example.jodernstore.activity.AuthActivity;
import com.example.jodernstore.activity.OrderListActivity;
import com.example.jodernstore.activity.WishlistActivity;
import com.example.jodernstore.provider.Provider;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.makeramen.roundedimageview.RoundedImageView;

public class UserFragment extends Fragment {
    public static final String TAG = "UserFragment";

    private FirebaseAuth mAuth;

    private ImageButton navbarBtn;
    private FrameLayout parentView;
    private RoundedImageView avatar;
    private TextView userName;
    private LinearLayout orderListBtn, wishlistBtn;
    private LinearLayout logoutBtn;

    public UserFragment() {
        // Required empty public constructor
        super(R.layout.fragment_user);
    }

    public UserFragment(ImageButton navbarBtn) {
        super(R.layout.fragment_user);
        this.navbarBtn = navbarBtn;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Provider.with(this.getContext()).setCurrentFragment(TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Provider.with(this.getContext()).setCurrentFragment(TAG);
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (navbarBtn != null)
            navbarBtn.setImageResource(R.drawable.ic_user_filled);

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setInfors();
        setEvents();
    }

    private void initViews() {
        parentView = getView().findViewById(R.id.userParentView);
        avatar = getView().findViewById(R.id.userMainAvatar);
        userName = getView().findViewById(R.id.userMainName);
        orderListBtn = getView().findViewById(R.id.userMainOrderListBtn);
        wishlistBtn = getView().findViewById(R.id.userMainWishlistBtn);
        logoutBtn = getView().findViewById(R.id.userMainLogoutBtn);
    }

    @Override
    public void onResume() {
        super.onResume();
        Provider.with(this.getContext()).setCurrentFragment(TAG);
        if (navbarBtn != null)
            navbarBtn.setImageResource(R.drawable.ic_user_filled);
    }

    private void setInfors() {
        FirebaseUser user = mAuth.getCurrentUser();
        userName.setText(user.getDisplayName());
        // avatar
        String url = "";
        if (AccessToken.getCurrentAccessToken() != null) {
            url = user.getPhotoUrl() + "?access_token=" + AccessToken.getCurrentAccessToken().getToken() + "&type=large";
        } else
            url = user.getPhotoUrl() + "?type=large";
        Glide.with(this).load(url).into(avatar);
    }

    private void setEvents() {
        orderListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), OrderListActivity.class);
                startActivity(intent);
            }
        });

        wishlistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), WishlistActivity.class);
                startActivity(intent);
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                AccessToken.setCurrentAccessToken(null);

                // start AuthActivity without backstack
                Intent intent = new Intent(getContext(), AuthActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (navbarBtn != null) {
            navbarBtn.setImageResource(R.drawable.ic_user);
        }
        super.onDestroyView();
    }
}