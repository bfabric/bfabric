/*
 *
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Technology;
import org.bfabric.entity.User;
import org.bfabric.entity.UserGroup;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.UserService;
import org.bfabric.util.MessageHelper;
import org.bfabric.util.StringHelper;

@MeasureCalls
@Named
@SessionScoped
public class SessionManager extends AbstractManager {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(SessionManager.class.getName());

    private final List<MessageHelper> facesMessagesList = new ArrayList<>();

    private boolean excludeFeeder;

    private String originalUrl;

    private String redirectURL = null;

    private boolean shibbolethLogin = false;

    private Technology technologyFilter = null;

    private UserGroup userGroupFilter = null;

    @Inject
    private UserService userService;

    public synchronized void addFacesMessage(MessageHelper message) {
        if (message != null) {
            getFacesMessagesList().add(message);
        }
    }

    public void authenticationFailed(User user, String username) {
        userService.increaseInvalidLoginAttempts(user);
        if (getConfiguration().getMaxLoginAttempts() > 0) {
            int attemptsLeft = user.getLoginAttemptsLeft();
            if (attemptsLeft <= 0) {
                logger.warning(Messages.get("loginTooManyInvalidAttempts").replace("{0}", username));
                getFacesMessagesManager().printError(Messages.get("loginTooManyInvalidAttempts").replace("{0}", username));
            } else {
                logger.fine(Messages.get("loginFailedRetryLeft").replace("{0}", String.valueOf(attemptsLeft)) + " for user " + username);
                getFacesMessagesManager().printError(Messages.get("loginFailedRetryLeft").replace("{0}", String.valueOf(attemptsLeft)));
            }
        } else {
            if (user.getInvalidLoginAttempts() > 5) {
                logger.warning("User " + username + " has " + user.getInvalidLoginAttempts() + " subsequent invalid login attempts!");
            }
            getFacesMessagesManager().printError(Messages.get("loginFailedRetry"));
        }
    }

    public void authenticationSuccessful(User user, char[] password, boolean passwordEncrypted) {
        userService.logLogin(user, password, passwordEncrypted);
        getConfManager().getLoggedInUsers().add(user);
        initFilters(user);
        getFacesMessagesManager().bufferWarningClear(Messages.get("loginSuccessful").replace("#0", user.getName()));
        if (user.isEmailOrganizationNotMatching()) {
            getFacesMessagesManager().bufferWarning(Messages.get("emailNotMatchOrganizationHint").replace("{0}", user.getEmail()).replace("{1}", user.getOrganizationName()));
            redirectRelative("/user/edit.xhtml?id=" + user.getId());
        } else {
            redirectRelative(getOriginalUrl() != null ? getOriginalUrl() : getHomeUrl(user));
        }
    }

    public void clearGlobalMessages() {
        getFacesMessagesList().clear();
    }

    public int getDataScrollerChunkSize() {
        return getCurrentUser() != null ? getCurrentUser().getDataScrollerChunkSize() : getConfiguration().getDataScrollerChunkSize();
    }

    public synchronized List<MessageHelper> getFacesMessagesList() {
        return facesMessagesList;
    }

    public int getListingRows() {
        return getCurrentUser() != null ? getCurrentUser().getListingRows() : getConfiguration().getListingRows();
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public Technology getTechnologyFilter() {
        if (technologyFilter != null) {
            try {
                technologyFilter = entityService.find(Technology.class, technologyFilter.getId());
            } catch (Exception e) {
                technologyFilter = null;
            }
        }
        return technologyFilter;
    }

    public String getTechnologyFilterName() {
        String ret = Constants.EMPTY_STRING;
        if (getTechnologyFilter() != null) {
            ret = getTechnologyFilter().getName();
        }
        return ret;
    }

    public UserGroup getUserGroupFilter() {
        if (userGroupFilter != null) {
            try {
                userGroupFilter = entityService.find(UserGroup.class, userGroupFilter.getId());
            } catch (Exception e) {
                userGroupFilter = null;
            }
        }
        return userGroupFilter;
    }

    public String getUserGroupFilterName() {
        String ret = Constants.EMPTY_STRING;
        if (getUserGroupFilter() != null) {
            ret = getUserGroupFilter().getName();
        }
        return ret;
    }

    public void initErrorMessages(String type, String message) {
        Severity severity = FacesMessage.SEVERITY_ERROR;
        String errorMessage = message;

        if (errorMessage == null || StringHelper.isEmpty(errorMessage)) {
            errorMessage = Messages.get(type);

            if (StringHelper.isEmpty(errorMessage) || errorMessage.equals("!!!")) {
                errorMessage = Messages.get("exceptionUnexpectedFailure");
            }
        }

        switch (type) {
        case "org.bfabric.exception.InvalidCodeException":
        case "org.bfabric.exception.InvalidDataException":
        case "java.lang.NullPointerException":
        case "javax.faces.convert.ConverterException":
            // Errors with severity fatal
            severity = FacesMessage.SEVERITY_FATAL;
            break;
        case "javax.persistence.EntityNotFoundException":
        case "javax.persistence.NoResultException":
        case "javax.persistence.EntityExistsException":
        case "javax.persistence.OptimisticLockException":
        case "javax.faces.application.ViewExpiredException":
            // Errors with severity warning
            severity = FacesMessage.SEVERITY_WARN;
            break;
        default:
            // Errors with severity error
            break;
        }

        clearGlobalMessages();
        addFacesMessage(new MessageHelper(severity, errorMessage));
    }

    public void initFilters(User user) {
        if (user != null) {
            setTechnologyFilter(user.getDefaultTechnology());
            setUserGroupFilter(user.getDefaultUserGroup());
        }
    }

    public boolean isExcludeFeeder() {
        return excludeFeeder;
    }

    public boolean isShibbolethLogin() {
        return shibbolethLogin;
    }

    public void printFacesMessages() {
        if (getRedirectURL() != null) {
            String tempRedirectURL = getRedirectURL();
            setRedirectURL(null);
            redirectRelative(tempRedirectURL);
        } else {
            Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
            while (msgIterator.hasNext()) {
                msgIterator.next();
                msgIterator.remove();
            }
            for (MessageHelper message : getFacesMessagesList()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(message.getSeverity(), message.getMessage(), message.getMessage()));
            }
            clearGlobalMessages();
        }
    }

    public void setExcludeFeeder(boolean excludeFeeder) {
        this.excludeFeeder = excludeFeeder;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    public void setShibbolethLogin(boolean shibbolethLogin) {
        this.shibbolethLogin = shibbolethLogin;
    }

    public void setTechnologyFilter(Technology technologyFilter) {
        this.technologyFilter = technologyFilter;
    }

    @SuppressWarnings("EmptyMethod")
    public void setTechnologyFilterName(String technologyFilterName) {
        // required dummy method for default filter option in application tables.
    }

    public void setUserGroupFilter(UserGroup userGroupFilter) {
        this.userGroupFilter = userGroupFilter;
    }

    @SuppressWarnings("EmptyMethod")
    public void setUserGroupFilterName(String userGroupFilterName) {
        // required dummy method for default filter option in application tables.
    }

    public void showDownloadStartedMessage() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, Messages.get("pleaseWaitWhileDownloadBeingPrepared"), null));
    }

    public void technologyFilterChanged(ValueChangeEvent event) {
        setTechnologyFilter((Technology) event.getNewValue());
    }
}
