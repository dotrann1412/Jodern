package android.jodern.app.util;

import java.text.DecimalFormat;

public class Utils {
    public static String vndFormatPrice(int price) {
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        return formatter.format(price) + " VNĐ";
    }
}
