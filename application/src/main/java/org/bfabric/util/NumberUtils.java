/* 
    MIT License

    Copyright (c) 2005-2026 Functional Genomics Center Zurich, UZH/ETH Zurich

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.   
 */

package org.bfabric.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.bfabric.Messages;

@Named
@ApplicationScoped
public class NumberUtils {

    public static BigDecimal getDecimalScale(BigDecimal value, int scale) {
        return value == null ? null : value.setScale(scale, RoundingMode.HALF_UP);
    }

    public static BigDecimal getDecimalScale2(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }

    public static String getPrintSize(long size) {
        return getPrintSize(size, null);
    }

    public static String getPrintSize(long size, Integer unit) {
        DecimalFormat df = new DecimalFormat("###0.000");

        // Size zero
        if (size <= 0) {
            return "0";
        }

        int length = (int) (Math.log(size) / Math.log(1024));
        if (unit != null) {
            length = unit;
        }

        String ret = df.format(size / Math.pow(1024, length));
        switch (length) {
        case 8:
            return ret + " YB";
        case 7:
            return ret + " ZB";
        case 6:
            return ret + " EB";
        case 5:
            return ret + " PB";
        case 4:
            return ret + " TB";
        case 3:
            return ret + " GB";
        case 2:
            return ret + " MB";
        case 1:
            return ret + " KB";
        case 0:
        default:
            return ret + " B";
        }
    }

    public static BigDecimal getRoundedPrice(BigDecimal value, String currencyCode) {
        if (value == null) {
            return null;
        }
        return "CHF".equals(currencyCode) ? roundToSwissFrancs(value) : value.setScale(2, RoundingMode.HALF_UP);
    }

    public static boolean isLong(Object value) {
        try {
            Long.parseLong(String.valueOf(value));
        } catch (final NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isNumeric(Object value) {
        try {
            Double.parseDouble(String.valueOf(value));
        } catch (final NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static String isNumericGreaterOrEqualsZero(Object value) {
        try {
            if (Double.parseDouble(String.valueOf(value)) < 0) {
                return Messages.get("notPositiveOrZero");
            }
        } catch (final NumberFormatException e) {
            return Messages.get("notNumeric");
        }
        return null;
    }

    public static String isNumericGreaterZero(Object value) {
        try {
            if (!(Double.parseDouble(String.valueOf(value)) > 0)) {
                return Messages.get("notPositive");
            }
        } catch (final NumberFormatException e) {
            return Messages.get("notNumeric");
        }
        return null;
    }

    public static double roundToDecimals(double value) {
        return roundToDecimals(value, 2);
    }

    public static double roundToDecimals(double value, int f) {
        int fractionalDigits = Math.max(f, 1);
        return Math.round(value * Math.pow(10, fractionalDigits)) / Math.pow(10, fractionalDigits);
    }

    public static BigDecimal roundToSwissFrancs(BigDecimal value) {
        if (value == null) {
            return null;
        }
        value = value.setScale(2, RoundingMode.HALF_UP);
        long cents = value.multiply(new BigDecimal("100")).longValue();
        long remainder = cents % 5;
        if (remainder == 0) {
            return value;
        }
        long roundedCents = cents - remainder + (remainder >= 3 ? 5 : 0);
        return new BigDecimal(roundedCents).divide(new BigDecimal("100"), 2, RoundingMode.UNNECESSARY);
    }
}