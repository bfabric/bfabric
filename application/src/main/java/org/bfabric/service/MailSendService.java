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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Configuration;
import org.bfabric.entity.EntityLog;
import org.bfabric.entity.Mail;
import org.bfabric.entity.User;
import org.bfabric.enums.LogActionEnum;
import org.bfabric.enums.LogStatusEnum;
import org.bfabric.enums.MailTypeEnum;

@Named
@Stateless
public class MailSendService extends AbstractService {

    private static final Logger logger = Logger.getLogger(MailSendService.class.getName());

    private static final long serialVersionUID = 1;

    @Inject
    private IdentityService identityService;

    @Resource(lookup = "mail/bfabric")
    private transient Session mailSession;

    public MailSendService() {
    }

    public String send(Mail mail) {
        return send(mail, true);
    }

    public String send(Mail mail, boolean logMessage) {
        if (mail != null && (!mail.getRecipients().isEmpty() || mail.getMailHelper() != null && mail.getMailHelper().isRecipientsNotEmpty())) {
            try {
                Configuration configuration = getConfiguration();
                if (configuration != null) {
                    if (configuration.isMailEnabled()) {
                        mail.getMailHelper().setConfiguration(configuration);
                        logger.fine("Trying to send " + mail.getType().getMailTemplateFileName());

                        // Remove inactive users from recipients.
                        List<User> recipients = new ArrayList<>();
                        if (!mail.getRecipients().isEmpty()) {
                            recipients.addAll(mail.getRecipients());
                        }
                        for (User recipient : recipients) {
                            if (!recipient.isEmailActive() && !mail.getType().equals(MailTypeEnum.USER_PASSWORD_LOST)) {
                                mail.getRecipients().remove(recipient);
                            }
                        }

                        // Create email addresses from recipients.
                        List<InternetAddress> recipientAddresses = new ArrayList<>();
                        for (User recipient : new HashSet<>(mail.getRecipients())) {
                            recipientAddresses.add(new InternetAddress(recipient.getEmail(), recipient.getLastNameFirstName()));
                        }

                        // Send user password lost mail also to the private email.
                        if (mail.getType().equals(MailTypeEnum.USER_PASSWORD_LOST) && mail.getRecipient() != null && mail.getRecipient().getPrivateEmail() != null) {
                            recipientAddresses.add(new InternetAddress(mail.getRecipient().getPrivateEmail(), mail.getRecipient().getLastNameFirstName()));
                        }

                        // Set mail to/cc/bbc depending on the mail type.
                        if (mail.getType().getMailRecipientTypeEnum() == null) {
                            if (recipientAddresses.size() == 1 || mail.getType().equals(MailTypeEnum.USER_PASSWORD_LOST)) {
                                mail.getMailHelper().setTo(recipientAddresses);
                            } else if (recipientAddresses.size() > 1) {
                                mail.getMailHelper().setBcc(recipientAddresses);
                            }
                        } else {
                            switch (mail.getType().getMailRecipientTypeEnum()) {
                            case TO:
                                mail.getMailHelper().setTo(recipientAddresses);
                                break;
                            case CC:
                                mail.getMailHelper().setCc(recipientAddresses);
                                break;
                            default:
                                mail.getMailHelper().setBcc(recipientAddresses);
                                break;
                            }
                        }

                        if (!mail.getMailHelper().getTo().isEmpty() || !mail.getMailHelper().getCc().isEmpty() || !mail.getMailHelper().getBcc().isEmpty()) {
                            mail.generateRecipientAddressList();
                            mail.setSubjectPrefix(configuration.getMailSubjectPrefix());

                            if (!mail.isCurrentUserSet()) {
                                mail.setCurrentUser(identityService.getCurrentUser());
                            }

                            // Create and set mail HTML message.
                            mail.setMessage(getConfManager().getMailTemplateEngine().buildMailHtmlMessage(mail));

                            // Actually send mail.
                            sendMimeMessage(mail);

                            if (logMessage) {
                                persist(mail);
                            }
                            logger.fine("Successfully sent mail=" + mail.getEntitySpecifics());
                        }
                    } else {
                        return Messages.get("mailDisabled").replace("{0}", mail.getType().getMailTemplateFileName());
                    }
                }
            } catch (Exception e) {
                logger.severe(e.getMessage());
                if (logMessage) {
                    String log = "<log><old><type></type><recipients></recipients><error></error></old><new><type>" + mail.getType() + "</type><recipients>" + mail.getRecipientsAddressList()
                        + "</recipients><error>" + e.getMessage() + "</error></new></log>";
                    EntityLog entityLog = new EntityLog(mail.getParent(), LogActionEnum.SEND_MAIL, LogStatusEnum.FAILED, Constants.SYSTEM, log);
                    persist(entityLog);
                }
                if (e instanceof MessagingException) {
                    return Messages.get("sendMailFailed") + ": " + e.getMessage();
                }
            }
        }
        return null;
    }

    public Set<String> sendMails(Set<Mail> mails) {
        Set<String> errorMsg = new HashSet<>();
        for (Mail mail : mails) {
            errorMsg.add(send(mail, true));
        }

        errorMsg.remove(null);
        return errorMsg;
    }

    private void sendMimeMessage(Mail mail) throws MessagingException {
        MimeMessage message = new MimeMessage(mailSession);
        message.setHeader("X-bfabric-baseurl", mail.getMailHelper().getConfiguration().getBaseUrl());
        message.setHeader("X-bfabric-deployer", mail.getMailHelper().getConfiguration().getDeployer().getValue());
        message.setHeader("X-bfabric-environment", mail.getMailHelper().getConfiguration().getEnvironment().getValue());
        message.setHeader("X-bfabric-recipients", mail.getRecipientsLoginList());
        message.setFrom(mail.getFrom());
        String replyToAddress = mail.getReplyToAddress();
        if (replyToAddress != null) {
            InternetAddress[] replyToAddressIA = { new InternetAddress(replyToAddress) };
            message.setReplyTo(replyToAddressIA);
        }
        message.setRecipients(Message.RecipientType.TO, mail.getMailHelper().getTo().toArray(new InternetAddress[0]));
        message.setRecipients(Message.RecipientType.CC, mail.getMailHelper().getCc().toArray(new InternetAddress[0]));
        message.setRecipients(Message.RecipientType.BCC, mail.getMailHelper().getBcc().toArray(new InternetAddress[0]));
        message.setSubject(mail.getSubject());
        message.setSentDate(new Date());

        Multipart multipart = new MimeMultipart();
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(mail.getMessage(), "text/html; charset=UTF-8");
        multipart.addBodyPart(messageBodyPart);
        for (File file : mail.getAttachments()) {
            BodyPart attachmentBodyPart = new MimeBodyPart();
            attachmentBodyPart.setFileName(file.getName());
            attachmentBodyPart.setDataHandler(new DataHandler(new FileDataSource(file)));
            multipart.addBodyPart(attachmentBodyPart);
        }
        message.setContent(multipart);
        Transport.send(message);
    }
}
