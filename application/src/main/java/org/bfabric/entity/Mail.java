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


package org.bfabric.entity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.NotEntityLoggable;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.service.UserService;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.MailHelper;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
public class Mail extends AbstractParentDependentBaseEntity implements ShowScreen, NotEntityLoggable {

    private static final long serialVersionUID = 1;

    @ManyToMany
    @JoinTable(name = "mailrecipient", joinColumns = @JoinColumn(name = "mailid"), inverseJoinColumns = @JoinColumn(name = "userid"))
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "recipient")
    private final List<User> recipients = new ArrayList<>();

    @Transient
    private Set<File> attachments = new HashSet<>();

    @Transient
    private MailHelper mailHelper = new MailHelper();

    @NotBlank
    @XmlElement
    private String message;

    @NotBlank
    @XmlElement
    private String recipientsAddressList;

    @Transient
    private String replyToAddress;

    @NotBlank
    @Size(max = 256)
    @XmlElement
    private String subject;

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private MailTypeEnum type;

    public Mail() {
        super();
    }

    public void addRecipient(User recipient) {
        if (recipient != null && !getRecipients().contains(recipient)) {
            getRecipients().add(recipient);
        }
    }

    public void addRecipientAddressToList(String address) {
        if (StringHelper.isNotEmpty(address)) {
            if (StringHelper.isEmpty(recipientsAddressList)) {
                this.recipientsAddressList = Constants.EMPTY_STRING;
            } else {
                this.recipientsAddressList += ", ";
            }
            this.recipientsAddressList += address;
        }
    }

    public void addRecipientIfEmployee(User recipient) {
        if (recipient != null && recipient.isEmployee()) {
            addRecipient(recipient);
        }
    }

    public void addRecipients(Collection<User> recipientsList) {
        if (recipientsList != null && !recipientsList.isEmpty()) {
            for (User recipient : recipientsList) {
                addRecipient(recipient);
            }
        }
    }

    public void addRecipientsIfEmployee(Collection<User> recipientsList) {
        if (recipientsList != null && !recipientsList.isEmpty()) {
            for (User recipient : recipientsList) {
                addRecipientIfEmployee(recipient);
            }
        }
    }

    public void addRecipientsInternal(Container container) {
        if (container != null) {
            // Add all coaches.
            addRecipientIfEmployee(container.getCoach());
            addRecipientIfEmployee(container.getCoachBackup());
            addRecipientIfEmployee(container.getBioinformatician());
            if (container.getProject() != null) {
                addRecipientIfEmployee(container.getProject().getCoach());
                addRecipientIfEmployee(container.getProject().getCoachBackup());
                addRecipientIfEmployee(container.getProject().getBioinformatician());
            }

            // Add all users associated with the service type of the container.
            if (container.getServiceType() != null) {
                addRecipientsIfEmployee(container.getServiceType().getUsers());
                for (OrderItem orderItem : container.getOrderItems()) {
                    if (orderItem.getService() != null) {
                        addRecipientsIfEmployee(orderItem.getService().getUsers());
                    }
                }
            }

            // Send all container comment mails to all internal members.
            if (container.getInternalMembers() != null && !container.getInternalMembers().isEmpty()) {
                addRecipientsIfEmployee(container.getInternalMembers());
            } else if (container.getProject() != null) {
                addRecipients(container.getProject().getInternalMembers());
            }

            if (container.isInFinalState() && getRecipients().isEmpty()) {
                addRecipients(CDI.current().select(UserService.class).get().getReviewManagers());
            }

            // Add all users tracking the container.
            addRecipients(container.getTrackingUsers());
        }
    }

    public void generateRecipientAddressList() {
        for (InternetAddress internetAddress : getMailHelper().getTo()) {
            addRecipientAddressToList(internetAddress.getAddress());
        }
        for (InternetAddress internetAddress : getMailHelper().getCc()) {
            addRecipientAddressToList(internetAddress.getAddress());
        }
        for (InternetAddress internetAddress : getMailHelper().getBcc()) {
            addRecipientAddressToList(internetAddress.getAddress());
        }
    }

    public Set<File> getAttachments() {
        return attachments;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.MAILMANAGER;
    }

    @Override
    public String getDisplayName() {
        return getId() + " - " + getSubject();
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getType() != null) {
            addEntityInfoItem(summary, "type", getType().name());
        }
        if (StringHelper.isNotEmpty(getSubject())) {
            addEntityInfoItem(summary, "subject", getSubject());
        }
        if (StringHelper.isNotEmpty(getRecipientsAddressList())) {
            addEntityInfoItem(summary, "recipients", getRecipientsAddressList());
        }
        if (StringHelper.isNotEmpty(getMessage())) {
            addEntityInfoItem(summary, "message", StringHelper.trimCommentText(StringHelper.removeEmptyLines(StringHelper.html2text(getMessage()))));
        }
        return summary.toString();
    }

    public InternetAddress getFrom() throws AddressException {
        return Optional.ofNullable(getMailHelper().getFrom()).orElse(new InternetAddress(getType().getMailSenderEnum().getAddress()));
    }

    public Object getInput(String attribute) {
        return getMailHelper().getInput().get(attribute);
    }

    public MailHelper getMailHelper() {
        if (mailHelper == null) {
            mailHelper = new MailHelper();
        }
        return mailHelper;
    }

    public String getMessage() {
        return message;
    }

    public String getMessagePlainText() {
        return StringHelper.html2text(getMessage());
    }

    public String getMessageSafeHtml() {
        return StringHelper.getSafeHtml(getMessage());
    }

    public String getMessageTrunc(int maxLength) {
        return StringHelper.truncate(getMessagePlainText(), maxLength);
    }

    public User getRecipient() {
        return !getRecipients().isEmpty() ? getRecipients().iterator().next() : null;
    }

    public List<User> getRecipients() {
        return recipients;
    }

    public String getRecipientsAddressList() {
        return recipientsAddressList;
    }

    public String getRecipientsLoginList() {
        return CollectionHelper.print(getRecipients(), "getLogin");
    }

    public String getRecipientsNamesList() {
        return CollectionHelper.print(getRecipients(), "getFullName");
    }

    public String getReplyToAddress() {
        if (replyToAddress == null && getType() != null) {
            replyToAddress = getType().getMailSenderEnum().getReplyToAddress();
        }
        return replyToAddress;
    }

    public String getSubject() {
        return subject;
    }

    public MailTypeEnum getType() {
        return type;
    }

    @Override
    public boolean isCreatable() {
        return true;
    }

    public boolean isCurrentUserSet() {
        return isInputSet("currentUser");
    }

    public boolean isInputSet(String attribute) {
        return getInput(attribute) != null;
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.MAILREADER) || isUserReadable();
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    @Override
    public boolean isUpdatableWS() {
        return false;
    }

    public boolean isUserReadable() {
        return isParent(getCurrentUser()) || getParent() != null && getParent() instanceof Container && ((Container) getParent()).isMailsUserReadable();
    }

    public void replaceMacros(User recipient) {
        String loginMacro = "\\$login";
        String fullnameMacro = "\\$fullname";
        String emailMacro = "\\$email";

        setSubject(getSubject().replaceAll(loginMacro, recipient.getLogin()));
        setSubject(getSubject().replaceAll(fullnameMacro, recipient.getFullName()));
        setSubject(getSubject().replaceAll(emailMacro, recipient.getEmail()));

        setMessage(getMessage().replaceAll(loginMacro, recipient.getLogin()));
        setMessage(getMessage().replaceAll(fullnameMacro, recipient.getFullName()));
        setMessage(getMessage().replaceAll(emailMacro, recipient.getEmail()));
    }

    public void setAttachments(Set<File> attachments) {
        this.attachments = attachments;
    }

    public void setCachedUser(User cachedUser) {
        setInput("cachedUser", cachedUser);
    }

    public void setCurrentUser(User currentUser) {
        setInput("currentUser", currentUser);
    }

    public void setInput(String attribute, Object value) {
        getMailHelper().getInput().put(attribute, value);
    }

    public void setMailHelper(MailHelper mailHelper) {
        this.mailHelper = mailHelper;
    }

    public void setMessage(String message) {
        this.message = StringHelper.formatMailMessage(message);
    }

    public void setRecipient(User recipient) {
        getRecipients().clear();
        addRecipient(recipient);
    }

    public void setRecipients(Collection<User> recipients) {
        getRecipients().clear();
        addRecipients(recipients);
    }

    public void setReplyToAddress(String replyToAddress) {
        this.replyToAddress = replyToAddress;
    }

    public void setSubject(String subject) {
        // Mail subject length is restricted to 256 = 253 + 3 for the terminator "..."!
        this.subject = StringHelper.truncate(StringHelper.trim(subject), 253);
    }

    public void setSubjectPrefix(String prefix) {
        StringBuilder prefixBuilder = new StringBuilder();
        StringBuilder subjectBuilder = new StringBuilder();

        if (StringHelper.isNotEmpty(prefix)) {
            prefixBuilder.append(prefix).append(" ");
        }

        if (!getSubject().startsWith(prefixBuilder.toString())) {
            subjectBuilder.append(prefixBuilder);
        }

        subjectBuilder.append(getSubject());

        setSubject(subjectBuilder.toString());
    }

    public void setType(MailTypeEnum mailTypeEnum) {
        setType(mailTypeEnum, Constants.EMPTY_STRING, Constants.EMPTY_STRING);
    }

    public void setType(MailTypeEnum mailTypeEnum, String prefix) {
        setType(mailTypeEnum, prefix, Constants.EMPTY_STRING);
    }

    public void setType(MailTypeEnum type, String prefix, String postfix) {
        this.type = type;
        if (type != null) {
            setSubject(type.getSubject(prefix, postfix));
        }
    }
}