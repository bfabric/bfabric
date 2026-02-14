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

package org.bfabric.forms;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.bfabric.Constants;
import org.bfabric.enums.SampleTypeEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.util.ClassHelper;
import org.bfabric.util.ConfigurationHelper;
import org.bfabric.util.StringHelper;
import org.bfabric.util.UriHelper;

public class MFHelper {

    public static BigDecimal bigDecimalValueOf(String source, String value) throws InvalidDataException {
        checkNotNull(source, value);
        BigDecimal longValue = BigDecimal.ZERO;
        try {
            if (StringHelper.isNotEmpty(value)) {
                longValue = new BigDecimal(value);
            }
        } catch (NumberFormatException e) {
            if (StringHelper.isNotEmpty(source)) {
                throw new InvalidDataException(source + " " + value + " is not a big decimal (numeric) value!");
            }
        }
        return longValue;
    }

    public static boolean booleanValueOf(String source, String value) throws InvalidDataException {
        if (value != null) {
            if (Boolean.TRUE.toString().equalsIgnoreCase(value) || value.equalsIgnoreCase("t") || value.equalsIgnoreCase("yes")) {
                return true;
            }
            if (Boolean.FALSE.toString().equalsIgnoreCase(value) || value.equalsIgnoreCase("f") || value.equalsIgnoreCase("no")) {
                return false;
            }
            throw new InvalidDataException((StringHelper.isNotEmpty(source) ? source + " " : "") + value + " is not boolean value (true or false)!");
        }
        return false;
    }

    public static boolean booleanValueOf(String value) throws InvalidDataException {
        return booleanValueOf(null, value);
    }

    public static void checkLength(String source, String value) throws InvalidDataException {
        checkLength(source, value, 256);
    }

    public static void checkLength(String source, String value, int length) throws InvalidDataException {
        if (StringHelper.isNotEmpty(source) && value != null && value.length() > length) {
            throw new InvalidDataException(source + " must not be longer than " + length + " characters!");
        }
    }

    public static void checkNotNull(String source, String value) throws InvalidDataException {
        if (StringHelper.isEmpty(value) && StringHelper.isNotEmpty(source)) {
            throw new InvalidDataException("No value specified for " + source + "!");
        }
    }

    public static void checkNotSampleTypeUserSampleInMultiplex(String value) throws InvalidDataException {
        if (StringHelper.isNotEmpty(value) && value.equals(SampleTypeEnum.USER_LIBRARY_IN_POOL.getLabel())) {
            throw new InvalidDataException("Modifications, i.e., creating or updating a sample of type 'User Library in Pool', are not possible via the web services.");
        }
    }

    public static void checkUri(String source, String uri) throws InvalidDataException {
        try {
            UriHelper.createUri(uri);
        } catch (InvalidDataException e) {
            throw new InvalidDataException(source + " is not a valid URI: " + uri + "!");
        }
    }

    public static void checkUri(String uri) throws InvalidDataException {
        try {
            UriHelper.createUri(uri);
        } catch (InvalidDataException e) {
            throw new InvalidDataException("Invalid uri format:" + uri + "!");
        }
    }

    public static String className(String source, String className) throws InvalidDataException {
        String targetClassName = ClassHelper.getClassName(className);
        if (targetClassName == null) {
            throw new InvalidDataException((StringHelper.isNotEmpty(source) ? source + " " : Constants.EMPTY_STRING) + className + " does not exists!");
        }
        return targetClassName;
    }

    public static LocalDateTime dateTimeValueOf(String value) throws InvalidDataException {
        return dateTimeValueOf(Constants.EMPTY_STRING, value);
    }

    public static LocalDateTime dateTimeValueOf(String source, String value) throws InvalidDataException {
        if (StringHelper.isNotEmpty(value) && !value.equalsIgnoreCase(Constants.NULL)) {
            LocalDateTime dateTime;
            try {
                dateTime = LocalDateTime.parse(value);
            } catch (Exception e) {
                try {
                    dateTime = LocalDate.parse(value, Constants.DATE_FORMATTER).atStartOfDay();
                } catch (Exception e1) {
                    try {
                        dateTime = LocalDate.parse(value, Constants.DATE_FORMATTER_EU).atStartOfDay();
                    } catch (Exception e2) {
                        try {
                            dateTime = LocalDateTime.parse(value, Constants.DATETIME_FORMATTER);
                        } catch (Exception e3) {
                            try {
                                dateTime = LocalDateTime.parse(value, DateTimeFormatter.ofPattern(Constants.DATETIME_PATTERN_MM));
                            } catch (Exception e4) {
                                try {
                                    dateTime = LocalDateTime.parse(value, DateTimeFormatter.ofPattern(Constants.DATETIME_PATTERN_N));
                                } catch (Exception e5) {
                                    try {
                                        dateTime = LocalDateTime.parse(value, DateTimeFormatter.ofPattern(Constants.DATETIME_PATTERN_TIMESTAMP));
                                    } catch (Exception e6) {
                                        try {
                                            dateTime = LocalDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                                        } catch (Exception e7) {
                                            throw new InvalidDataException(source + " " + value + " is not a datetime or has wrong format! Expected: "
                                                + Constants.DATE_PATTERN + " | " + Constants.DATE_PATTERN_EU + " | " + Constants.DATETIME_PATTERN + " | " + Constants.DATETIME_PATTERN_MM
                                                + " | " + Constants.DATETIME_PATTERN_N + " | " + Constants.DATETIME_PATTERN_TIMESTAMP + " | " + Constants.DATETIME_PATTERN_ISO);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return dateTime;
        }
        return null;
    }

    public static LocalDate dateValueOf(String value) throws InvalidDataException {
        return dateValueOf(Constants.EMPTY_STRING, value);
    }

    public static LocalDate dateValueOf(String source, String value) throws InvalidDataException {
        if (StringHelper.isNotEmpty(value) && !value.equalsIgnoreCase(Constants.NULL)) {
            LocalDate dateTime;
            try {
                dateTime = LocalDate.parse(value, Constants.DATE_FORMATTER);
            } catch (Exception e1) {
                try {
                    dateTime = LocalDate.parse(value, Constants.DATE_FORMATTER_EU);
                } catch (Exception e2) {
                    throw new InvalidDataException(source + " " + value + " is not a date or has wrong format! Expected: " + Constants.DATE_FORMATTER + " | " + Constants.DATE_PATTERN_EU);
                }
            }
            return dateTime;
        }
        return null;
    }

    public static double doubleValueOf(String source, String value) throws InvalidDataException {
        checkNotNull(source, value);
        try {
            if (StringHelper.isNotEmpty(value)) {
                return Double.parseDouble(value.trim());
            }
        } catch (NumberFormatException e) {
            if (StringHelper.isNotEmpty(source)) {
                throw new InvalidDataException(source + " " + value + " is not a double (numeric) value!");
            }
        }
        return 0;
    }

    public static float floatValueOf(String source, String value) throws InvalidDataException {
        checkNotNull(source, value);
        try {
            if (StringHelper.isNotEmpty(value)) {
                return Float.parseFloat(value.trim());
            }
        } catch (NumberFormatException e) {
            if (StringHelper.isNotEmpty(source)) {
                throw new InvalidDataException(source + " " + value + " is not a float (numeric) value!");
            }
        }
        return 0;
    }

    public static Class<?> getEntityClass(String source, String className) throws InvalidDataException {
        return ClassHelper.getClassByName(className(source, className));
    }

    public static int integerValueOf(String source, String value) throws InvalidDataException {
        checkNotNull(source, value);
        try {
            if (StringHelper.isNotEmpty(value)) {
                return Integer.parseInt(value.trim());
            }
        } catch (NumberFormatException e) {
            if (StringHelper.isNotEmpty(source)) {
                throw new InvalidDataException(source + " " + value + " is not an integer (numeric) value!");
            }
        }
        return 0;
    }

    public static boolean isDate(Object value) {
        try {
            dateValueOf(Constants.EMPTY_STRING, String.valueOf(value));
        } catch (final InvalidDataException e) {
            return false;
        }
        return true;
    }

    public static boolean isDateTime(Object value) {
        try {
            dateTimeValueOf(String.valueOf(value));
        } catch (final InvalidDataException e) {
            return false;
        }
        return true;
    }

    public static boolean isTime(Object value) {
        try {
            timeValueOf(Constants.EMPTY_STRING, String.valueOf(value));
        } catch (final InvalidDataException e) {
            return false;
        }
        return true;
    }

    public static long longValueOf(String source, String value) throws InvalidDataException {
        checkNotNull(source, value);
        try {
            if (StringHelper.isNotEmpty(value)) {
                return Long.parseLong(value.trim());
            }
        } catch (NumberFormatException e) {
            if (StringHelper.isNotEmpty(source)) {
                throw new InvalidDataException(source + " " + value + " is not a long (numeric) value!");
            }
        }
        return 0;
    }

    public static long nonNegativeLongValueOf(String source, Long value) throws InvalidDataException {
        if (value == null) {
            return 0;
        }
        if (value < 0 && StringHelper.isNotEmpty(source)) {
            throw new InvalidDataException(source + " " + value + " is negative!");
        }
        return value;
    }

    public static Long nonNegativeLongValueOf(String source, String value) throws InvalidDataException {
        return nonNegativeLongValueOf(source, longValueOf(source, value));
    }

    public static Long positiveLongValueOf(String source, String value) throws InvalidDataException {
        long longValue = longValueOf(source, value);
        if (longValue <= 0 && StringHelper.isNotEmpty(source)) {
            throw new InvalidDataException(source + " " + value + " is non-positive!");
        }
        return longValue;
    }

    public static void throwNoValueSpecifiedError(String source) throws InvalidDataException {
        throw new InvalidDataException("No value specified for " + source + "!");
    }

    public static void throwValueNotSupportedError(String source, String type) throws InvalidDataException {
        throw new InvalidDataException(source + " value is not supported for type " + type.toLowerCase() + "!");
    }

    public static LocalTime timeValueOf(String source, String value) throws InvalidDataException {
        if (StringHelper.isNotEmpty(value) && !value.equalsIgnoreCase(Constants.NULL)) {
            LocalTime time;
            try {
                time = LocalTime.parse(value, Constants.TIME_FORMATTER);
            } catch (Exception e) {
                throw new InvalidDataException(source + " " + value + " is not a time or has wrong format! Expected: " + ConfigurationHelper.getConfiguration()
                    .getDefaultTimePattern());
            }
            return time;
        }
        return null;
    }
}
