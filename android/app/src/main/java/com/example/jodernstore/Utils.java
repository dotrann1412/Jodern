package com.example.jodernstore;
import java.text.DecimalFormat;

public class Utils {
    public static String vndFormatPrice(Long price) {
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        return formatter.format(price) + " VNĐ";
    }
}
