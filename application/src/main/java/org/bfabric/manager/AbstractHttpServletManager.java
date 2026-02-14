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

package org.bfabric.manager;

import java.io.Serializable;

import javax.enterprise.inject.spi.CDI;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bfabric.entity.Configuration;
import org.bfabric.enums.RoleEnum;
import org.bfabric.interceptors.CachedMethodResult;

public abstract class AbstractHttpServletManager implements Serializable {

    private static final long serialVersionUID = 1;

    private ConfManager confManager;

    private Configuration configuration;

    public String getBaseURL() {
        String requestURL = getRequestURL();
        String requestURI = getRequestURI();
        String contextPath = getContextPath();
        return requestURL != null && requestURI != null && contextPath != null ? requestURL.substring(0, requestURL.indexOf(requestURI)) + contextPath + "/" : "";
    }

    public ConfManager getConfManager() {
        if (confManager == null) {
            confManager = CDI.current().select(ConfManager.class).get();
        }
        return confManager;
    }

    public Configuration getConfiguration() {
        if (configuration == null && getConfManager() != null) {
            configuration = getConfManager().getConfiguration();
        }
        return configuration;
    }

    public String getContextPath() {
        HttpServletRequest httpServletRequest = getHttpServletRequest();
        return httpServletRequest == null ? null : httpServletRequest.getContextPath();
    }

    public HttpServletRequest getHttpServletRequest() {
        return FacesContext.getCurrentInstance() == null ? null : (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
    }

    public HttpServletResponse getHttpServletResponse() {
        return FacesContext.getCurrentInstance() == null ? null : (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
    }

    public HttpSession getHttpSession() {
        HttpServletRequest httpServletRequest = getHttpServletRequest();
        return httpServletRequest == null ? null : httpServletRequest.getSession();
    }

    public String getRequestURI() {
        HttpServletRequest httpServletRequest = getHttpServletRequest();
        return httpServletRequest == null ? null : httpServletRequest.getRequestURI();
    }

    public String getRequestURL() {
        HttpServletRequest httpServletRequest = getHttpServletRequest();
        return httpServletRequest == null ? null : httpServletRequest.getRequestURL().toString();
    }

    public String getRequestURLDetails() {
        HttpServletRequest httpServletRequest = getHttpServletRequest();
        if (httpServletRequest == null) {
            return null;
        }
        String requestURL = httpServletRequest.getRequestURL().toString();
        String queryString = httpServletRequest.getQueryString();
        return queryString == null ? requestURL : requestURL + "?" + queryString;
    }

    @CachedMethodResult
    public boolean hasCurrentUserRoleEnum(RoleEnum roleEnum) {
        return getConfiguration().hasCurrentUserRoleEnum(roleEnum);
    }

    public boolean isShowScreenUrl() {
        String requestURI = getRequestURI();
        return requestURI != null && requestURI.contains("/show.html") && !requestURI.contains("eventschedule/show.html") && !requestURI.contains("instrumentschedule/show.html");
    }
}
