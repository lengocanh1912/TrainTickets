package t3h.edu.vn.traintickets.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class PriceUtils {

    private static final Locale VI_LOCALE = new Locale("vi", "VN");

    // Format BigDecimal 300000 -> "300.000 VND"
    public static String formatWithVND(BigDecimal price) {
        if (price == null) return "";
        NumberFormat nf = NumberFormat.getInstance(VI_LOCALE);
        nf.setMaximumFractionDigits(0);
        return nf.format(price) + " VND";
    }

    // Format BigDecimal 300000 -> "300.000" (no currency)
    public static String formatNoCurrency(BigDecimal price) {
        if (price == null) return "";
        NumberFormat nf = NumberFormat.getInstance(VI_LOCALE);
        nf.setMaximumFractionDigits(0);
        return nf.format(price);
    }

    // Parse string like "350.000" or "355.555" -> BigDecimal 350000 or 355555
    public static BigDecimal parseFromFormattedString(String raw) {
        if (raw == null) return null;
        // Remove dots, commas, spaces, and non-digit characters
        String cleaned = raw.replaceAll("[^0-9]", "");
        if (cleaned.isEmpty()) return null;
        return new BigDecimal(cleaned);
    }
}
