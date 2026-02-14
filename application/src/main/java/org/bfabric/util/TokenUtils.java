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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class TokenUtils extends ConfigurationHelper {

    private static final Charset charset = StandardCharsets.UTF_8;

    private static final String messageDigestAlgorithm = "SHA-1";

    private static final Integer keyLength = 16;

    private static final String encryptionAlgorithm = "AES/ECB/PKCS5PADDING";

    private static final String secret = "Fz23t9e2KA";

    private static final String secretKey = "AES";

    private static final Logger logger = Logger.getLogger(TokenUtils.class.getName());

    private static SecretKeySpec secretKeySpec;

    public static String decrypt(String value) {
        try {
            setKey(secret);
            Cipher cipher = getCipher();
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return new String(cipher.doFinal(Base64.getUrlDecoder().decode(value)), charset);
        } catch (Exception e) {
            logger.info("Error while decrypting: " + e);
        }
        return null;
    }

    public static String encrypt(String value) {
        try {
            setKey(secret);
            Cipher cipher = getCipher();
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return Base64.getUrlEncoder().encodeToString(cipher.doFinal(value.getBytes(charset)));
        } catch (Exception e) {
            logger.info("Error while encrypting: " + e);
        }
        return null;
    }

    private static Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance(encryptionAlgorithm);
    }

    public static void setKey(String key) {
        try {
            byte[] byteKey = key.getBytes(charset);
            byteKey = MessageDigest.getInstance(messageDigestAlgorithm).digest(byteKey);
            byteKey = Arrays.copyOf(byteKey, keyLength);
            secretKeySpec = new SecretKeySpec(byteKey, secretKey);
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
    }

    public String getCalendarSyncUrl(String parameter) {
        return getConfiguration().getBaseUrl() + "rest/calendar/sync?token=" + encrypt(parameter);
    }

    public String getToken(String parameter) {
        return "token=" + encrypt(parameter);
    }

    public String getTokenValidateUrl(String parameter) {
        return getConfiguration().getBaseUrl() + "rest/token/validate?token=" + encrypt(parameter);
    }
}