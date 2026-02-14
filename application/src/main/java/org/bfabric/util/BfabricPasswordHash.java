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

import java.util.Arrays;

import javax.security.enterprise.identitystore.PasswordHash;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

public class BfabricPasswordHash implements PasswordHash {

    private static final Pbkdf2PasswordEncoder encoder = Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    public BfabricPasswordHash() {
    }

    public static String encode(char[] password) {
        return new BfabricPasswordHash().generate(password);
    }

    public static String encode(String password) {
        return password != null ? new BfabricPasswordHash().generate(password.toCharArray()) : null;
    }

    @Override
    public String generate(char[] password) {
        return encoder.encode(new String(password));
    }

    public String md5Hex(char[] password) {
        byte[] input = StringHelper.charsToBytes(password);
        String md5Hex = DigestUtils.md5Hex(input);
        // Clear sensitive data.
        Arrays.fill(input, (byte) 0);
        return md5Hex;
    }

    @Override
    public boolean verify(char[] password, String encodedPassword) {
        // long startTime = System.currentTimeMillis();
        return encodedPassword != null && (encodedPassword.equals(new String(password)) || encoder.matches(new String(password), encodedPassword) || encodedPassword.equals(md5Hex(password)));
        // System.out.println("verify (ms): " + (System.currentTimeMillis() - startTime) + " password=" + new String(password) + " encodedPassword=" + encodedPassword);
    }
}