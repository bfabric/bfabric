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

package org.bfabric.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Named;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.SecurityContext;
import static javax.security.enterprise.authentication.mechanism.http.AuthenticationParameters.withParams;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bfabric.Constants;
import org.bfabric.entity.EntityLog;
import org.bfabric.entity.User;
import org.bfabric.enums.LogActionEnum;
import org.bfabric.enums.LogStatusEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.StringHelper;

@Named
@Stateless
public class IdentityService extends AbstractService {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(IdentityService.class.getName());

    private transient SecurityContext securityContext;

    private UserService userService;

    public AuthenticationStatus authenticate(HttpServletRequest request, HttpServletResponse response, UsernamePasswordCredential credentials) {
        if (getSecurityContext() == null) {
            return AuthenticationStatus.SEND_FAILURE;
        }
        final AuthenticationStatus status = getSecurityContext().authenticate(request, response, withParams().credential(credentials));
        if (status.equals(AuthenticationStatus.SUCCESS) && getCurrentUser() != null) {
            logger.fine("Successfully authenticated user " + getCurrentUser().getLogin());
            request.getSession().setAttribute(Constants.LOGIN, getCurrentUser().getLogin());
        }
        return status;
    }

    public AuthenticationStatus authenticateWS(HttpServletRequest request, HttpServletResponse response, String username, String passwordWS) {
        if (StringHelper.isNotEmpty(username) && StringHelper.isNotEmpty(passwordWS)) {
            final User user = getUserByLogin(username);
            if (user != null && passwordWS.equals(user.getPasswordWS())) {
                return authenticate(request, response, new UsernamePasswordCredential(username, user.getPassword()));
            }
        }
        return AuthenticationStatus.SEND_FAILURE;
    }

    public User getCurrentUser() {
        return getUserByLogin(getCurrentUsername());
    }

    public List<String> getCurrentUserRoleNames() {
        // logger.fine("************** IdentityService.getCurrentUserRoleNames ");
        return getRoleNamesByUsername(getCurrentUsername());
    }

    public String getCurrentUsername() {
        return getSecurityContext() != null && getSecurityContext().getCallerPrincipal() != null ? getSecurityContext().getCallerPrincipal().getName() : null;
    }

    public EntityLog getLastReleaseEntityLog() {
        List<?> result = createQuery("select  a from EntityLog a where a.action = org.bfabric.enums.LogActionEnum.SYSTEM_RELEASE order by id desc").setMaxResults(1).getResultList();
        return result.isEmpty() ? null : (EntityLog) result.get(0);
    }

    public List<String> getRoleNamesByUsername(String username) {
        // logger.fine("************** IdentityService.getRoleNamesByUsername ");
        return username != null ? createNativeQuery("SELECT name FROM impliedrolenames(:login) order by name").setParameter("login", username).getResultList() : null;
    }

    public SecurityContext getSecurityContext() {
        if (securityContext == null) {
            securityContext = CDI.current().select(SecurityContext.class).get();
        }
        return securityContext;
    }

    public User getUserByLogin(String login) {
        return login != null && getUserService() != null ? getUserService().getUserByLogin(login) : null;
    }

    public UserService getUserService() {
        if (userService == null) {
            userService = CDI.current().select(UserService.class).get();
        }
        return userService;
    }

    public boolean hasRole(String roleName) {
        // logger.fine("*** IdentityService.hasRole " + roleName);
        boolean ret = false;
        if (StringHelper.isNotEmpty(getCurrentUsername()) && StringHelper.isNotEmpty(roleName)) {
            ret = !createNativeQuery("SELECT name FROM impliedrolenames(:login) WHERE name = :name").setParameter("name", roleName).setParameter("login", getCurrentUsername()).setMaxResults(1)
                .getResultList().isEmpty();
        }
        return ret;
    }

    public boolean hasRoleEnum(RoleEnum roleEnum) {
        return hasRole(roleEnum.getName());
    }

    public boolean isLoggedIn() {
        return getSecurityContext() != null && getSecurityContext().getCallerPrincipal() != null;
    }

    public void logLogout(User user, EntityLog entityLog) {
        if (user != null) {
            if (entityLog != null) {
                persist(entityLog);
            }
            getConfManager().getLoggedInUsers().remove(user);
            logger.fine("Successfully logged out " + user.getLogin());
        }
    }

    public void logLogoutViaTimeout(String login) {
        User user = getUserByLogin(login);
        if (user != null) {
            logLogout(user, new EntityLog(user, LogActionEnum.LOGOUT_TIMEOUT));
        }
    }

    public void logout(HttpServletRequest request, boolean createEntityLog) {
        final User user = getCurrentUser();
        if (user != null) {
            logLogout(user, createEntityLog ? new EntityLog(user, LogActionEnum.LOGOUT, LogStatusEnum.DONE, user.getLogin()) : null);
            try {
                if (request != null) {
                    request.getSession().setAttribute(Constants.LOGOUT, user.getLogin());
                    request.logout();
                    request.getSession(false).invalidate();
                }
            } catch (final Exception e) {
                logger.warning("Logout failed: " + e.getMessage());
            }
        }
    }

    public void updateUserLastActionTime() {
        createQuery("update User set lastActionTime = :lastActionTime where login = :login").setParameter("lastActionTime", LocalDateTime.now())
            .setParameter("login", getCurrentUsername()).executeUpdate();
    }
}
