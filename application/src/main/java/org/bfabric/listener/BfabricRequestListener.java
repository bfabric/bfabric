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

package org.bfabric.listener;

import java.util.logging.Logger;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

// Important: Do not delete! The Web listener is currently used for testing purposes to see when a request is created or destroyed.
//@WebListener
public class BfabricRequestListener implements ServletRequestListener {

    private static final Logger logger = Logger.getLogger(BfabricRequestListener.class.getName());

    @Override
    public void requestDestroyed(ServletRequestEvent event) {
        logger.finer("RequestListener requestDestroyed");
    }

    @Override
    public void requestInitialized(ServletRequestEvent event) {
        logger.finer("RequestListener requestInitialized");
    }
}
