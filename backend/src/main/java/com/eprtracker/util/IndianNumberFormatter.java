package com.eprtracker.util;

import java.text.DecimalFormat;

public final class IndianNumberFormatter {

    private IndianNumberFormatter() {}

    public static String format(long number) {
        if (number == 0) {
            return "0";
        }
        boolean isNegative = number < 0;
        long absNumber = Math.abs(number);

        String str = String.valueOf(absNumber);
        if (str.length() <= 3) {
            return (isNegative ? "-" : "") + str;
        }

        String lastThree = str.substring(str.length() - 3);
        String remaining = str.substring(0, str.length() - 3);

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = remaining.length() - 1; i >= 0; i--) {
            sb.append(remaining.charAt(i));
            count++;
            if (count % 2 == 0 && i != 0) {
                sb.append(",");
            }
        }

        String formatted = sb.reverse() + "," + lastThree;
        return (isNegative ? "-" : "") + formatted;
    }

    public static String format(double number) {
        long integerPart = (long) number;
        double fractionalPart = Math.abs(number - integerPart);
        String formattedInt = format(integerPart);
        if (fractionalPart == 0) {
            return formattedInt;
        }
        DecimalFormat df = new DecimalFormat(".##");
        return formattedInt + df.format(fractionalPart);
    }
}
