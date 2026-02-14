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

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.credential.Password;
import javax.security.enterprise.credential.UsernamePasswordCredential;

import org.bfabric.Messages;
import org.bfabric.entity.User;
import org.bfabric.enums.RoleEnum;
import org.bfabric.interceptors.CachedMethodResult;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.IdentityService;
import org.bfabric.service.UserService;
import org.bfabric.util.StringHelper;

@MeasureCalls
@Named
@ViewScoped
public class IdentityManager extends AbstractHttpServletManager {

    private static final Logger logger = Logger.getLogger(IdentityManager.class.getName());

    private static final long serialVersionUID = 1;

    private User currentUser;

    private List<String> currentUserRoleNames;

    @Inject
    private FacesMessagesManager facesMessagesManager;

    @Inject
    private IdentityService identityService;

    private char[] password;

    @Inject
    private UserService userService;

    private String username;

    public void authenticate() {
        authenticate(false);
    }

    public void authenticate(boolean passwordEncrypted) {
        if (StringHelper.isEmpty(getUsername()) || getPassword() == null || getPassword().length == 0) {
            facesMessagesManager.printError(Messages.get("loginNamePasswordRequired"));
        } else {
            User user = userService.getUserByLoginOrEmail(getUsername());
            if (user == null) {
                facesMessagesManager.printError(Messages.get("userLoginNotExist").replace("{0}", getUsername()));
                return;
            }
            if (!user.isAccountEnabled()) {
                facesMessagesManager.printError(Messages.get("userLoginDisabled").replace("{0}", getUsername()));
                return;
            }
            if (user.hasNoLoginAttemptsLeft()) {
                logger.warning(Messages.get("loginTooManyInvalidAttempts").replace("{0}", getUsername()));
                facesMessagesManager.printError(Messages.get("loginTooManyInvalidAttempts").replace("{0}", getUsername()));
                return;
            }
            // Create credentials: Note that encoding is required when the password comes from the GUI.
            // Important: use below user.getLogin() instead of getUsername() since the latter can be either the login or email!
            UsernamePasswordCredential credentials = new UsernamePasswordCredential(user.getLogin(), new Password(getPassword()));
            AuthenticationStatus status = identityService.authenticate(getHttpServletRequest(), getHttpServletResponse(), credentials);
            if (status.equals(AuthenticationStatus.SUCCESS)) {
                getSessionManager().authenticationSuccessful(user, getPassword(), passwordEncrypted);
            } else if (status.equals(AuthenticationStatus.SEND_FAILURE)) {
                getSessionManager().authenticationFailed(user, getUsername());
            }
        }
    }

    public void authenticate(User user) {
        setUsername(user.getLogin());
        setPassword(user.getPassword().toCharArray());
        authenticate(true);
    }

    @CachedMethodResult
    public User getCurrentUser() {
        if (currentUser == null) {
            currentUser = identityService.getCurrentUser();
        }
        return currentUser;
    }

    public List<String> getCurrentUserRoleNames() {
        if (currentUserRoleNames == null) {
            currentUserRoleNames = identityService.getCurrentUserRoleNames();
        }
        return currentUserRoleNames;
    }

    public FacesMessagesManager getFacesMessagesManager() {
        return facesMessagesManager;
    }

    public LocalDateTime getNow() {
        return LocalDateTime.now();
    }

    public char[] getPassword() {
        return password != null ? password.clone() : null;
    }

    public SessionManager getSessionManager() {
        return getFacesMessagesManager().getSessionManager();
    }

    public String getUsername() {
        return username;
    }

    @CachedMethodResult
    public boolean hasCurrentUserRole(String roleName) {
        // logger.fine("--- hasCurrentUserRole " + roleName);
        return getCurrentUserRoleNames() != null && getCurrentUserRoleNames().contains(roleName);
    }

    @CachedMethodResult
    public boolean hasCurrentUserRoleEnum(RoleEnum roleEnum) {
        // logger.fine("--- hasCurrentUserRoleEnum " + roleEnum);
        return roleEnum != null && hasCurrentUserRole(roleEnum.getName());
    }

    @Produces
    @Named("loggedIn")
    @CachedMethodResult
    public boolean isLoggedIn() {
        return identityService.isLoggedIn();
    }

    @CachedMethodResult
    public void redirectOnCondition(boolean condition, String page) {
        if (condition) {
            getSessionManager().redirectRelative(page);
        }
    }

    @CachedMethodResult
    public void restrict(boolean condition) {
        redirectOnCondition(!condition, "/error/permission-denied.xhtml");
    }

    @CachedMethodResult
    public void restrictRole(String role) {
        restrict(hasCurrentUserRole(role));
    }

    public void setPassword(char[] password) {
        this.password = password != null ? password.clone() : null;
    }

    public void setUsername(String username) {
        this.username = username != null ? username.toLowerCase() : null;
    }
}
