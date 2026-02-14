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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.WordUtils;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.BillingInfo;
import org.bfabric.entity.Country;
import org.bfabric.entity.CustomAttribute;
import org.bfabric.entity.Division;
import org.bfabric.entity.Institute;
import org.bfabric.exception.BfabricValidatorException;
import org.htmlparser.Parser;
import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

@Named
@ApplicationScoped
public class StringHelper extends ConfigurationHelper {

    public static final String EMAIL_XX = "@xx.xx";

    public static final int MIN_SUBSTRING_LENGTH = 3;

    public static final String lineSeparator = System.lineSeparator();

    public static final String validEmailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    private static final String[][] umlautReplacements = { { "Ä", "Ae" }, { "Ü", "Ue" }, { "Ö", "Oe" }, { "ä", "ae" }, { "ü", "ue" }, { "ö", "oe" } };

    // private static final Logger logger = Logger.getLogger(StringHelper.class.getName());

    public static void addEntityInfoItem(StringBuilder entityInfo, String label, Object value) {
        StringBuilder item = new StringBuilder();
        if (value != null && (!(value instanceof String) || isNotEmpty((String) value))) {
            item.append("\n");
            if (isNotEmpty(label)) {
                String messageLabel = Messages.get(label);
                if (isNotEmpty(messageLabel)) {
                    item.append(messageLabel).append(" = ");
                }
            }
            item.append(value);
        }
        if (item.length() > 0) {
            entityInfo.append(item);
        }
    }

    public static void addEntityInfoItemIfNotEmpty(StringBuilder entityInfo, String label, Object value) {
        if (isNotEmpty((String) value)) {
            addEntityInfoItem(entityInfo, label, value);
        }
    }

    public static void addEntityInfoItems(StringBuilder entityInfo, List<CustomAttribute> customAttributes) {
        if (entityInfo != null && customAttributes != null && !customAttributes.isEmpty()) {
            StringBuilder items = new StringBuilder();
            for (CustomAttribute customAttribute : customAttributes) {
                items.append("\n").append(customAttribute.getName()).append(" = ").append(customAttribute.getValue());
            }
            if (items.length() > 0) {
                entityInfo.append(items);
            }
        }
    }

    public static String capitalize(String value) {
        String ret = format(value);
        return isEmpty(ret) ? ret : WordUtils.capitalize(ret.toLowerCase(), ' ', '(', '_');
    }

    public static byte[] charsToBytes(char[] chars) {
        final ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
        byte[] bytes = Arrays.copyOf(byteBuffer.array(), byteBuffer.limit());
        // Clear sensitive data.
        Arrays.fill(byteBuffer.array(), (byte) 0);
        return bytes;
    }

    public static void clearCharArray(char[] chars) {
        if (chars != null) {
            Arrays.fill(chars, '\u0000');
        }
    }

    public static String concatenate(String value1, String value2, String separator) {
        String ret = Constants.EMPTY_STRING; // Return empty string if both values are null.
        String concatenateSeparator = getSeparator(separator);
        if (isNotEmpty(value1) || isNotEmpty(value2)) {
            if (isNotEmpty(value1) && isNotEmpty(value2)) {
                ret = value1 + concatenateSeparator + value2;
            } else if (isNotEmpty(value1)) {
                ret = value1;
            } else {
                ret = value2;
            }
        }
        return ret;
    }

    public static int containsPartly(String source, String subString, int minSubstringLength) {
        if (source != null && subString != null && minSubstringLength <= subString.length() && minSubstringLength > 0) {
            String sourceLowerCase = source.toLowerCase();
            String subStringLowerCase = subString.toLowerCase();
            for (int i = 0; i <= subStringLowerCase.length() - minSubstringLength; i++) {
                for (int j = subStringLowerCase.length(); j >= i + minSubstringLength; j--) {
                    if (sourceLowerCase.contains(subStringLowerCase.substring(i, j))) {
                        return subStringLowerCase.substring(i, j).length();
                    }
                }
            }
        }
        return 0;
    }

    public static boolean containsSubstring(char[] target, char[] substring) {
        if (target != null && substring != null && target.length >= substring.length) {
            for (int i = 0; i < target.length; i++) {
                if (Character.toLowerCase(target[i]) == Character.toLowerCase(substring[0])) {
                    // Found first character, now look at the rest of the substring.
                    boolean matching = true;
                    int substringSubIndex = 1;
                    for (int targetSubIndex = i + 1; targetSubIndex < target.length && substringSubIndex < substring.length; targetSubIndex++, substringSubIndex++) {
                        if (Character.toLowerCase(target[targetSubIndex]) != Character.toLowerCase(substring[substringSubIndex])) {
                            matching = false;
                            break;
                        }
                    }
                    if (matching && substringSubIndex == substring.length) {
                        // The target contains the whole substring.
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    public static boolean containsSubstring(char[] target, String substring) {
        return containsSubstring(target, substring != null ? substring.toCharArray() : null);
    }

    public static String convertToBreakable(String value) {
        // return isNotEmpty(value) ? value.replaceAll("[^A-Za-z]", "$0\u200B"): value;
        return isNotEmpty(value) ? value.replaceAll(".(?!$)", "$0\u200B") : value;
    }

    public static boolean correctTubeIdFormat(String tubeId) {
        return tubeId.matches(Constants.ORDER_ITEM_TUBEID_REGEXP);
    }

    public static String createRowMessageComponent(String rowKeyId, String componentId) {
        return Constants.MESSAGE_COMPONENT_ROW_IDENTIFIER + Constants.MESSAGE_COMPONENT_SEPARATOR + rowKeyId + Constants.MESSAGE_COMPONENT_SEPARATOR + componentId;
    }

    public static String createRowMessageComponentForInput(String rowKeyId, String columnId) {
        return createRowMessageComponent(rowKeyId, columnId + Constants.INPUT);
    }

    public static LocalDate decodeBase64Date(String code) {
        try {
            return LocalDate.parse(new String(Base64.decodeBase64(code), getConfiguration().getDefaultCharset()));
        } catch (Exception e) {
            return null;
        }
    }

    public static String embraceParentheses(String value) {
        return isNotEmpty(value) ? "(" + value + ")" : null;
    }

    public static String encodeBase64(File file) {
        if (file != null && file.exists() && file.canRead()) {
            FileInputStream inputStream = null;
            try {
                byte[] stringAsByteArray = new byte[(int) file.length()];
                inputStream = new FileInputStream(file);
                //noinspection ResultOfMethodCallIgnored
                inputStream.read(stringAsByteArray);
                return Base64.encodeBase64String(stringAsByteArray).replaceAll("\\n", Constants.EMPTY_STRING).replaceAll("\\r", Constants.EMPTY_STRING);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public static String encodeBase64Date() {
        return encodeBase64Date(LocalDate.now());
    }

    public static String encodeBase64Date(LocalDate date) {
        String defaultCharset = getConfiguration().getDefaultCharset();
        try {
            return new String(Base64.encodeBase64(date.toString().getBytes(defaultCharset)), defaultCharset);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String firstLower(String value) {
        String ret = format(value);
        return isNotEmpty(ret) ? ret.substring(0, 1).toLowerCase().concat(ret.substring(1)) : ret;
    }

    public static String firstUpper(String value) {
        String ret = format(value);
        return isNotEmpty(ret) ? ret.substring(0, 1).toUpperCase().concat(ret.substring(1)) : ret;
    }

    public static String format(String value) {
        String ret = trimEmptyLinesAndControlCharacters(value);
        return isNotEmpty(ret) ? ret.replaceAll("[\\s]+", " ") : null;
    }

    public static String formatFileName(String value) {
        return value != null ? value.replaceAll("[^A-Za-z0-9.\\-]", "_") : null;
    }

    public static String formatFolderName(String value) {
        String folderName = value;
        if (folderName != null) {
            folderName = folderName.replaceAll("[^a-zA-Z0-9_]", Constants.EMPTY_STRING);
            if (folderName.isEmpty()) {
                folderName = null;
            }
        }
        return folderName;
    }

    public static String formatLowerCase(String value) {
        return value != null ? format(value.toLowerCase()) : null;
    }

    public static String formatMailMessage(String value) {
        String ret = trimBoth(removeControlCharacters(value));
        return isNotEmpty(ret) ? ret : null;
    }

    public static String formatNonNull(String value) {
        return value != null ? trimEmptyLinesAndControlCharacters(value) : Constants.EMPTY_STRING;
    }

    public static String formatText(String value) {
        String ret = value;
        if (ret != null) {
            ret = trimBoth(removeDoubleEmptyLines(ret.replaceAll("[\u00AD\u007F\uFEFF\u0000-\u0009\u000B-\u000C\u000E-\u001F\u0080-\u009F\u2000-\u200F\u2028-\u202F\u2060-\u206F]", "")
                .replaceAll("[\u00A0]", " ").trim()));
        }
        return isNotEmpty(ret) ? ret.replaceAll(" +", " ") : null;
    }

    public static String generateString(char character, int numberOfChars) {
        char[] chars = new char[numberOfChars];
        Arrays.fill(chars, character);
        return new String(chars);
    }

    public static String generateString(int numberOfChars) {
        char[] chars = new char[numberOfChars];
        Arrays.fill(chars, 'c');
        return new String(chars);
    }

    public static String getComponentIdFromRowMessageComponent(String rowMessageComponent) {
        // rowMessageComponent = Constants.MESSAGE_COMPONENT_ROW_ID + Constants.MESSAGE_COMPONENT_SEPARATOR + rowKeyId + Constants.MESSAGE_COMPONENT_SEPARATOR + componentId
        return rowMessageComponent.split(Constants.MESSAGE_COMPONENT_SEPARATOR)[2];
    }

    public static String getEnding(int count) {
        return count == 1 ? Constants.EMPTY_STRING : Constants.PLURAL_S;
    }

    public static String getFormattedAsSeconds(long duration) {
        return DurationFormatUtils.formatDuration(duration, "dd'd 'HH':'mm':'ss''");
    }

    public static String getFormattedDuration(Duration duration) {
        return getFormattedDuration(duration, "dd'd 'HH':'mm':'ss''");
    }

    public static String getFormattedDuration(Duration duration, String format) {
        if (duration != null && isNotEmpty(format)) {
            String formattedDuration = DurationFormatUtils.formatDuration(duration.toMillis(), format);
            String formattedDurationTrim = formattedDuration.replace("00:", Constants.EMPTY_STRING).replace(":00", Constants.EMPTY_STRING).trim();
            return isNotEmpty(formattedDurationTrim) ? formattedDuration : null;
        }
        return null;
    }

    public static String getFormattedDurationTrim(Duration duration) {
        if (duration != null) {
            String formattedDuration = DurationFormatUtils.formatDuration(duration.toMillis(), "dd'd 'HH'h 'mm'm 'ss's'")
                .replace("00d", Constants.EMPTY_STRING).replace("00h", Constants.EMPTY_STRING).replace("00m", Constants.EMPTY_STRING).replace("00s", Constants.EMPTY_STRING).trim();
            return isNotEmpty(formattedDuration) ? formattedDuration.length() > 1 && formattedDuration.charAt(0) == '0' ? formattedDuration.substring(1) : formattedDuration : "0s";
        }
        return null;
    }

    public static String getFullAddress(String supplement, String street, String zip, String city, Country country) {
        return getFullAddress(supplement, null, street, zip, city, country, 0);
    }

    public static String getFullAddress(String street, String zip, String city, Country country) {
        return getFullAddress(null, null, street, zip, city, country, 0);
    }

    public static String getFullAddress(String street, String zip, String city, Country country, int format) {
        return getFullAddress(null, null, street, zip, city, country, format);
    }

    public static String getFullAddress(String supplement, String room, String street, String zip, String city, Country country, int format) {
        StringBuilder fullAddress = new StringBuilder();
        String separator = ", ";
        if (format == 1) {
            separator = "\n";
        }

        if (isNotEmpty(supplement)) {
            fullAddress.append(supplement);
            if (isNotEmpty(room) || isNotEmpty(street) || isNotEmpty(zip) || isNotEmpty(city) || country != null) {
                fullAddress.append(separator);
            }
        }

        if (isNotEmpty(room)) {
            fullAddress.append(room);
            if (isNotEmpty(street) || isNotEmpty(zip) || isNotEmpty(city) || country != null) {
                fullAddress.append(separator);
            }
        }

        if (isNotEmpty(street)) {
            fullAddress.append(street);
            if (isNotEmpty(zip) || isNotEmpty(city) || country != null) {
                fullAddress.append(separator);
            }
        }

        if (country != null && (isNotEmpty(zip) || isNotEmpty(city))) {
            String countryId = country.getId();
            if (isNotEmpty(countryId)) {
                fullAddress.append(countryId);
                if (format == 1 && !isNotEmpty(zip)) {
                    fullAddress.append("\n");
                } else {
                    fullAddress.append("-");
                }
            }
        }

        if (isNotEmpty(zip)) {
            fullAddress.append(zip);
            if (isNotEmpty(city)) {
                fullAddress.append(" ");
            }
        }

        if (isNotEmpty(city)) {
            fullAddress.append(city);
        }

        if (country != null) {
            String countryName = country.getName();
            if (isNotEmpty(countryName) && !countryName.equalsIgnoreCase(getConfiguration().getDeployerAddressCountry())) {
                if (isNotEmpty(zip) || isNotEmpty(city)) {
                    fullAddress.append(separator);
                }
                fullAddress.append(countryName);
            }
        }

        return fullAddress.toString();
    }

    public static String getPostalAddress(BillingInfo billingInfo, Institute institute, Division division) {
        StringBuilder postalAddress = new StringBuilder();
        if (billingInfo != null) {
            postalAddress.append(billingInfo.getBillingCustomerName());
            if (isNotEmpty(billingInfo.getBillingEmail())) {
                postalAddress.append(", ").append(billingInfo.getBillingEmail());
            }
            if (institute != null) {
                postalAddress.append(", ").append(institute.getAffiliation());
            } else if (division != null) {
                postalAddress.append(", ").append(division.getAffiliation());
            }
            postalAddress.append(", ").append(billingInfo.getBillingAddressFull());
        }
        return postalAddress.toString();
    }

    public static String getRowKeyIdFromRowMessageComponentKey(String rowMessageComponent) {
        // rowMessageComponent = Constants.MESSAGE_COMPONENT_ROW_ID + Constants.MESSAGE_COMPONENT_SEPARATOR + rowKeyId + Constants.MESSAGE_COMPONENT_SEPARATOR + componentId
        return rowMessageComponent.split(Constants.MESSAGE_COMPONENT_SEPARATOR)[1];
    }

    public static String getSafeHtml(String html) {
        String safeHtml = Constants.EMPTY_STRING;
        if (isNotEmpty(html)) {
            PolicyFactory policy = new HtmlPolicyBuilder()
                .allowStandardUrlProtocols()
                .allowCommonBlockElements()
                .allowCommonInlineFormattingElements()
                .allowElements("a", "img")
                .allowAttributes("src").onElements("img")
                .allowAttributes("href").onElements("a")
                .toFactory();
            safeHtml = policy.sanitize(mixedToHtml(html));
            safeHtml = removeDoubleBrs(safeHtml.trim());
        }
        return safeHtml;
    }

    public static String getSeparator(String separator) {
        return separator != null ? separator : "...";
    }

    public static String getTimeFormat(Duration duration) {
        return getTimeFormat(duration, "HH':'mm");
    }

    public static String getTimeFormat(Duration duration, String format) {
        return duration != null && duration.toMinutes() >= 0 ? DurationFormatUtils.formatDuration(duration.toMillis(), format).replace("00s", Constants.EMPTY_STRING).trim() : null;
    }

    public static String getTimeFormatHM(Duration duration) {
        return getTimeFormat(duration, "HH'h 'mm'm'");
    }

    public static String getTitle(String value) {
        return getTitle(value, 80);
    }

    public static String getTitle(String value, int lengthLimit) {
        return value != null && value.length() > lengthLimit ? value : null;
    }

    public static String html2text(String html) {
        // convert HTML document
        StringBean sb = new StringBean();
        sb.setLinks(false); // no links
        sb.setReplaceNonBreakingSpaces(false); // replace non-breaking spaces
        sb.setCollapse(false); // replace sequences of whitespaces
        Parser parser = new Parser();
        try {
            parser.setInputHTML(html);
            parser.visitAllNodesWith(sb);
        } catch (ParserException e) {
            return null;
        }
        String docText = sb.getStrings();

        if (docText == null) {
            docText = Constants.EMPTY_STRING; // no content
        }

        return docText;
    }

    public static boolean isEmpty(String value) {
        return value == null || trimBoth(value).isEmpty();
    }

    public static boolean isInvalidEmailAddress(String email) {
        if (isEmpty(email) || !Pattern.compile(validEmailRegex).matcher(email).matches()) {
            return true;
        }
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            return true;
        }
        return false;
    }

    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    public static boolean isTrimBothNotEmpty(String value) {
        return value != null && !trimBoth(value).isEmpty();
    }

    public static String iso2utf8trim(String value) {
        Charset utf8charset = StandardCharsets.UTF_8;
        Charset iso88591charset = StandardCharsets.ISO_8859_1;
        return value != null ? new String(trimBoth(value).getBytes(iso88591charset), utf8charset) : null;
    }

    public static String itrim(String value) {
        return value != null ? value.replaceAll("\\b\\s{2,}\\b", " ") : null;
    }

    public static String ltrim(String value) {
        return value != null ? value.replaceAll("^\\s+", Constants.EMPTY_STRING) : null;
    }

    public static String misctrim(String value) {
        return value != null ? value.replaceAll("\\s\\s+|\\n|\\r|\\t", " ") : null;
    }

    public static String mixedToHtml(String value) {
        return value != null ? removeEmptyLines(value).replaceAll("\n", "<p>").replaceAll("\r", "<p>") : null;
    }

    public static String removeControlCharacters(String value) {
        return value != null ? value.replaceAll("[\\p{C}\\r\\n\\t\\u00AD\\u007F\\uFEFF\\u0000-\\u001F\\u0080-\\u009F\\u2000-\\u200F\\u2028-\\u202F\\u2060-\\u206F]", "")
            .replaceAll("[\\u00A0]", " ") : null;
    }

    public static String removeDoubleBrs(String value) {
        return value != null ? value.replaceAll("(<br />[\\s]*<br />)+", "<br>") : null;
    }

    public static String removeDoubleEmptyLines(String value) {
        return value != null ? value.replaceAll("\\n[\\s]*\\n[\\s]*\\n", "\n\n").replaceAll("\\r[\\s]*\\r[\\s]*\\r", "\r\r") : null;
    }

    public static String removeEmptyLines(String value) {
        return value != null ? value.replaceAll("\\n[\\s]*\\n", "\n").replaceAll("\\r[\\s]*\\r", "\r") : null;
    }

    public static String replaceAccent(String src) {
        return Normalizer.normalize(src, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", Constants.EMPTY_STRING);
    }

    public static String replaceUmlaut(String src) {
        String result = src;
        for (String[] umlautReplacement : umlautReplacements) {
            result = result.replace(umlautReplacement[0], umlautReplacement[1]);
        }
        return result;
    }

    public static String rtrim(String value) {
        return value != null ? value.replaceAll("\\s+$", Constants.EMPTY_STRING) : null;
    }

    public static String stripNonAlphaNumeric(String value) {
        return value != null ? value.replaceAll("[^A-Za-z0-9]", "") : "";
    }

    public static String textToHtml(String value) {
        return value != null ? StringEscapeUtils.escapeHtml4(value).replaceAll(" {2}", "&nbsp; ").replaceAll("\n", "<br/>").replaceAll("\r", "<br/>") : null;
    }

    public static String toGreenRed(boolean value) {
        return value ? Constants.COLOR_GREEN : Constants.COLOR_RED;
    }

    public static String toLowerCase(String value) {
        return isNotEmpty(value) ? value.toLowerCase() : value;
    }

    public static String toUpperCase(String value) {
        return isNotEmpty(value) ? value.toUpperCase() : value;
    }

    public static String toYesNo(Boolean value) {
        return value != null ? value ? Messages.get("YES") : Messages.get("NO") : null;
    }

    public static String transformColumnName(String key) {
        String ret = null;

        if (key != null) {
            StringBuilder result = new StringBuilder(key);

            // set first letter to upper case
            result.replace(0, 1, result.substring(0, 1).toUpperCase());

            // replace underscores
            for (int i = result.indexOf("_"); i >= 0; i = result.indexOf("_")) {
                result.replace(i, i + 1, " "); // replace any underscore by a space
            }

            ret = result.toString();

            // split camel case
            ret = ret.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
        }

        return ret;
    }

    public static String transformKeyName(String value) {
        return transformKeyName(value, null);
    }

    public static String transformKeyName(String value, String suffix) {
        String ret = null;
        if (value != null) {
            // eliminated spaces
            ret = value.replaceAll(" ", Constants.EMPTY_STRING);
            // set first letter to lower case
            ret = firstLower(ret);
            // add suffix if not null
            if (suffix != null) {
                ret = ret + "_" + suffix;
            }
        }
        return ret;
    }

    public static String trim(String value) {
        return value != null ? itrim(ltrim(rtrim(misctrim(value.trim())))) : null;
    }

    public static String trimBoth(String value) {
        return value != null ? value.replaceAll("^[ \\t\\n\\r]*", Constants.EMPTY_STRING).replaceAll("[ \\t\\n\\r]*$", Constants.EMPTY_STRING) : null;
    }

    public static String trimCommentText(String value) {
        String ret = removeDoubleEmptyLines(trimText(trimBoth(value)));
        return ret != null && !ret.isEmpty() ? ret : null;
    }

    public static String trimEmptyLinesAndControlCharacters(String value) {
        return trimBoth(removeDoubleEmptyLines(removeControlCharacters(value)));
    }

    public static String trimText(String value) {
        if (value != null) {
            // Regular expression to detect white spaces (not all possible one; just a major subset).
            String chars = "\\s|\u00A0|<br/>";
            String whiteSpaces = "((<\\w+( \\w+=\".*\")*>)+(" + chars + ")*(</\\w+>)+|" + chars + ")";
            String whiteSpacesLeading = "^" + whiteSpaces + "*((<\\w+( \\w+=\".*\")*>)+(" + chars + ")*(</\\w+>)+|\n|\r)+";
            String whiteSpacesTrailing = whiteSpaces + "+$";
            // Remove leading and trailing white spaces, including non-breaking spaces.
            return value.replaceAll(whiteSpacesLeading, Constants.EMPTY_STRING).replaceAll(whiteSpacesTrailing, Constants.EMPTY_STRING);
        }
        return null;
    }

    public static String truncate(String value) {
        return truncate(value, 30, 30);
    }

    public static String truncate(String value, int prefixLength) {
        return truncate(value, prefixLength, "...");
    }

    public static String truncate(String value, int prefixLength, int suffixLength) {
        return truncate(value, prefixLength, suffixLength, "...");
    }

    public static String truncate(String value, int prefixLength, int suffixLength, String separator) {
        if (value != null) {
            int truncPrefixLength = Math.max(prefixLength, 0);
            int truncSuffixLength = Math.max(suffixLength, 0);
            int truncLength = truncPrefixLength + truncSuffixLength;
            String truncSeparator = getSeparator(separator);
            if (truncLength == 0 || value.length() <= truncLength + truncSeparator.length()) {
                return value;
            }
            String prefix = value.substring(0, truncPrefixLength);
            String suffix = value.substring(value.length() - truncSuffixLength);
            return prefix + truncSeparator + suffix;
        }
        return null;
    }

    public static String truncate(String value, int prefixLength, String terminator) {
        return truncate(value, prefixLength, 0, terminator);
    }

    @SuppressWarnings("SameReturnValue")
    public boolean validateNameValue(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        if (value != null && ((String) value).contains("%")) {
            throw new BfabricValidatorException("errorInvalidPercentCharacter");
        }
        if (isEmpty((String) value)) {
            throw new BfabricValidatorException("errorNullOrEmptyValue");
        }
        return true;
    }

    @SuppressWarnings("SameReturnValue")
    public boolean validateNumber(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        if (value != null && !NumberUtils.isCreatable(value.toString())) {
            throw new BfabricValidatorException("numberValidationException");
        }
        return true;
    }
}