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
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bfabric.service.EntityLogService;

@WebFilter(urlPatterns = { "/logdownloadstatus" })
public class DownloadLogFilter implements Filter {

    private static final Logger logger = Logger.getLogger(DownloadLogFilter.class.getName());

    @Inject
    private EntityLogService entityLogService;

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {
        Long id = null;
        String code = null;
        String status = null;
        String ip = null;
        String summary = null;
        if (!request.getParameterMap().isEmpty()) {
            final Enumeration<String> parameterNames = request.getParameterNames();
            while (parameterNames.hasMoreElements()) {
                String param = parameterNames.nextElement();
                switch (param) {
                case "id":
                    try {
                        id = Long.parseLong(request.getParameter("id"));
                    } catch (Exception e) {
                        id = null;
                    }
                    break;
                case "code":
                    code = request.getParameter("code");
                    break;
                case "status":
                    status = request.getParameter("status");
                    break;
                case "ip":
                    ip = request.getParameter("ip");
                    break;
                case "summary":
                    summary = request.getParameter("summary");
                    break;
                default:
                    break;
                }
            }
            if (id != null && code != null && status != null && ip != null) {
                try {
                    entityLogService.logDownloadStatus(id, code, status, ip, summary);
                } catch (Exception e) {
                    logger.warning("Error logging download status: " + e.getMessage());
                }
            }
        }
        ((HttpServletResponse) response).sendRedirect(((HttpServletRequest) request).getContextPath() + "/entitylog/list.html");
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }
}
