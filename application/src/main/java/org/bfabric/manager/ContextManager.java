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

import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.Container;
import org.bfabric.entity.User;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.UserService;
import org.bfabric.util.StringHelper;
import org.omnifaces.cdi.Param;
import org.primefaces.event.SelectEvent;
import org.primefaces.extensions.event.ClipboardErrorEvent;
import org.primefaces.extensions.event.ClipboardSuccessEvent;

@MeasureCalls
@Named
@ViewScoped
public class ContextManager extends AbstractManager {

    private static final Logger logger = Logger.getLogger(ContextManager.class.getName());

    private static final long serialVersionUID = 1;

    @Param
    protected Long containerId;

    private String containerFilterValue = null;

    private Container contextContainer;

    private String key;

    @Inject
    private UserService userService;

    public void contextContainerChanged(SelectEvent<Container> event) {
        Container container = event.getObject();
        if (container == null) {
            container = getContextContainer();
        }
        getSessionManager().redirectToContainer(container);
    }

    public void copyDownloadFolderCommandFailedListener(final ClipboardErrorEvent event) {
        FacesContext.getCurrentInstance()
            .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, Messages.get("copyDownloadFolderCommandFailed"), "ComponentId: " + event.getComponent().getId()));
    }

    public void copyDownloadFolderCommandSuccessfulListener(final ClipboardSuccessEvent event) {
        FacesContext.getCurrentInstance()
            .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, Messages.get("copyDownloadFolderCommandSuccessful"), "ComponentId: " + event.getComponent().getId()));
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, event.getText(), null));
    }

    public void copyUrlEventFailedListener(final ClipboardErrorEvent event) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, Messages.get("copyCalendarUrlFailed"), "ComponentId: " + event.getComponent().getId()));
    }

    public void copyUrlEventSuccessfulListener(final ClipboardSuccessEvent event) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, Messages.get("copyCalendarUrlSuccessful"), "ComponentId: " + event.getComponent().getId()));
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, event.getText(), null));
    }

    public String getContainerFilterValue() {
        if (containerFilterValue == null && getContextContainer() != null) {
            containerFilterValue = String.valueOf(getContextContainer().getId());
        }
        return containerFilterValue;
    }

    public Container getContextContainer() {
        // logger.fine("--- getContextContainer " + contextContainer);
        return contextContainer;
    }

    public String getKey() {
        return key;
    }

    @CachedMethodResult
    public int getListingRows() {
        return getSessionManager().getListingRows();
    }

    @PostConstruct
    public void init() {
        if (containerId != null) {
            setContextContainerById(containerId);
        } else if (identityManager.getCurrentUser() != null) {
            contextContainer = identityManager.getCurrentUser().getLastContainer();
        }
        // logger.fine("--- init contextmanager " + contextContainer);
    }

    public void keydownListener(AjaxBehaviorEvent event) {
        if (key != null && key.equals("Enter")) {
            Map<String, String> map = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            // Enter key was pressed.
            if (StringHelper.isNotEmpty(map.get("contextForm:contextautocomplete_hinput"))) {
                // Read the target from the auto complete form.
                try {
                    Container targetContainer = entityService.find(Container.class, Long.valueOf(map.get("contextForm:contextautocomplete_hinput")));
                    if (targetContainer != null) {
                        getSessionManager().redirectToContainer(targetContainer);
                    }
                } catch (NumberFormatException e) {
                    logger.fine("NumberFormatException for id " + map.get("contextForm:contextautocomplete_hinput"));
                } catch (Exception e) {
                    logger.fine("There is no container with id " + Long.valueOf(map.get("contextForm:contextautocomplete_hinput")));
                }
            }
        }
    }

    public void setContainerFilterValue(String containerFilterValue) {
        this.containerFilterValue = containerFilterValue;
    }

    public void setContextContainer(Container contextContainer) {
        // logger.fine(this.toString() + "--- setContextContainer " + contextContainer);
        if (contextContainer != null && !contextContainer.equals(this.contextContainer) && contextContainer.isReadable()) {
            this.contextContainer = contextContainer;
            // Switch the context to the new container if the container is readable for the given user.
            User currentUser = getSessionManager().getCurrentUser();
            if (currentUser != null && !contextContainer.equals(currentUser.getLastContainer())) {
                currentUser.setLastContainer(contextContainer);
                userService.updateLastContainer(currentUser, contextContainer);
            }
        } else if (contextContainer == null) {
            this.contextContainer = null;
        }
    }

    public void setContextContainerById(Long contextContainerId) {
        // logger.fine(this.toString() + "--- setContextContainerById " + contextContainerId);
        setContextContainer(entityService.find(Container.class, contextContainerId));
    }

    public void setKey(String key) {
        this.key = key;
    }
}
