package com.example.jodern.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.jodern.R;

public class CartFragment extends Fragment {
    private String mParam1;
    private String mParam2;
    private ImageButton navbarBtn;

    public CartFragment() {
        // Required empty public constructor
        super(R.layout.fragment_cart);
    }

    public CartFragment(ImageButton navbarBtn) {
        super(R.layout.fragment_home);
        this.navbarBtn = navbarBtn;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString("ARG_PARAM1");
            mParam2 = getArguments().getString("ARG_PARAM2");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (navbarBtn != null)
            navbarBtn.setImageResource(R.drawable.ic_cart_filled);
    }

    @Override
    public void onDestroyView() {
        if (navbarBtn != null)
            navbarBtn.setImageResource(R.drawable.ic_cart);
        super.onDestroyView();
    }
}