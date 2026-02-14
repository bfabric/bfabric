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

import java.util.Arrays;
import java.util.logging.Logger;

import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Cookie;

import org.apache.commons.codec.digest.DigestUtils;
import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.User;
import org.bfabric.exception.BfabricValidatorException;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.UserService;
import org.bfabric.util.StringHelper;

@MeasureCalls
@Named
@SessionScoped
public class ShibbolethManager extends AbstractManager {

    private static final Logger logger = Logger.getLogger(ShibbolethManager.class.getName());

    private static final long serialVersionUID = 1;

    @Inject
    protected UserService userService;

    private String gender;

    private String givenName;

    private String mail;

    private char[] password;

    private String surname;

    private String uniqueID;

    private String userName;

    public void clearCookies() {
        logger.fine("Clear Shibboleth cookies ...");
        // Clear cookies iff the login has been performed via Shibboleth.
        if (getSessionManager().isShibbolethLogin() && isRequestInFacesContext()) {
            Cookie[] cookies = getHttpServletRequest().getCookies();
            if (cookies != null) {
                String cookieName;
                for (Cookie cookie : cookies) {
                    cookieName = cookie.getName();
                    if (cookieName.contains("shibsession")) {
                        logger.fine("Clear Shibboleth cookie: " + cookieName);
                        Cookie nameCookie = new Cookie(cookieName, Constants.EMPTY_STRING);
                        nameCookie.setMaxAge(0);
                        nameCookie.setPath("/");
                        getHttpServletResponse().addCookie(nameCookie);
                    } else if (cookieName.equals("JSESSIONID")) {
                        logger.fine("Clear session cookie: " + cookieName);
                        Cookie nameCookie = new Cookie(cookieName, Constants.EMPTY_STRING);
                        nameCookie.setMaxAge(0);
                        nameCookie.setPath("/");
                        getHttpServletResponse().addCookie(nameCookie);
                    }
                }
            }
        }
    }

    public String getGender() {
        return gender;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getMail() {
        return mail;
    }

    public char[] getPassword() {
        return password != null ? password.clone() : null;
    }

    public String getShibbolethEmail() {
        return mail;
    }

    public String getShibbolethFirstName() {
        return givenName;
    }

    public String getShibbolethGender() {
        return gender;
    }

    public boolean getShibbolethHeaders() {
        setUniqueID(StringHelper.iso2utf8trim(getHttpServletRequest().getHeader("uniqueID")));
        if (getUniqueID() != null) {
            setSurname(StringHelper.iso2utf8trim(getHttpServletRequest().getHeader("surname")));
            setGivenName(StringHelper.iso2utf8trim(getHttpServletRequest().getHeader("givenName")));
            setMail(StringHelper.iso2utf8trim(getHttpServletRequest().getHeader("mail")));
            setGender(StringHelper.iso2utf8trim(getHttpServletRequest().getHeader("gender")));

            logger.fine("getShibbolethHeaders: uniqueID = " + getUniqueID() + ", givenName = " + getGivenName() + ", surname = " + getSurname() + ", mail = " + getMail() + ", gender = " + getGender() + ", homeOrganization= " + StringHelper.iso2utf8trim(getHttpServletRequest().getHeader("homeOrganization")) + ", homeOrganizationType= " + StringHelper.iso2utf8trim(getHttpServletRequest().getHeader("homeOrganizationType")) + ", affiliation= " + StringHelper.iso2utf8trim(getHttpServletRequest().getHeader("affiliation")) + ", employeeNumber= " + StringHelper.iso2utf8trim(getHttpServletRequest().getHeader("employeeNumber")) + ", telephoneNumber= " + StringHelper.iso2utf8trim(getHttpServletRequest().getHeader("telephoneNumber")) + ", dateOfBirth= " + StringHelper.iso2utf8trim(getHttpServletRequest().getHeader("dateOfBirth")) + ", homePostalAddress= " + StringHelper.iso2utf8trim(getHttpServletRequest().getHeader("homePostalAddress")) + ", postalAddress= " + StringHelper.iso2utf8trim(getHttpServletRequest().getHeader("postalAddress")) + ", homePhone= " + StringHelper.iso2utf8trim(getHttpServletRequest().getHeader("homePhone")));
            return true;
        }

        return false;
    }

    public String getShibbolethId() {
        return uniqueID;
    }

    public String getShibbolethLastName() {
        return surname;
    }

    public String getSurname() {
        return surname;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public String getUserDetails() {
        StringBuilder details = new StringBuilder();

        if (StringHelper.isNotEmpty(getGivenName())) {
            details.append(getGivenName());
        }
        if (StringHelper.isNotEmpty(getSurname())) {
            if (!details.toString().isEmpty()) {
                details.append(" ");
            }
            details.append(getSurname());
        }
        if (StringHelper.isNotEmpty(getMail())) {
            if (!details.toString().isEmpty()) {
                details.append(" ");
            }
            details.append(getMail());
        }

        return details.toString();
    }

    public String getUserName() {
        return userName;
    }

    public boolean isRequestInFacesContext() {
        return FacesContext.getCurrentInstance() != null;
    }

    public boolean isShibbolethAuthenticated() {
        return getShibbolethHeaders();
    }

    public String login() {
        logger.fine("Trying to log in as Shibboleth user (" + getUniqueID() + ")");

        if (!getShibbolethHeaders()) {
            getFacesMessagesManager().bufferWarningClear(Messages.get("shibbolethLoginException"));
            // Redirect to home page.
            return getUrlHomeScreen();
        }

        getSessionManager().setShibbolethLogin(false);

        User user = userService.getUserByShibbolethId(getUniqueID());
        if (user == null) {
            user = userService.getUserByShibbolethEmail(getMail());
        }
        if (user == null) {
            logger.fine("Shibboleth account " + getUniqueID() + " (" + getMail() + ") could not be matched to a B-Fabric user account.");
            // Redirect to shibboleth/login-failed page.
            return createRedirectURL("shibboleth/login-failed");
        }
        logger.fine("Shibboleth account " + getUniqueID() + " (" + getMail() + ") is matched to the B-Fabric user account " + user.getLogin());

        // Login with the username and password of the retrieved user.
        performShibbolethLogin(user.getLogin(), user.getPassword());

        userService.saveAfterShibbolethLogin(user, getUniqueID(), user.getShibbolethId() == null);

        getSessionManager().setShibbolethLogin(true);
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyLoggedInShibbolethAccount") + " " + getUserDetails());
        return getUrlHomeScreen();
    }

    public String mapToShibbolethAccount() {
        // Login with the username and password which are ensured to be correct by the validators.
        byte[] input = StringHelper.charsToBytes(password);
        performShibbolethLogin(getUserName(), DigestUtils.md5Hex(input));
        // Clear sensitive data.
        Arrays.fill(input, (byte) 0);

        if (getCurrentUser() != null) {
            // Do the mapping after the successful login.
            logger.fine("Mapping B-Fabric account (" + getUserName() + ") to Shibboleth account (" + getUniqueID() + ").");
            User user = userService.getUserByLogin(getUserName());
            if (user != null) {
                userService.saveAfterShibbolethLogin(user, getUniqueID(), true);
            }

            // Clear sensitive data.
            StringHelper.clearCharArray(password);
            getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyMappedLoggedInShibbolethAccount") + " " + getUserDetails());
            return getUrlHomeScreen();
        }

        String errorMessage = "Credentials are not correct for " + getUserName();
        logger.fine(errorMessage);
        getFacesMessagesManager().printError(errorMessage);
        return null;
    }

    @SuppressWarnings("EmptyMethod")
    public void performShibbolethLogin(String login, String aPassword) {
        // Implement this method when this component should be reactivated!
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setPassword(char[] password) {
        this.password = password != null ? password.clone() : null;
        // Clear sensitive data.
        StringHelper.clearCharArray(password);
    }

    public void setShibbolethEmail(String shibbolethEmail) {
        mail = shibbolethEmail;
    }

    public void setShibbolethId(String shibbolethId) {
        uniqueID = shibbolethId;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @SuppressWarnings("SameReturnValue")
    public boolean validateMappedAccountLogin(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        if (value != null) {
            setUserName((String) value);
            User user = userService.getUserByLogin(getUserName());
            if (user == null) {
                throw new BfabricValidatorException("loginNameNotFound");
            }
        }
        return true;
    }

    @SuppressWarnings("SameReturnValue")
    public boolean validateMappedAccountPassword(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        if (value != null) {
            setPassword((char[]) value);

            User user = userService.getUserByLogin(getUserName());
            if (user == null) {
                throw new BfabricValidatorException("loginNameNotFound");
            }
            byte[] input = StringHelper.charsToBytes(password);
            if (!user.getPassword().equalsIgnoreCase(DigestUtils.md5Hex(input))) {
                // Clear sensitive data.
                Arrays.fill(input, (byte) 0);
                throw new BfabricValidatorException("passwordNotMatch");
            }
            // Clear sensitive data.
            Arrays.fill(input, (byte) 0);
        }
        return true;
    }
}
