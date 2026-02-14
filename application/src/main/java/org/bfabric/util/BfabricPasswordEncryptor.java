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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class BfabricPasswordEncryptor {

    private static final Logger logger = Logger.getLogger(BfabricPasswordEncryptor.class.getName());

    private PublicKey publicKey;

    public BfabricPasswordEncryptor() {
    }

    private BfabricPasswordEncryptor(PublicKey publicKey) {
        setPublicKey(publicKey);
    }

    public String encrypt(char[] password) {
        // Important: Ensure that after this call the input parameter password is cleared, i.e., filled with zeros, if not used anymore!
        String ret = null;
        if (getPublicKey() != null && password != null) {
            try {
                byte[] input = StringHelper.charsToBytes(password);
                byte[] cipherData = encrypt(getPublicKey(), input);
                // Clear sensitive data.
                Arrays.fill(input, (byte) 0);
                ret = Base64.getEncoder().encodeToString(cipherData);
            } catch (Exception e) {
                logger.severe(e.getMessage());
            }
        } else {
            if (publicKey == null) {
                logger.severe("PasswordAD encryptor requires publicKey!");
            }
            if (password == null) {
                logger.severe("PasswordAD encryptor received null word!");
            }
        }
        return ret;
    }

    private byte[] encrypt(PublicKey aPublicKey, byte[] password) throws NoSuchAlgorithmException,
        NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, aPublicKey);
        return cipher.doFinal(password);
    }

    public BfabricPasswordEncryptor getInstance() {
        BfabricPasswordEncryptor ret = null;
        String errorMsg = "PasswordAD encryptor is not available! ";
        synchronized (BfabricPasswordEncryptor.class) {
            String publicKeyFilePath = ConfigurationHelper.getConfiguration().getPwEncPublicKeyFilePath();
            String errorMsgPrefix = "Public key file ";
            if (StringHelper.isNotEmpty(publicKeyFilePath)) {
                errorMsgPrefix += publicKeyFilePath + " ";
                try {
                    // Check if the public key file exists and can be read.
                    File publicKeyFile = new File(publicKeyFilePath);
                    if (!publicKeyFile.exists()) {
                        errorMsg += errorMsgPrefix + "not found!";
                    } else if (!publicKeyFile.canRead()) {
                        errorMsg += "Public key file  " + publicKeyFilePath + "cannot be read!";
                    } else {
                        ret = new BfabricPasswordEncryptor(readPublicKey(publicKeyFilePath));
                    }
                } catch (InvalidKeySpecException e) {
                    errorMsg += errorMsgPrefix + "is corrupt or outdated!";
                } catch (Exception e) {
                    logger.severe(e.getMessage());
                }
            }
        }
        if (ret == null) {
            logger.severe(errorMsg);
        }
        return ret;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    private PublicKey readPublicKey(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(Files.readAllBytes(Paths.get(filename)));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(publicSpec);
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }
}