package com.example.jodern.customwidget;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MyToast {
    public static void makeText(Context context, String message, int duration) {
        Toast toast = Toast.makeText(context, message, duration);
        LinearLayout layout = (LinearLayout) toast.getView();
        if (layout != null && layout.getChildCount() > 0) {
            TextView tv = (TextView) layout.getChildAt(0);
            tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        }
        toast.show();
    }
}
