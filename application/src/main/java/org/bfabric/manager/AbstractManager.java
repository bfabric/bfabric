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

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AbstractEntity;
import org.bfabric.entity.Container;
import org.bfabric.entity.User;
import org.bfabric.enums.ConverterTypeEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.service.EntityService;
import org.bfabric.util.SidebarHelper;
import org.primefaces.PrimeFaces;

/**
 * The AbstractManager provides generic backing bean functionality such as handling of faces messages. It relies on the genericEJB to read and write database entities using bean-managed transactions.
 */
public abstract class AbstractManager extends AbstractHttpServletManager {

    private static final Logger logger = Logger.getLogger(AbstractManager.class.getName());

    private static final long serialVersionUID = 1;

    @Inject
    protected ContextManager contextManager;

    @Inject
    protected EntityService entityService;

    @Inject
    protected IdentityManager identityManager;

    @Inject
    protected SidebarHelper sidebarHelper;

    @Resource
    protected transient UserTransaction userTransaction;

    private Container contextContainer;

    private int customListingRows = 0;

    private String oldRefererURL;

    private String refererURL;

    public void addValidationErrorMessage(String tableId, String componentId, String message, int rowIndex) {
        if (tableId != null && componentId != null && message != null && rowIndex >= 0) {
            getFacesMessagesManager().validationError(tableId + ":" + rowIndex + ":" + componentId, message);
        }
    }

    public String createRedirectShowScreenURL(AbstractEntity entity, String tab, Map<String, String> fParams) {
        return entity != null ? createRedirectURL(null, true, entity.getShowScreenPathPrefix(), entity.getId(), tab, fParams) : null;
    }

    public String createRedirectShowScreenURL(String className, Long redirectId, String tab, Map<String, String> fParams) {
        return className != null ? createRedirectURL(null, true, className.toLowerCase() + "/" + Constants.SHOW, redirectId, tab, fParams) : null;
    }

    public String createRedirectShowScreenURL(AbstractEntity entity) {
        return createRedirectShowScreenURL(entity, null, null);
    }

    public String createRedirectURL(String root, boolean facesRedirectTrue, String screen, Long redirectId, String tab, Map<String, String> fParams) {
        StringBuilder redirectURL = new StringBuilder((root != null ? root : Constants.EMPTY_STRING) + "/" + screen + ".html" + (facesRedirectTrue ? "?faces-redirect=true" : Constants.EMPTY_STRING));
        boolean isFirstArgument = !facesRedirectTrue;
        if (redirectId != null && redirectId > 0) {
            redirectURL.append(isFirstArgument ? "?" : "&").append("id=").append(redirectId);
            if (isFirstArgument) {
                isFirstArgument = false;
            }
        }
        if (tab != null) {
            redirectURL.append(isFirstArgument ? "?" : "&").append("tab=").append(tab);
            if (isFirstArgument) {
                isFirstArgument = false;
            }
        }
        if (fParams != null) {
            for (Entry<String, String> entry : fParams.entrySet()) {
                redirectURL.append(isFirstArgument ? "?" : "&").append(entry.getKey()).append("=").append(entry.getValue());
                if (isFirstArgument) {
                    isFirstArgument = false;
                }
            }
        }
        return redirectURL.toString();
    }

    public String createRedirectURL(String screen, Long redirectId, String tab, Map<String, String> fParams) {
        return createRedirectURL(null, true, screen, redirectId, tab, fParams);
    }

    public String createRedirectURL(String screen) {
        return createRedirectURL(null, true, screen, null, null, null);
    }

    public String createRedirectURLFromRefererURL() {
        StringBuilder redirectURL = new StringBuilder(getRefererURL());
        redirectURL.delete(0, redirectURL.toString().indexOf(getContextPath()) + getContextPath().length());
        redirectURL.append(redirectURL.indexOf("?") < 0 ? "?" : "&");
        redirectURL.append("faces-redirect=true");
        return redirectURL.toString();
    }

    public void facesMessageAdd(boolean isCreated) {
        if (isCreated) {
            getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyCreated"));
        } else {
            getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyUpdated"));
        }
    }

    public Container getContextContainer() {
        // logger.fine("--- abstractManager.getContextContainer");
        if (contextContainer == null && getContextManager() != null) {
            contextContainer = getContextManager().getContextContainer();
        }
        return contextContainer;
    }

    public ContextManager getContextManager() {
        return contextManager;
    }

    @CachedMethodResult
    public Converter<?> getConverter(ConverterTypeEnum converterTypeEnum) {
        return ConverterTypeEnum.getConverter(converterTypeEnum);
    }

    @CachedMethodResult
    @Produces
    @Named("currentUser")
    public User getCurrentUser() {
        return getIdentityManager().getCurrentUser();
    }

    public int getCustomListingRows() {
        if (customListingRows == 0) {
            customListingRows = getCurrentUser() != null ? getCurrentUser().getListingRows() : getConfiguration().getListingRows();
        }
        return customListingRows;
    }

    public String getDefaultRedirectURL() {
        return getCurrentUser() != null && getContextContainer() != null ? createRedirectShowScreenURL(getContextContainer()) : getUrlHomeScreen();
    }

    public FacesMessagesManager getFacesMessagesManager() {
        return identityManager.getFacesMessagesManager();
    }

    public String getHomeUrl(User user) {
        if (user != null && user.isEmailActive()) {
            if (user.getLastContainer() != null) {
                return user.getLastContainer().getUrlShowScreen() + "?id=" + user.getLastContainer().getId();
            }
            return "/home-active.xhtml";
        }
        return "/home-inactive.xhtml";
    }

    public Long getIdLong(String idString) {
        try {
            long idLong = Long.parseLong(idString);
            if (idLong > 0) {
                return idLong;
            }
        } catch (Exception e) {
            logger.fine("Entity id " + idString + " is invalid!");
        }
        return null;
    }

    public IdentityManager getIdentityManager() {
        return identityManager;
    }

    public String getOldRefererURL() {
        return oldRefererURL;
    }

    public String getRefererURL() {
        return refererURL;
    }

    public SessionManager getSessionManager() {
        return getFacesMessagesManager().getSessionManager();
    }

    public String getUrlHomeScreen() {
        return createRedirectURL("landing");
    }

    @PostConstruct
    public void init() {
        // logger.fine("Initialize entity manager: " + getClass().getSimpleName());
    }

    public void printFacesMessages(Map<String, Set<String>> facesMessages) {
        printFacesMessages(facesMessages, true);
    }

    public void printFacesMessages(Map<String, Set<String>> facesMessages, boolean buffer) {
        if (facesMessages != null) {
            if (facesMessages.containsKey(Constants.ERROR_MESSAGES)) {
                if (buffer) {
                    getFacesMessagesManager().bufferErrors(facesMessages.get(Constants.ERROR_MESSAGES));
                } else {
                    getFacesMessagesManager().printErrors(facesMessages.get(Constants.ERROR_MESSAGES));
                }
            }
            if (facesMessages.containsKey(Constants.DISPLAY_MESSAGES)) {
                if (buffer) {
                    getFacesMessagesManager().bufferWarnings(facesMessages.get(Constants.DISPLAY_MESSAGES));
                } else {
                    getFacesMessagesManager().printWarnings(facesMessages.get(Constants.DISPLAY_MESSAGES));
                }
            }
        }
    }

    public void printFacesMessagesClear(Map<String, Set<String>> facesMessages, boolean buffer) {
        getFacesMessagesManager().clearGlobalMessages();
        printFacesMessages(facesMessages, buffer);
    }

    public void printFacesMessagesClear(Map<String, Set<String>> facesMessages) {
        getFacesMessagesManager().clearGlobalMessages();
        printFacesMessages(facesMessages, true);
    }

    public void redirect(String targetPage) {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect(targetPage.replace(".xhtml", ".html"));
            FacesContext.getCurrentInstance().responseComplete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void redirectInNewTab(String targetPage) {
        PrimeFaces.current().executeScript("window.open('" + targetPage + "', '_blank');");
    }

    public void redirectRelative(String targetPage) {
        redirect(getContextPath() + targetPage);
    }

    public void redirectToContainer(Container container) {
        redirectRelative("/" + container.getEntityName().toLowerCase() + "/show.html?id=" + container.getId() + Constants.TAB_DETAILS);
    }

    public void redirectToContainer(long id) {
        redirectToContainer(entityService.find(Container.class, id));
    }

    public void redirectToEntityIdInvalidErrorPage(String className, String id) {
        // logger.fine("Entity id invalid: " + entityClassName + " " + getId());
        getFacesMessagesManager().bufferWarning(className + " " + id + " is invalid");
        getSessionManager().redirectRelative("/error/entity-id-invalid.html");
    }

    public void redirectToEntityNotFoundErrorPage(String className, String id) {
        // logger.fine("Entity not found: " + entityClassName + " " + getId());
        getFacesMessagesManager().bufferWarning(className + " " + id + " not found");
        getSessionManager().redirectRelative("/error/entity-not-found.html");
    }

    public void resetRefererURL() {
        if (!FacesContext.getCurrentInstance().isValidationFailed()) {
            setRefererURL(getHttpServletRequest().getHeader("referer"));
        }
        if (FacesContext.getCurrentInstance().getExternalContext().getFlash().containsKey("oldRefererURL")) {
            setRefererURL((String) FacesContext.getCurrentInstance().getExternalContext().getFlash().get("oldRefererURL"));
        }
        setOldRefererURL(getRefererURL());
    }

    public void rollback() throws SystemException {
        try {
            if (userTransaction.getStatus() != Status.STATUS_ACTIVE) {
                userTransaction.begin();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        userTransaction.rollback();
    }

    public void setCustomListingRows(int customListingRows) {
        this.customListingRows = customListingRows;
    }

    public void setOldRefererURL(String oldRefererURL) {
        this.oldRefererURL = oldRefererURL;
    }

    public void setRefererURL(String refererURL) {
        this.refererURL = refererURL;
    }

    public boolean transactionBegin() {
        boolean transactionWasActive = false;
        try {
            if (userTransaction != null) {
                transactionWasActive = userTransaction.getStatus() == Status.STATUS_ACTIVE;
            }
            if (userTransaction != null && !transactionWasActive) {
                userTransaction.begin();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transactionWasActive;
    }
}
