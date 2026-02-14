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

import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.User;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.UserService;
import org.bfabric.util.StringHelper;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class UserAccountManager extends AbstractEntityManager<User> {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(UserAccountManager.class.getName());

    @Inject
    protected UserService userService;

    @Param
    private String code;

    private String loginOrEmail;

    private String unsubscriptionMessage;

    public UserAccountManager() {
        super(User.class);
    }

    public String activate() {
        if (getInstance() == null || getInstance().getActivationCode() == null || !getInstance().getActivationCode().equals(getCode())) {
            logger.info(Messages.get("activationCodeUserNotValid").replace("{0}", getInstance() != null ? getInstance().getLogin() : Constants.NULL));
            getFacesMessagesManager().bufferErrorClear(Messages.get("emailVerificationFailed"));
        } else {
            getInstance().setEmailVerified(true);
            save(true, false, false);
            getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyVerifiedMail"));
        }
        if (getCurrentUser() != null) {
            return createRedirectShowScreenURL(getInstance());
        }
        return getUrlHomeScreen();
    }

    public String getCode() {
        return code;
    }

    public String getLoginOrEmail() {
        return loginOrEmail;
    }

    public String getUnsubscriptionMessage() {
        return unsubscriptionMessage;
    }

    @Override
    public User loadInstance() {
        return getInstance(getIdLong());
    }

    public String requestPasswordReset() {
        Map<String, Set<String>> facesMessages = userService.requestPasswordReset(getLoginOrEmail());
        printFacesMessagesClear(facesMessages);
        if (!facesMessages.get(Constants.DISPLAY_MESSAGES).isEmpty()) {
            return getUrlHomeScreen();
        }
        getFacesMessagesManager().printErrors(facesMessages.get(Constants.ERROR_MESSAGES));
        return null;
    }

    public String resetPassword() {
        final User user = getInstance();
        boolean valid = true;

        if (StringHelper.isNotEmpty(getCode())) {
            // Check the functional part of the reset password code.
            final String requestedResetCodeFunctional = getCode().substring(0, 32);

            if (user == null) {
                logger.info("User is null");
                valid = false;
            } else {
                if (user.getResetPasswordCodeFunctional() == null || !user.getResetPasswordCodeFunctional().equals(requestedResetCodeFunctional)) {
                    // The requested reset code has encoded a different password or functional part!
                    logger.info("Reset password code " + requestedResetCodeFunctional + " does not match the code " + user.getResetPasswordCodeFunctional() + " of the user " + user.getLogin());
                    valid = false;
                }
            }

            // Check the date part of the reset password code.
            final LocalDate requestedResetCodeDate = StringHelper.decodeBase64Date(getCode().substring(32));
            if (requestedResetCodeDate != null && user != null) {
                final LocalDate currentDate = LocalDate.now();
                if (!currentDate.isBefore(requestedResetCodeDate)) {
                    final long daysBetween = Period.between(requestedResetCodeDate, currentDate).getDays();
                    if (daysBetween != 0) {
                        logger.info("Reset password code " + requestedResetCodeDate + " expired for date " + currentDate + " and user " + user.getLogin());
                        valid = false;
                    }
                } else {
                    logger.info("Reset password code " + requestedResetCodeDate + " invalid for date " + currentDate + " and user " + user.getLogin());
                    valid = false;
                }
            } else {
                logger.info("Reset password code " + getCode().substring(32) + " is corrupt");
                valid = false;
            }
        } else {
            logger.info("Reset password code empty");
            valid = false;
        }

        if (valid && user != null) {
            getSessionManager().setOriginalUrl(createRedirectURL("user/change-password", user.getId(), null, null));
            identityManager.authenticate(user);
        } else {
            getFacesMessagesManager().bufferErrorClear(Messages.get("passwordResetFailed"));
        }

        return getUrlHomeScreen();
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setLoginOrEmail(String loginOrEmail) {
        this.loginOrEmail = loginOrEmail;
    }

    public void setUnsubscriptionMessage(String unsubscriptionMessage) {
        this.unsubscriptionMessage = unsubscriptionMessage;
    }

    public void unsubscribe() {
        final String expectedCode = getInstance().getUnsubscriptionCode();
        if (!expectedCode.equals(code)) {
            setUnsubscriptionMessage(Messages.get("unsubscriptionCodeInvalid"));
            getFacesMessagesManager().printError(getUnsubscriptionMessage());
            logger.warning(getUnsubscriptionMessage() + " login: " + getInstance().getLogin() + ", expected: " + expectedCode + ", received: " + code + ".");
        } else {
            userService.disableMassMailForUser(getInstance().getId());
            setUnsubscriptionMessage(Messages.get("unsubscribeConfirmation").replace("{0}", getInstance().getLogin()));
            getFacesMessagesManager().printWarn(Messages.get("unsubscriptionConfirmed"));
        }
    }
}