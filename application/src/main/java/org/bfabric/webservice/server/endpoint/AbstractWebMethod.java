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

package org.bfabric.webservice.server.endpoint;

import java.util.List;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.Transient;
import javax.security.enterprise.AuthenticationStatus;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.lang3.StringUtils;
import org.bfabric.Constants;
import org.bfabric.entity.WebServiceLog;
import org.bfabric.enums.LogStatusEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.exception.WSException;
import org.bfabric.service.EntityService;
import org.bfabric.service.IdentityService;
import org.bfabric.util.ConfigurationHelper;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.request.AbstractXMLAuthBaseRequest;
import org.bfabric.webservice.response.XMLResponse;
import org.bfabric.xml.JAXBMarshaller;

public abstract class AbstractWebMethod {

    // private static final Logger logger = Logger.getLogger(AbstractWebMethod.class.getName());

    @Transient
    private List<String> currentUserRoleNames;

    private EntityService entityService;

    private IdentityService identityService;

    private WebServiceContext wsContext;

    private AbstractXMLAuthBaseRequest xmlRequest;

    public AbstractWebMethod() {
    }

    public AbstractWebMethod(WebServiceContext wsContext, AbstractXMLAuthBaseRequest xmlRequest) {
        this.wsContext = wsContext;
        this.xmlRequest = xmlRequest;
    }

    public XMLResponse execute() {
        XMLResponse xmlResponse = new XMLResponse();
        try {
            if (xmlRequest != null && StringHelper.isNotEmpty(xmlRequest.getLogin()) && StringHelper.isNotEmpty(xmlRequest.getPassword())) {
                // long startTime = System.currentTimeMillis();
                MessageContext mc = wsContext.getMessageContext();
                HttpServletRequest request = (HttpServletRequest) mc.get(MessageContext.SERVLET_REQUEST);
                HttpServletResponse response = (HttpServletResponse) mc.get(MessageContext.SERVLET_RESPONSE);
                // System.out.println("request.getRequestURL()="+request.getRequestURL());
                ConfigurationHelper.getConfiguration().setBaseUrl(request.getRequestURL().substring(0, request.getRequestURL().lastIndexOf("/") + 1));
                String entityClassName = request.getServletPath().replaceAll("/", Constants.EMPTY_STRING).toLowerCase();
                String action = xmlRequest.getClass().getSimpleName().replaceAll("XMLRequest", Constants.EMPTY_STRING).toLowerCase();
                if (action.endsWith(entityClassName)) {
                    action = action.substring(0, action.lastIndexOf(entityClassName));
                }
                String log = StringHelper.format(StringUtils.substringBefore(StringUtils.substringAfter(JAXBMarshaller.getXmlAsText(xmlRequest), "</password>"), "</xmlRequest"));
                // Remove content of elements that should NOT be part of the web service log!
                if (log != null) {
                    log = log.replaceAll("(<base64>).+(</base64>)", "<base64></base64>");
                }
                WebServiceLog webServiceLog = new WebServiceLog(xmlRequest.getLogin(), LogStatusEnum.INVOKED, entityClassName, action, log);
                // System.out.println("WebMethod " + action + " on " + entityClassName + " called by " + xmlRequest.getLogin() + " log=" + log + " xmlRequest=" + JAXBMarshaller.getXmlAsText(xmlRequest));
                AuthenticationStatus status = getIdentityService().authenticateWS(request, response, xmlRequest.getLogin(), xmlRequest.getPassword());
                if (!status.equals(AuthenticationStatus.SUCCESS)) {
                    webServiceLog.setLog("<error>user " + xmlRequest.getLogin() + " could not login</error>" + webServiceLog.getLog());
                    getEntityService().persist(webServiceLog);
                    throw new WSException("Invalid login or password. Could not login.");
                }
                if (!hasPermission()) {
                    webServiceLog.setLog("<error>user " + xmlRequest.getLogin() + " has no permission</error>" + webServiceLog.getLog());
                    getEntityService().persist(webServiceLog);
                    throw new WSException("Invalid login: only admins, application managers and feeders can use this web service. " + getIdentityService().getCurrentUsername());
                } else {
                    getEntityService().persist(webServiceLog);
                }
                // long relativeTime = System.currentTimeMillis();
                webServiceLog.setStart();
                xmlResponse = performOperation();
                webServiceLog.setDone();
                getEntityService().merge(webServiceLog);
                getIdentityService().logout(request, false);
                // System.out.println(webServiceLog + "\nExecution total time (ms): " + (System.currentTimeMillis() - startTime) + " Before Perform: " + (relativeTime - startTime));
            }
        } catch (WSException e) {
            xmlResponse.setErrorreport(e.getMessage());
        }
        return xmlResponse;
    }

    public List<String> getCurrentUserRoleNames() {
        if (currentUserRoleNames == null) {
            currentUserRoleNames = identityService.getCurrentUserRoleNames();
        }
        return currentUserRoleNames;
    }

    protected EntityService getEntityService() {
        if (entityService == null) {
            entityService = CDI.current().select(EntityService.class).get();
        }
        return entityService;
    }

    protected IdentityService getIdentityService() {
        if (identityService == null) {
            identityService = CDI.current().select(IdentityService.class).get();
        }
        return identityService;
    }

    public boolean hasCurrentUserRoleEnum(RoleEnum roleEnum) {
        return getCurrentUserRoleNames() != null && roleEnum != null && getCurrentUserRoleNames().contains(roleEnum.getName());
    }

    protected boolean hasPermission() {
        return hasCurrentUserRoleEnum(RoleEnum.WEBSERVICEUSER);
    }

    protected abstract XMLResponse performOperation();
}
