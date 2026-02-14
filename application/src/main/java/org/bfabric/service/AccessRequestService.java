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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.AccessRequest;
import org.bfabric.entity.Configuration;
import org.bfabric.entity.Mail;
import org.bfabric.enums.AccessRequestStatusEnum;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.service.util.BfabricLazyDataModel;
import org.bfabric.service.util.EntityQuery;

@Named
@Stateless
public class AccessRequestService extends AbstractMailSendingService {

    private static final long serialVersionUID = 1;

    public AccessRequestService() {
        super(AccessRequest.class);
    }

    public Map<String, Set<String>> approveAccessRequest(AccessRequest accessRequest, Configuration configuration) {
        Map<String, Set<String>> facesMessages = createFacesMessagesMap();

        if (accessRequest != null && accessRequest.isPending()) {
            // If access card was changed store this change.
            merge(accessRequest.getUser());
            accessRequest.setBirthDate(accessRequest.getUser().getBirthDate());
            accessRequest.setAccessCardNumber(accessRequest.getUser().getAccessCardNumber());
            accessRequest.setAccessCardCode(accessRequest.getUser().getAccessCardCode());
            accessRequest.setAccessCardExpiryDate(accessRequest.getUser().getAccessCardExpiryDate());

            String ret = null;
            String accessRequestTypeName = accessRequest.getAccessRequestType() != null ? accessRequest.getAccessRequestType().getName() : null;
            if (accessRequestTypeName != null) {
                switch (accessRequestTypeName) {
                case "Guest Card Application":
                    accessRequest.getUser().setAccessCardExpiryDate(accessRequest.getAccessCardValidityEndDate());
                    // Send mail to the user for whom the access was approved.
                    ret = sendMail(MailTypeEnum.USER_REQUEST_ACCESS_SENT, accessRequest, configuration);
                    if (ret == null) {
                        // Access manager approves this request and updates access request state.
                        decideAccessRequest(accessRequest, Boolean.TRUE);
                        // Send mail to the user for whom the access was approved.
                        facesMessages.get(Constants.ERROR_MESSAGES).add(sendMail(MailTypeEnum.USER_REQUEST_ACCESS_APPROVED, accessRequest, configuration));
                    } else {
                        facesMessages.get(Constants.ERROR_MESSAGES).add(Messages.get("accessErrorUzhSuDepartment"));
                    }
                    break;
                case "Personal Card Access":
                    accessRequest.getUser().setAccessCardExpiryDate(null);
                    accessRequest.setAccessCardExpiryDate(null);
                    // Send mail to the user for whom the access was approved.
                    ret = sendMail(MailTypeEnum.USER_REQUEST_ACCESS_SENT, accessRequest, configuration);
                    if (ret == null) {
                        // Access manager approves this request and updates access request state.
                        decideAccessRequest(accessRequest, Boolean.TRUE);
                        // Send mail to the user for whom the access was approved.
                        facesMessages.get(Constants.ERROR_MESSAGES).add(sendMail(MailTypeEnum.USER_REQUEST_ACCESS_APPROVED, accessRequest, configuration));
                    } else {
                        facesMessages.get(Constants.ERROR_MESSAGES).add(Messages.get("accessErrorUzhSuDepartment"));
                    }
                    break;
                case "Guest Card Extension":
                    // Access manager approves this request and updates extension request state.
                    decideAccessRequest(accessRequest, Boolean.TRUE);
                    // Send mail to the user for whom the extension was approved.
                    facesMessages.get(Constants.ERROR_MESSAGES).add(sendMail(MailTypeEnum.USER_REQUEST_EXTENSION_APPROVED, accessRequest, configuration));
                    break;
                default:
                    ret = "Unknown access request type!";
                    facesMessages.get(Constants.ERROR_MESSAGES).add(ret);
                    break;
                }
                if (ret == null) {
                    // If ret is null, there are no errors except the ones from sending the mail.
                    facesMessages.get(Constants.DISPLAY_MESSAGES)
                        .add(accessRequest.getAccessRequestType().getName()
                            .equals("Guest Card Extension") ? Messages.get("accessRequestApprovedMessage") : Messages.get("accessApprovedUzhSuDepartment"));
                }
            } else {
                ret = "Access request type is null!";
                facesMessages.get(Constants.ERROR_MESSAGES).add(ret);
            }
        } else {
            facesMessages.get(Constants.ERROR_MESSAGES)
                .add(Messages.get("errorMessageGeneric").replace("{0}", " approving the access request").replace("{1}", "Access request or approved were null"));
        }

        return facesMessages;
    }

    public void decideAccessRequest(AccessRequest accessRequest, boolean decision) {
        accessRequest.setDecision(decision);
        accessRequest.setAffiliation(accessRequest.getUser().getFullContactDetails());
        save(accessRequest);
    }

    public BfabricLazyDataModel<AccessRequest> getProcessAccessRequestTasksLazyModel() {
        EntityQuery entityQuery = createEntityQuery();
        entityQuery.setWhere("status = :status");
        entityQuery.addParameter("status", AccessRequestStatusEnum.PENDING);
        entityQuery.setOrder("id");
        return new BfabricLazyDataModel<>(entityQuery);
    }

    public Map<String, Set<String>> rejectAccessRequest(AccessRequest accessRequest, Configuration configuration) {
        Map<String, Set<String>> facesMessages = createFacesMessagesMap();

        if (accessRequest != null && accessRequest.isPending()) {
            // Access manager approves this request and updates access request state.
            decideAccessRequest(accessRequest, Boolean.FALSE);

            switch (accessRequest.getAccessRequestType().getName()) {
            case "Personal Card Access":
            case "Guest Card Application":
                // Send mail to the user for whom the access was requested.
                facesMessages.get(Constants.ERROR_MESSAGES).add(sendMail(MailTypeEnum.USER_REQUEST_ACCESS_REJECTED, accessRequest, configuration));
                break;
            case "Guest Card Extension":
                // Send mail to the user for whom the extension was requested.
                facesMessages.get(Constants.ERROR_MESSAGES).add(sendMail(MailTypeEnum.USER_REQUEST_EXTENSION_REJECTED, accessRequest, configuration));
                break;
            default:
                facesMessages.get(Constants.ERROR_MESSAGES).add("Unknown access request type!");
                break;
            }

            facesMessages.get(Constants.DISPLAY_MESSAGES).add(Messages.get("accessRequestRejectedMessage"));
        } else {
            facesMessages.get(Constants.ERROR_MESSAGES)
                .add(Messages.get("errorMessageGeneric").replace("{0}", " rejecting the access request").replace("{1}", "Access request or approved were null"));
        }

        return facesMessages;
    }

    private String sendMail(MailTypeEnum mailTypeEnum, AccessRequest accessRequest, Configuration configuration) {
        Mail mail = new Mail();
        mail.setParent(accessRequest.getUser());
        mail.setType(mailTypeEnum);

        switch (mailTypeEnum) {
        case USER_REQUEST_ACCESS_APPROVED:
        case USER_REQUEST_ACCESS_REJECTED:
        case USER_REQUEST_EXTENSION_APPROVED:
        case USER_REQUEST_EXTENSION_REJECTED:
            mail.setRecipient(accessRequest.getUser());
            break;
        case USER_REQUEST_ACCESS_SENT:
            try {
                mail.getMailHelper().setFrom(new InternetAddress(configuration.getAccessRequestManagerEmail()));
                mail.getMailHelper().setTo(new ArrayList<>(Collections.singletonList(new InternetAddress(configuration.getAccessRequestUZHEmail()))));
                mail.getMailHelper().setBcc(new ArrayList<>(Collections.singletonList(new InternetAddress(configuration.getAccessRequestNotificationEmail()))));
            } catch (AddressException e) {
                e.printStackTrace();
            }
            break;
        default:
            break;
        }
        mail.setInput("accessRequest", accessRequest);
        mail.setInput("accessRequestManagerDate", LocalDate.now());
        return mailSendService.send(mail);
    }
}