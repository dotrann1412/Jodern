package com.example.jodern.customwidget;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.example.jodern.R;
import com.google.android.material.snackbar.Snackbar;

public class MySnackbar {
    public static Snackbar inforSnackar(Context context, View parent, String message) {
        Snackbar snackbar = Snackbar.make(parent, message, Snackbar.LENGTH_SHORT);
        if (context != null) {
            snackbar.setAction(context.getString(R.string.skip_snackbar), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    snackbar.dismiss();
                }
            });
        }
        TextView textView = (TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_action);
        textView.setAllCaps(false);
        return snackbar;
    }
}
