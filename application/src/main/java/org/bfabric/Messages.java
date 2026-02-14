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

package org.bfabric;

import java.io.Serializable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;

import org.bfabric.util.StringHelper;

@Startup
@ApplicationScoped
public class Messages implements Serializable {

    private static final Logger logger = Logger.getLogger(Messages.class.getName());

    private static final long serialVersionUID = 1;

    public static String get(String key) {
        try {
            return ResourceBundle.getBundle("messages").getString(key);
        } catch (MissingResourceException e) {
            if (StringHelper.isNotEmpty(key)) {
                logger.warning("Key " + key + " does not exist in messages.properties.");
            } else {
                logger.fine("Empty/null key passed to Messages.get() - called from: " +
                    Thread.currentThread().getStackTrace()[2].getClassName() + "." +
                    Thread.currentThread().getStackTrace()[2].getMethodName() + ":" +
                    Thread.currentThread().getStackTrace()[2].getLineNumber());
            }
            String ret = Constants.EMPTY_STRING;
            if (key != null && (key.toLowerCase().endsWith("error") || key.toLowerCase().endsWith("exception"))) {
                ret = "!!!";
            }
            return ret;
        }
    }
}
