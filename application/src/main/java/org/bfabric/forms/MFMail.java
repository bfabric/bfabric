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

package org.bfabric.forms;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;

import org.bfabric.entity.Mail;
import org.bfabric.entity.User;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.service.UserService;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveMail;

public class MFMail extends AbstractMF {

    private final Mail mail;

    private final XMLRequestParameterSaveMail xmlRequestSaveMail;

    private boolean force;

    private UserService userService;

    public MFMail(Mail mail, XMLRequestParameterSaveMail xmlRequestSaveMail) {
        this.mail = mail;
        this.xmlRequestSaveMail = xmlRequestSaveMail;
    }

    @Override
    public synchronized void apply() throws Exception {
        checkPermission();
        setForce(getForce());
        getMail().setType(MailTypeEnum.CUSTOM);
        getMail().setParent(getIdentityService().getCurrentUser());
        getMail().setSubject(getSubject());
        getMail().setMessage(getMessage());
        getMail().addRecipients(getRecipients());
    }

    public void checkPermission() throws Exception {
        User currentUser = getIdentityService().getCurrentUser();
        if (!currentUser.hasRoleImplicit(RoleEnum.MAILSENDER)) {
            throw new InvalidDataException("The user " + currentUser.getLogin() + " has no right to send email via WS!");
        }
    }

    public Boolean getForce() throws Exception {
        return MFHelper.booleanValueOf("force", getXmlRequestSaveMail().getForce());
    }

    public Mail getMail() {
        return mail;
    }

    public String getMessage() {
        return getXmlRequestSaveMail().getMessage();
    }

    public Set<User> getRecipients() throws Exception {
        if (!getXmlRequestSaveMail().getRecipientemail().isEmpty()) {
            Set<User> recipients = new HashSet<>();
            for (String recipientemail : getXmlRequestSaveMail().getRecipientemail()) {
                User user = getUserService().getUserByEmail(recipientemail);
                if (user == null) {
                    throw new InvalidDataException("No user found for recipient mail " + recipientemail + "!");
                }
                if (isForce() || user.getMassMailEnabled()) {
                    recipients.add(user);
                }
            }
            if (recipients.isEmpty()) {
                throw new InvalidDataException("Recipient emails do not contain any valid user!");
            }
            return recipients;
        }
        return null;
    }

    public String getSubject() {
        return getXmlRequestSaveMail().getSubject();
    }

    public UserService getUserService() {
        if (userService == null) {
            userService = CDI.current().select(UserService.class).get();
        }
        return userService;
    }

    public XMLRequestParameterSaveMail getXmlRequestSaveMail() {
        return xmlRequestSaveMail;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }
}