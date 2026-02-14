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

import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.bfabric.Messages;
import org.bfabric.manager.SessionTimeoutManager;
import org.bfabric.service.IdentityService;
import org.primefaces.PrimeFaces;

public class BfabricPhaseListener implements PhaseListener {

    private static final Logger logger = Logger.getLogger(BfabricPhaseListener.class.getName());

    private static final long serialVersionUID = 1;

    private IdentityService identityService;

    private long phaseStartTime;

    @Inject
    private SessionTimeoutManager sessionTimeoutManager;

    private boolean showAfterPhaseEnabled = true;

    private boolean showBeforePhaseEnabled = true;

    @Override
    public void afterPhase(PhaseEvent event) {
        // logger.fine("after phase " + event.getPhaseId());
        if (isShowAfterPhaseEnabled() && StringHelper.isNotEmpty(getIdentityService().getCurrentUsername()) && !isTimerRequest()) {
            String duration = StringUtils.leftPad((System.nanoTime() - getPhaseStartTime()) / 1e6 + " ms", 15);
            logger.info("\n" + duration + " " + getIdentityService().getCurrentUsername() + " -> " + getRequestFullUrl());
        }
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        if (StringHelper.isNotEmpty(getIdentityService().getCurrentUsername()) && !isTimerRequest()) {
            identityService.updateUserLastActionTime();
        }

        if (isShowAfterPhaseEnabled()) {
            setPhaseStartTime(System.nanoTime());
        }

        if (FacesContext.getCurrentInstance().isValidationFailed()) {
            // Add message validationErrors in case it is not a virusException.
            boolean virusException = false;
            Iterator<FacesMessage> facesMessages = FacesContext.getCurrentInstance().getMessages();
            while (facesMessages.hasNext()) {
                FacesMessage facesMessage = facesMessages.next();
                if (facesMessage.getSummary().startsWith("ClamAV")) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, facesMessage.getSummary(), null));
                    virusException = true;
                    break;
                }
            }
            if (!virusException) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, Messages.get("validationErrors"), null));
            }

            if (isShowBeforePhaseEnabled()) {
                StringBuilder messages = new StringBuilder("-- Client Messages if FacesContext.getCurrentInstance().isValidationFailed() -----------------");
                Iterator<String> clientIdsWithMessages = FacesContext.getCurrentInstance().getClientIdsWithMessages();
                while (clientIdsWithMessages.hasNext()) {
                    String clientId = clientIdsWithMessages.next();
                    for (FacesMessage message : FacesContext.getCurrentInstance().getMessageList(clientId)) {
                        messages.append("\nMessage detail for clientId ").append(clientId).append(" -> ").append(message.getDetail());
                        messages.append("\nMessage summary for clientId ").append(clientId).append(" -> ").append(message.getSummary());
                        messages.append("\nMessage severity for clientId ").append(clientId).append(" -> ").append(message.getSeverity());
                    }
                }
                messages.append("\n--------------------------------------------------------------------------------------------");
                logger.fine(messages.toString());
            }
        }

        String facesSource = getFacesSource();
        if (facesSource != null) {
            // At this point, we have an ajax request (partial/ajax): ignore ajax requests caused by timer.
            if (!isTimerRequest()) {
                // logger.fine("isTimerRequest=" + isTimerRequest() + " facesSource=" + facesSource);
                boolean timeoutWarning = sessionTimeoutManager.isTimeoutWarning();
                sessionTimeoutManager.setLastAccessedTime();
                if (timeoutWarning) {
                    // Execute script timeoutAlertUpdate to re-render the timeout message bar!
                    PrimeFaces.current().executeScript("timeoutAlertUpdate()");
                }
            }
        } else {
            sessionTimeoutManager.setLastAccessedTime();
        }
    }

    public String getFacesSource() {
        Object request = FacesContext.getCurrentInstance().getExternalContext().getRequest();
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            if ("partial/ajax".equals(httpRequest.getHeader("Faces-Request"))) {
                return httpRequest.getParameter("javax.faces.source");
            }
        }
        return null;
    }

    public IdentityService getIdentityService() {
        if (identityService == null) {
            identityService = CDI.current().select(IdentityService.class).get();
        }
        return identityService;
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }

    public long getPhaseStartTime() {
        return phaseStartTime;
    }

    private String getRequestFullUrl() {
        StringBuilder urlBuilder = new StringBuilder();
        Object request = FacesContext.getCurrentInstance().getExternalContext().getRequest();
        try {
            if (request instanceof HttpServletRequest) {
                HttpServletRequest servletRequest = (HttpServletRequest) request;
                urlBuilder.append(servletRequest.getRequestURL().toString());
                if (StringHelper.isNotEmpty(servletRequest.getQueryString())) {
                    urlBuilder.append("?").append(servletRequest.getQueryString());
                }

                if (ConfigurationHelper.getConfiguration().isEnvironmentLocal()) {
                    Enumeration<String> parameterNames = servletRequest.getParameterNames();
                    String parameterName;
                    while (parameterNames.hasMoreElements()) {
                        parameterName = parameterNames.nextElement();
                        if (!parameterName.toLowerCase().contains("loginname") && !parameterName.toLowerCase().contains("password")) {
                            urlBuilder.append(" ").append(parameterName).append("=").append(servletRequest.getParameter(parameterName));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return urlBuilder.toString();
    }

    public boolean isShowAfterPhaseEnabled() {
        return showAfterPhaseEnabled;
    }

    public boolean isShowBeforePhaseEnabled() {
        return showBeforePhaseEnabled;
    }

    public boolean isTimerRequest() {
        String facesSource = getFacesSource();
        return facesSource != null && (facesSource.contains("timeoutAlert") || facesSource.contains("timeoutTimer"));
    }

    public void setPhaseStartTime(long phaseStartTime) {
        this.phaseStartTime = phaseStartTime;
    }

    public void setShowAfterPhaseEnabled(boolean showAfterPhaseEnabled) {
        this.showAfterPhaseEnabled = showAfterPhaseEnabled;
    }

    public void setShowBeforePhaseEnabled(boolean showBeforePhaseEnabled) {
        this.showBeforePhaseEnabled = showBeforePhaseEnabled;
    }
}
