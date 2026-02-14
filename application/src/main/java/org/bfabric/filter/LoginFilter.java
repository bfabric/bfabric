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
import java.util.Arrays;

import javax.faces.application.ResourceHandler;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bfabric.Constants;
import org.bfabric.manager.SessionManager;
import org.bfabric.service.IdentityService;
import org.bfabric.util.ClassHelper;

/**
 * LoginFilter intercepts a page request and redirects to the login screen whenever a login is required. Note that in case of an ajax request the redirection is not performed to avoid that a timed-out user loses his data edited in a form.
 */
@WebFilter(urlPatterns = { "*.html", "*.pdf" })
public class LoginFilter implements Filter {

    @Inject
    private IdentityService identityService;

    @Inject
    private SessionManager sessionManager;

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        if (httpServletRequest.getSession() != null) {
            String contextPath = httpServletRequest.getContextPath();
            String requestUri = httpServletRequest.getRequestURI();
            String relativePath = requestUri.replace(contextPath, Constants.EMPTY_STRING);
            if (httpServletRequest.getQueryString() != null) {
                requestUri += "?" + httpServletRequest.getQueryString();
            }
            // Note: Special treatment for the user/edit screen which does not require login when invoked with no parameters to create a new user!
            if (identityService.isLoggedIn() || isLoginNotRequired(relativePath) || relativePath.equals("/user/edit.html") && httpServletRequest.getQueryString() == null) {
                chain.doFilter(httpServletRequest, response);
            } else {
                if (!"partial/ajax".equals(httpServletRequest.getHeader("Faces-Request"))) {
                    // Cache original requestUri for redirection after login.
                    sessionManager.setOriginalUrl(requestUri.replace(contextPath, Constants.EMPTY_STRING));
                    ((HttpServletResponse) response).sendRedirect(contextPath);
                }
            }
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    public boolean isExcluded(String relativePath) {
        return ClassHelper.getWsEndPoints().contains(relativePath) || relativePath.startsWith("/css") || relativePath.startsWith("/js") || relativePath.startsWith("/images") || relativePath
            .equals("/fragments/downloadmanager.jar") || relativePath.startsWith("/manifest.webmanifest") || relativePath.startsWith(ResourceHandler.RESOURCE_IDENTIFIER);
    }

    public boolean isLoginNotRequired(String relativePath) {
        // The pages which do not require that the user is logged in.
        String[] loginNotRequiredPages = { "/home.html", "/project.html", "/user/activate.html", "/user/password-lost.html", "/user/reset-password.html", "/user/unsubscribe.html",
            "/shibboleth/login.html",
            "/shibboleth/account-map.html", "/shibboleth/login-failed.html" };
        return Arrays.asList(loginNotRequiredPages).contains(relativePath) || isExcluded(relativePath);
    }
}