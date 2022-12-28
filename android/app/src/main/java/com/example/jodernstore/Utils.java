package com.example.jodernstore;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Utils {
    public static String vndFormatPrice(Long price) {
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        return formatter.format(price) + " VNĐ";
    }

    public static LocalDate stringToDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return LocalDate.parse(date, formatter);
    }

    public static String localDateToString(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
