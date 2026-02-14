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
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Container;
import org.bfabric.manager.SessionManager;
import org.bfabric.service.ContainerService;
import org.bfabric.util.MessageHelper;
import org.bfabric.util.UriHelper;

@WebFilter(urlPatterns = { "/agenda/*", "/common/*", "/service/show-order.html", "/userlab/*", "/container/show.html" })
public class MaskFilter implements Filter {

    @Inject
    private ContainerService containerService;

    @Inject
    private SessionManager sessionManager;

    private String createMaskedUrl(WrappedRequest wrappedRequest) {
        String contextPath = wrappedRequest.getContextPath();
        String relativePath = wrappedRequest.getRequestURI().replace(contextPath, Constants.EMPTY_STRING);

        StringBuilder maskedUrl = new StringBuilder();
        switch (relativePath) {
        case "/agenda/show-event.html":
            maskedUrl.append("/event/show.html");
            break;
        case "/agenda/show-credit.html":
            maskedUrl.append("/credit/show.html");
            break;
        case "/agenda/show-eventtype.html":
            maskedUrl.append("/eventtype/show.html");
            break;
        case "/common/activate.html":
            maskedUrl.append("/user/activate.html");
            break;
        case "/common/process-access-request.html":
            maskedUrl.append("/accessrequest/process.html");
            break;
        case "/common/request-access.html":
            maskedUrl.append("/accessrequest/request-access.html");
            break;
        case "/common/reset-password.html":
            maskedUrl.append("/user/reset-password.html");
            break;
        case "/common/show-contract.html":
            maskedUrl.append("/contract/show.html");
            break;
        case "/common/show-instrument.html":
            maskedUrl.append("/instrument/show.html");
            break;
        case "/common/show-purchase.html":
            maskedUrl.append("/purchase/show.html");
            break;
        case "/common/show-user.html":
            maskedUrl.append("/user/show.html");
            break;
        case "/common/submit-feedback.html":
            maskedUrl.append("/feedack/submit.html");
            break;
        case "/service/show-order.html":
        case "/userlab/show-projectorder.html":
            maskedUrl.append("/order/show.html");
            break;
        case "/userlab/show-dataset.html":
            maskedUrl.append("/dataset/show.html");
            break;
        case "/userlab/show-instrumentreservation.html":
            maskedUrl.append("/instrumentreservation/show.html");
            break;
        case "/userlab/show-project.html":
            maskedUrl.append("/project/show.html");
            break;
        case "/userlab/show-resource.html":
            maskedUrl.append("/resource/show.html");
            break;
        case "/userlab/show-sample.html":
            maskedUrl.append("/sample/show.html");
            break;
        case "/userlab/show-workunit.html":
            maskedUrl.append("/workunit/show.html");
            break;
        case "/container/show.html":
            maskedUrl.append(relativePath);
            break;
        default:
            // NOTE: Redirect all other cases to corresponding error page or generalize filter such that more url are masked!
            break;
        }

        String replaceContainerURL = null;
        if (!maskedUrl.toString().isEmpty() && !wrappedRequest.getParameterMap().isEmpty()) {
            maskedUrl.append("?");
            final Enumeration<String> parameterNames = wrappedRequest.getParameterNames();
            while (parameterNames.hasMoreElements()) {
                String param = parameterNames.nextElement();

                Long id = null;
                if (param.equals("id")) {
                    try {
                        id = Long.parseLong(wrappedRequest.getParameter("id"));
                    } catch (Exception ignored) {
                    }
                }
                if (relativePath.equals("/container/show.html") && id != null) {
                    Container container = containerService.find(Container.class, id);
                    if (container != null) {
                        replaceContainerURL = "/" + container.getEntityName().toLowerCase() + "/show.html";
                    }
                }
                if (relativePath.equals("/service/show-order.html") && id != null) {
                    Long containerIdByOldServiceOrderId = containerService.getContainerIdByOldServiceOrderId(id);
                    if (containerIdByOldServiceOrderId != null) {
                        maskedUrl.append(param).append("=").append(containerService.getContainerIdByOldServiceOrderId(id));
                    } else {
                        sessionManager.addFacesMessage(new MessageHelper(FacesMessage.SEVERITY_WARN, Messages.get("oldServiceOrderIdNotFound").replace("{0}", String.valueOf(id))));
                        sessionManager.setRedirectURL("/error/entity-not-found.html");
                    }
                } else if (relativePath.equals("/userlab/show-projectorder.html") && id != null) {
                    Long containerIdByOldProjectOrderId = containerService.getContainerIdByOldProjectOrderId(id);
                    if (containerIdByOldProjectOrderId != null) {
                        maskedUrl.append(param).append("=").append(containerService.getContainerIdByOldProjectOrderId(id));
                    } else {
                        sessionManager.addFacesMessage(new MessageHelper(FacesMessage.SEVERITY_WARN, Messages.get("oldProjectOrderIdNotFound").replace("{0}", String.valueOf(id))));
                        sessionManager.setRedirectURL("/error/entity-not-found.html");
                    }
                } else {
                    maskedUrl.append(param).append("=").append(wrappedRequest.getParameter(param));
                }
                if (parameterNames.hasMoreElements()) {
                    maskedUrl.append("&");
                }
            }
        }

        String maskedTargetUrl = contextPath + maskedUrl;
        if (!maskedTargetUrl.isEmpty()) {
            maskedTargetUrl = UriHelper.removeCid(maskedTargetUrl);
        }

        return replaceContainerURL != null && maskedTargetUrl != null ? maskedTargetUrl.replace("/container/show.html", replaceContainerURL) : maskedTargetUrl;
    }

    @Override
    public void destroy() {
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        WrappedRequest wrappedRequest = new WrappedRequest((HttpServletRequest) request);
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.sendRedirect(createMaskedUrl(wrappedRequest));
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    private static class WrappedRequest extends HttpServletRequestWrapper {

        private final Map<String, String[]> modifiableParameters = new TreeMap<>();

        private Map<String, String[]> allParameters = null;

        public WrappedRequest(final HttpServletRequest request) {
            super(request);
            if (!super.getParameterMap().containsKey("id")) {
                List<String> ids = Arrays.asList("eventId", "contractId", "userId", "orderId", "instrumentId", "instrumentReservationId", "projectId", "projectOrderId", "workunitId");
                for (String id : ids) {
                    if (super.getParameterMap().containsKey(id)) {
                        modifiableParameters.put("id", super.getParameterMap().get(id));
                    }
                }
            }
        }

        @Override
        public String getParameter(final String name) {
            String[] strings = getParameterMap().get(name);
            if (strings != null) {
                return strings[0];
            }
            return super.getParameter(name);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            if (allParameters == null) {
                allParameters = new TreeMap<>();
                allParameters.putAll(super.getParameterMap());
                allParameters.putAll(modifiableParameters);

                // Return an unmodifiable collection because we need to uphold the interface contract.
                allParameters = Collections.unmodifiableMap(allParameters);
            }
            return allParameters;
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(getParameterMap().keySet());
        }

        @Override
        public String[] getParameterValues(final String name) {
            return getParameterMap().get(name);
        }
    }
}