package common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConvertUtil {
    public static long tryParseLong(String longStr) {
        return tryParseLong(longStr, 0L);
    }

    public static long tryParseLong(String longStr, long defaultValue) {
        try {
            return Long.parseLong(longStr);
        } catch (NumberFormatException nfe) {
        }
        return defaultValue;
    }

    public static double tryParseDouble(String doubleStr) {
        return tryParseDouble(doubleStr, 0.0);
    }

    public static double tryParseDouble(String doubleStr, double defaultValue) {
        try {
            return Double.parseDouble(doubleStr);
        } catch (NumberFormatException nfe) {
        }
        return defaultValue;
    }

    public static int tryParseInteger(String integerStr) {
        return tryParseInteger(integerStr, 0);
    }

    public static int tryParseInteger(String integerStr, int defaultValue) {
        try {
            return Integer.parseInt(integerStr);
        } catch (NumberFormatException nfe) {
        }
        return defaultValue;
    }
}
