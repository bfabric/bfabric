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

package org.bfabric.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.bfabric.interceptors.MeasureCallsInterceptor;

/**
 * The MeasureCalls class is used to measure the time it takes to execute intercepted methods of a component.
 * Note: This filter is registered via the WebListener BfabricServletContextListener only when B-Fabric is running in local mode!
 * To unconditionally switch on this filter, you could use the @WebFilter annotation below which is now commented out.
 */
//@WebFilter(urlPatterns = { "*.html" })
public class MeasureCallsFilter implements Filter {

    private static final Logger logger = Logger.getLogger(MeasureCallsFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        MeasureCallsInterceptor.callChain.get().clear();

        chain.doFilter(request, response);

        List<MeasureCalls> invocations = new ArrayList<>(MeasureCallsInterceptor.callChain.get().values());

        if (!invocations.isEmpty()) {
            Collections.sort(invocations);
            logger.fine("\n" + StringUtils.join(invocations, "\n"));
        }
    }
}
