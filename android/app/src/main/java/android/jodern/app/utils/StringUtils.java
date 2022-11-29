package android.jodern.app.utils;

public class StringUtils {
    public static String long2money(long number) {
        String string = String.valueOf(number);
        StringBuilder buffer = new StringBuilder();

        for (int i = string.length() - 1; i >= 0; i--) {
            buffer.append(string.charAt(i));
            if ((string.length() - i) % 3 == 0 && i != 0) {
                buffer.append('.');
            }
        }

        buffer.reverse();
        buffer.append(" VND");

        return buffer.toString();
    }
}
