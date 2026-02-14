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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.api.ParentDependent;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.indexer.IndexMapContent;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.indexer.enums.IndexMapEnum;
import org.bfabric.service.CommentService;
import org.bfabric.util.BfabricUploadedFile;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.MailRecipientHelper;
import org.bfabric.util.RepositoryHelper;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.DiscriminatorOptions;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "discriminator", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorOptions(force = true)
@NamedQuery(name = "Comment.findLastCommentIdByParent", query = "SELECT MAX(a.id) FROM Comment a WHERE a.parentId = :parentId AND a.discriminator = :discriminator")
@NamedQuery(name = "Comment.countByParentIdsAndType", query = "SELECT COUNT(a.id) FROM Comment a WHERE a.parentId in (:parentIds) AND a.discriminator = :discriminator")
@NamedQuery(name = "Comment.countPinnedByParentIdsAndType", query = "SELECT COUNT(a.id) FROM Comment a WHERE a.parentId in (:parentIds) AND a.discriminator = :discriminator AND a.pinned = TRUE")
@NamedQuery(name = "Comment.findContainerCommentsReadableByUserAfterRevokingEmployeeRights", query = "SELECT DISTINCT a FROM Comment a, Container container WHERE a.parentId = container.id AND a.internal = false AND a.discriminator IN (:discriminators) AND (container.id IN (:containerIds) OR container.discriminator = 'Project' AND container.status = 'PUBLISHED' OR container.discriminator = 'Order' AND container.project.id = (SELECT p.id FROM Container o, Container p WHERE container.id = o.id AND o.project.id = p.id AND p.status = 'PUBLISHED'))")
public class Comment extends AbstractComment implements ParentDependent, Indexable {

    private static final long serialVersionUID = 1;

    @Column(insertable = false, updatable = false)
    protected long parentId;

    @XmlElement
    protected String parentClassName;

    @Transient
    Boolean lastComment;

    @Transient
    MailRecipientHelper mailRecipientHelper = new MailRecipientHelper(this);

    @ManyToMany
    @JoinTable(name = "commentacknowledgedbyuser", joinColumns = @JoinColumn(name = "commentid"), inverseJoinColumns = @JoinColumn(name = "userid"))
    @OrderBy("lastName")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<User> acknowledgedBy = new HashSet<>();

    @OneToMany(mappedBy = "comment", cascade = { CascadeType.MERGE, CascadeType.REMOVE })
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Attachment> attachments = new HashSet<>();

    @Transient
    private transient CommentTemplate commentTemplate;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(insertable = false, updatable = false)
    private CommentDiscriminator discriminator;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean internal = false;

    @Transient
    private boolean internalChanged = false;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean pinned = false;

    @OneToMany(mappedBy = "replyTo")
    @OrderBy("created DESC")
    private Set<Comment> replies = new HashSet<>();

    @Transient
    private boolean replyHistoryFlag = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replytoid")
    @XmlIDREF
    private Comment replyTo;

    @Transient
    private boolean sendMailExternals = !isManaged() && !(isOrderNote() || isOrderResult() || isInstrumentReservationNote() || isInstrumentEventNote());

    @Transient
    private boolean sendMailInternals = !isManaged() && !(isOrderNote() || isOrderResult() || isInstrumentReservationNote() || isInstrumentEventNote());

    @ManyToMany
    @JoinTable(name = "commentstarredbyuser", joinColumns = @JoinColumn(name = "commentid"), inverseJoinColumns = @JoinColumn(name = "userid"))
    @OrderBy("lastName")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<User> starredBy = new HashSet<>();

    @Size(max = 32)
    @XmlElement
    private String subject;

    @ManyToMany
    @JoinTable(name = "commentviewedbyuser", joinColumns = @JoinColumn(name = "commentid"), inverseJoinColumns = @JoinColumn(name = "userid"))
    @OrderBy("lastName")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<User> viewedBy = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "commentworkunit", joinColumns = @JoinColumn(name = "commentid"), inverseJoinColumns = @JoinColumn(name = "workunitid"))
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Workunit> workunits = new HashSet<>();

    public Comment() {
    }

    public void addAttachment(BfabricUploadedFile uploadedFile) {
        final Attachment attachment = new Attachment(this, uploadedFile);
        getAttachments().add(attachment);
    }

    public void changeReplyHistoryFlag(ValueChangeEvent event) {
        setReplyHistoryFlag((Boolean) event.getNewValue());
    }

    public void commentTemplateChanged(ValueChangeEvent event) {
        commentTemplate = (CommentTemplate) event.getNewValue();
        if (commentTemplate != null) {
            setComment(commentTemplate.getContent());
            replaceMacro("\\$currentUserFullName", getCurrentUser().getFullName());
            replaceMacro("\\$currentUserFirstName", getCurrentUser().getFirstName());
            if (getParent() instanceof Container) {
                Container container = (Container) getParent();
                replaceMacro("\\$requesterFullName", container.getRequester().getFullName());
                replaceMacro("\\$requesterFirstName", container.getRequester().getFirstName());
                replaceMacro("\\$contactFullName", container.getContact().getFullName());
                replaceMacro("\\$contactFirstName", container.getContact().getFirstName());
                replaceMacro("\\$budgetOfficerFullName", container.getBudgetOfficer().getFullName());
                replaceMacro("\\$budgetOfficerFirstName", container.getBudgetOfficer().getFirstName());
                if (container.getLeader() != null) {
                    replaceMacro("\\$leaderFullName", container.getLeader().getFullName());
                    replaceMacro("\\$leaderFirstName", container.getLeader().getFirstName());
                }
            }
        }
    }

    public Mail createMail() {
        Mail mail = null;
        if (isSendMail() && !getMailRecipientHelper().getUsers().isEmpty()) {
            mail = new Mail();
            mail.addRecipients(getMailRecipientHelper().getUsers());
            mail.setParent(getParent());
            mail.setType(getMailTypeEnum(), getParent().getTrimmedClassName() + " " + getParent().getId());
            mail.setInput("configuration", getConfiguration());
            mail.setInput("comment", this);
            mail.setInput("parent", getParent());

            if (getParent() instanceof Container) {
                mail.setInput("container", getParent());
            }

            if (StringHelper.isNotEmpty(getSubject())) {
                mail.setSubject(mail.getSubject() + " - " + getSubject());
            }
        }
        return mail;
    }

    public Set<User> getAcknowledgedBy() {
        return acknowledgedBy;
    }

    public String getAcknowledgedByHint(User currentUser) {
        if (currentUser != null && !getAcknowledgedBy().isEmpty()) {
            StringBuilder hint = new StringBuilder(Messages.get("acknowledgedBy"));
            if (getAcknowledgedBy().contains(currentUser)) {
                hint.append("\n").append(currentUser.getFullNameLogin());
            }
            for (User user : getAcknowledgedBy()) {
                if (!user.equals(currentUser)) {
                    hint.append("\n").append(user.getFullNameLogin());
                }
            }
            return hint.toString();
        }
        return null;
    }

    public Attachment getAttachment() {
        return getAttachments() != null && !getAttachments().isEmpty() ? getAttachments().iterator().next() : null;
    }

    // Uncomment annotation if entity logging for attachments is revised (Issue #51).
    // @XmlElement(name = "attachments")
    public String getAttachmentNames() {
        return CollectionHelper.print(getAttachments(), "getName");
    }

    public Set<Attachment> getAttachments() {
        return attachments;
    }

    public String getAttachmentsInfo() {
        StringBuilder attachmentsInfo = new StringBuilder();
        if (!getAttachments().isEmpty()) {
            attachmentsInfo.append("\n---\n");
            attachmentsInfo.append(getAttachments().size()).append(" attachments:");
            attachmentsInfo.append("\n---\n");
            for (Attachment attachment : getAttachments()) {
                attachmentsInfo.append(attachment.getFileName()).append(" ").append(attachment.getPrintSize()).append("\n");
            }
        }
        return attachmentsInfo.toString();
    }

    private long getAttachmentsSize() {
        long attachmentsSize = 0;
        for (final Attachment attachment : getAttachments()) {
            attachmentsSize += attachment.getSize();
        }
        return attachmentsSize;
    }

    public String getCategory() {
        return getDiscriminator() != null ? getDiscriminator().getCategory() : null;
    }

    public Set<Attachment> getCheckedAttachments() {
        final Set<Attachment> checkedAttachments = new HashSet<>();
        for (final Attachment attachment : getAttachments()) {
            if (attachment.isChecked()) {
                checkedAttachments.add(attachment);
            }
        }
        return checkedAttachments;
    }

    @Override
    public String getClassLabel() {
        return Messages.get(StringHelper.firstLower(Comment.class.getSimpleName()));
    }

    @Override
    public String getClassName() {
        return Comment.class.getSimpleName().toLowerCase();
    }

    public String getCommentOrAttachmentsInfo() {
        return StringHelper.isNotEmpty(getComment()) ? getCommentTrunc(60) : "Attachment available";
    }

    public CommentTemplate getCommentTemplate() {
        return commentTemplate;
    }

    public String getCommentWithAttachmentsInfo() {
        return (StringHelper.isNotEmpty(getComment()) ? getComment() : Constants.EMPTY_STRING) + getAttachmentsInfo();
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.COMMENTMANAGER;
    }

    public String getDirectory() {
        return getDiscriminator() != null ? getDiscriminator().getDirectoryPrefix() + "_" + getId() : null;
    }

    public CommentDiscriminator getDiscriminator() {
        return discriminator;
    }

    @Transient
    public String getDiscriminatorValue() {
        return getClass().getAnnotation(DiscriminatorValue.class).value();
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (StringHelper.isNotEmpty(getSubject())) {
            addEntityInfoItem(summary, "subject", getSubject());
        }
        if (getAttachments() != null && !getAttachments().isEmpty()) {
            addEntityInfoItem(summary, "attachments", getAttachments().size());
        }
        if (getDiscriminator() != null) {
            addEntityInfoItem(summary, "discriminator", getDiscriminator().getLabel());
        }
        if (getParent() != null) {
            addEntityInfoItem(summary, "parentClass", getParentClassName());
            addEntityInfoItem(summary, "parentId", getParentId());
        }
        if (getReplyTo() != null && !getReplyTo().isEmpty()) {
            addEntityInfoItem(summary, "replyTo", getReplyTo().getId());
        }
        if (StringHelper.isNotEmpty(getComment())) {
            addEntityInfoItem(summary, "comment", getComment());
        }
        return summary.toString();
    }

    public List<Comment> getHistory() {
        return getParent() != null ? getParent().getCommentsCurrentUser() : new ArrayList<>();
    }

    public List<Comment> getHistoryList() {
        if (isReplyHistoryFlag() && getReplyTo() != null) {
            return getReplyToTransitive();
        }
        return getHistory();
    }

    @Override
    public void getIndexFields(Collection<String> fields) {
        fields.addAll(getIndexListingFields());
        fields.add(IndexMapContentEnum.PARENTID.getField());
        fields.add(IndexMapContentEnum.PARENTCLASSNAME.getField());
    }

    @Override
    public List<String> getIndexListingFields() {
        final List<String> fields = new ArrayList<>();
        fields.add(IndexMapContentEnum.PARENT.getField());
        fields.add(IndexMapContentEnum.TYPE.getField());
        return fields;
    }

    @Override
    public IndexMapContent getIndexMapContent() throws Exception {
        IndexMapContent content = super.getIndexMapContent();

        content.add(IndexMapContentEnum.NAME, getIndexedComment());
        content.add(IndexMapContentEnum.TYPE, getIndexedDiscriminator());

        if (getParent() != null) {
            content.add(IndexMapContentEnum.PARENT, getParent());
            content.add(IndexMapContentEnum.PARENTID, getParentId());
            content.add(IndexMapContentEnum.PARENTCLASSNAME, getParentClassName());
        }
        return content;
    }

    @Override
    public IndexMapEnum getIndexMapEnum() {
        return IndexMapEnum.COMMENT;
    }

    @Override
    public String getIndexedComment() {
        if (getComment() != null && !getComment().isEmpty()) {
            return getComment();
        }
        return !getAttachments().isEmpty() ? Messages.get("includesAttachmentsHint") : Constants.EMPTY_STRING;
    }

    public String getIndexedDiscriminator() {
        String ret = Constants.EMPTY_STRING;
        if (getDiscriminator() != null) {
            ret = getDiscriminator().toString();
        }
        if (isInternal()) {
            ret += "-" + getInternalLabel();
        }
        return ret;
    }

    public String getInternalLabel() {
        return isInternal() ? Messages.get("internalLabel") : null;
    }

    public MailRecipientHelper getMailRecipientHelper() {
        return mailRecipientHelper;
    }

    public List<String> getMailTargetList() {
        return getDiscriminator() != null ? getDiscriminator().getMailTargetList(isInternal()) : null;
    }

    public MailTypeEnum getMailTypeEnum() {
        if (isParentClassOrder()) {
            if (isCategoryComment()) {
                if (hasCurrentUserRoleEnum(RoleEnum.COMMENTMANAGER)) {
                    if (isInternal()) {
                        return MailTypeEnum.CONTAINER_EMPLOYEE_INTERNAL_COMMENT;
                    }
                    return MailTypeEnum.CONTAINER_EMPLOYEE_COMMENT;
                }
                return MailTypeEnum.CONTAINER_USER_COMMENT;
            }
            if (isCategoryNote()) {
                return MailTypeEnum.CONTAINER_NOTE;
            }
            if (isCategoryResult()) {
                return MailTypeEnum.CONTAINER_RESULT;
            }
        }

        if (isParentClass(Project.class) && isCategoryComment()) {
            if (isInternal()) {
                return MailTypeEnum.CONTAINER_INTERNAL_COMMENT;
            }
            return MailTypeEnum.CONTAINER_COMMENT;
        }

        if (isParentClass(Plate.class) && isCategoryComment()) {
            if (isInternal()) {
                return MailTypeEnum.PLATE_INTERNAL_COMMENT;
            }
            return MailTypeEnum.PLATE_COMMENT;
        }

        if (isParentClass(Run.class) && isCategoryComment()) {
            if (isInternal()) {
                return MailTypeEnum.RUN_INTERNAL_COMMENT;
            }
            return MailTypeEnum.RUN_COMMENT;
        }

        if (isParentClass(Sample.class) && isCategoryComment()) {
            if (isInternal()) {
                return MailTypeEnum.SAMPLE_INTERNAL_COMMENT;
            }
            return MailTypeEnum.SAMPLE_COMMENT;
        }

        if (isParentClass(Workunit.class) && isCategoryComment()) {
            if (isInternal()) {
                return MailTypeEnum.WORKUNIT_INTERNAL_COMMENT;
            }
            return MailTypeEnum.WORKUNIT_COMMENT;
        }

        if (isParentClass(Dataset.class) && isCategoryComment()) {
            if (isInternal()) {
                return MailTypeEnum.DATASET_INTERNAL_COMMENT;
            }
            return MailTypeEnum.DATASET_COMMENT;
        }

        if (isParentClass(Annotation.class) && isCategoryComment()) {
            if (isInternal()) {
                return MailTypeEnum.ANNOTATION_INTERNAL_COMMENT;
            }
            return MailTypeEnum.ANNOTATION_COMMENT;
        }

        if (isParentClass(Offer.class) && isCategoryComment()) {
            if (isInternal()) {
                return MailTypeEnum.OFFER_INTERNAL_COMMENT;
            }
            return MailTypeEnum.OFFER_COMMENT;
        }

        if (isParentClass(Offer.class) && isCategoryNote()) {
            return MailTypeEnum.OFFER_NOTE;
        }

        if (isParentClass(Consumable.class) && isCategoryNote()) {
            return MailTypeEnum.CONSUMABLE_NOTE;
        }

        if (isParentClass(Contract.class) && isCategoryNote()) {
            return MailTypeEnum.CONTRACT_NOTE;
        }

        if (isParentClass(Service.class) && isCategoryNote()) {
            return MailTypeEnum.SERVICE_NOTE;
        }

        if (isParentClass(ServiceArea.class) && isCategoryNote()) {
            return MailTypeEnum.SERVICEAREA_NOTE;
        }

        if (isParentClass(ServiceType.class) && isCategoryNote()) {
            return MailTypeEnum.SERVICETYPE_NOTE;
        }

        if (isParentClass(Instrument.class) && isCategoryNote()) {
            return MailTypeEnum.INSTRUMENT_NOTE;
        }

        if (isParentClass(InstrumentEvent.class) && isCategoryNote()) {
            return MailTypeEnum.INSTRUMENT_EVENT_NOTE;
        }

        if (isParentClass(InstrumentReservation.class) && isCategoryNote()) {
            return MailTypeEnum.INSTRUMENT_RESERVATION_NOTE;
        }

        if (isParentClass(Purchase.class) && isCategoryNote()) {
            return MailTypeEnum.PURCHASE_NOTE;
        }

        if (isParentClass(SamplePreparationProtocol.class) && isCategoryNote()) {
            return MailTypeEnum.SAMPLE_PREPARATION_PROTOCOL_NOTE;
        }
        if (isParentClass(WorkflowStep.class) && isCategoryComment()) {
            if (isInternal()) {
                return MailTypeEnum.WORKFLOW_STEP_INTERNAL_COMMENT;
            }
            return MailTypeEnum.WORKFLOW_STEP_COMMENT;
        }

        return null;
    }

    public Set<Attachment> getNewAttachments() {
        final Set<Attachment> newAttachments = new HashSet<>();
        for (final Attachment attachment : getAttachments()) {
            if (attachment.getUploadedFile() != null) {
                newAttachments.add(attachment);
            }
        }
        return newAttachments;
    }

    public String getNoteLabel() {
        return isOrderNote() ? Messages.get("noteLabel") : null;
    }

    public AbstractEntity getParent() {
        return null;
    }

    @Override
    public String getParentClassName() {
        return parentClassName;
    }

    @Override
    public Long getParentId() {
        return parentId;
    }

    public String getParentTab() {
        return getDiscriminator() != null ? getDiscriminator().toString().toLowerCase().substring(getDiscriminator().toString().lastIndexOf("_") + 1) + Constants.PLURAL_S : null;
    }

    @Override
    public String getParentUrlShowScreen() {
        return getParent() != null ? getParent().getUrlShowScreen() : null;
    }

    @Override
    public String getRelativeRepositoryPath() {
        return getParent().getRelativeRepositoryPath() + File.separator + getDirectory();
    }

    public Set<Comment> getReplies() {
        return replies;
    }

    public Comment getReplyTo() {
        return replyTo;
    }

    public Long getReplyToId() {
        return getReplyTo() != null ? getReplyTo().getId() : null;
    }

    public List<Comment> getReplyToTransitive() {
        List<Comment> replyToTransitive = new LinkedList<>();
        if (getReplyTo() != null) {
            replyToTransitive.add(getReplyTo());
            replyToTransitive.addAll(getReplyTo().getReplyToTransitive());
        }
        return replyToTransitive;
    }

    public Set<User> getReplyToUsers() {
        Set<User> replyToUsers = new HashSet<>();
        for (Comment comment : getReplyToTransitive()) {
            replyToUsers.add(comment.getCreatedByUser());
        }
        return replyToUsers;
    }

    public long getSize() {
        return getAttachmentsSize() + (getComment() != null ? getComment().length() : 0);
    }

    public Set<User> getStarredBy() {
        return starredBy;
    }

    public String getStarredByHint(User currentUser) {
        if (currentUser != null && !getStarredBy().isEmpty()) {
            StringBuilder hint = new StringBuilder(Messages.get("starredBy"));
            if (getStarredBy().contains(currentUser)) {
                hint.append("\n").append(currentUser.getFullNameLogin());
            }
            for (User user : getStarredBy()) {
                if (!user.equals(currentUser)) {
                    hint.append("\n").append(user.getFullNameLogin());
                }
            }
            return hint.toString();
        }
        return null;
    }

    public String getSubject() {
        return subject;
    }

    @Transient
    public String getTab() {
        return getDiscriminatorValue().substring(getDiscriminatorValue().lastIndexOf("_") + 1).toLowerCase() + Constants.PLURAL_S;
    }

    public Set<Attachment> getUncheckedAttachments() {
        final Set<Attachment> uncheckedAttachments = new HashSet<>();
        for (final Attachment attachment : getAttachments()) {
            if (!attachment.isChecked()) {
                uncheckedAttachments.add(attachment);
            }
        }
        return uncheckedAttachments;
    }

    @Override
    public String getUrlScreen(String screen) {
        return "/comment/" + screen + ".xhtml";
    }

    public Set<User> getViewedBy() {
        return viewedBy;
    }

    public String getViewedByHint(User currentUser) {
        if (currentUser != null && currentUser.hasRoleImplicit(RoleEnum.EMPLOYEE)) {
            StringBuilder hint = new StringBuilder(Messages.get("viewedBy"));
            if (!getViewedBy().isEmpty()) {
                if (getViewedBy().contains(currentUser)) {
                    hint.append("\n").append(currentUser.getFullNameLogin());
                }
                for (User user : getViewedBy()) {
                    if (!user.equals(currentUser)) {
                        hint.append("\n").append(user.getFullNameLogin());
                    }
                }
            } else {
                hint.append("\n").append(Messages.get("nobodyHint"));
            }
            return hint.toString();
        }
        return null;
    }

    public Set<Workunit> getWorkunits() {
        return workunits;
    }

    public List<Workunit> getWorkunitsAsList() {
        return CollectionHelper.asList(workunits);
    }

    public void internalFlagChanged(ValueChangeEvent event) {
        setInternal((Boolean) event.getNewValue());
        mailRecipientHelper = new MailRecipientHelper(this);
        mailRecipientHelper.init();
    }

    public boolean isCategoryComment() {
        return Constants.COMMENT_CATEGORY_COMMENT.equals(getCategory());
    }

    public boolean isCategoryNote() {
        return Constants.COMMENT_CATEGORY_NOTE.equals(getCategory());
    }

    public boolean isCategoryResult() {
        return Constants.COMMENT_CATEGORY_RESULT.equals(getCategory());
    }

    public boolean isContainerComment() {
        return getParent() instanceof Container;
    }

    @Override
    public boolean isCreatable() {
        return true;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && replies.isEmpty();
    }

    public boolean isEmpty() {
        return StringHelper.isEmpty(getComment()) && getAttachments().isEmpty();
    }

    public boolean isInstrumentEventNote() {
        return CommentDiscriminator.INSTRUMENT_EVENT_NOTE.equals(getDiscriminator());
    }

    public boolean isInstrumentReservationNote() {
        return CommentDiscriminator.INSTRUMENT_RESERVATION_NOTE.equals(getDiscriminator());
    }

    public boolean isInternal() {
        return internal;
    }

    public boolean isInternalChanged() {
        return internalChanged;
    }

    public boolean isLastComment() {
        if (lastComment == null) {
            lastComment = CDI.current().select(CommentService.class).get().isLastComment(this);
        }
        return lastComment;
    }

    public boolean isLastCommentAndCreatedByUser() {
        return isCreator() && isLastComment();
    }

    public boolean isMailRecipientsEditingEnabled() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public boolean isOrderComment() {
        return CommentDiscriminator.ORDER_COMMENT.equals(getDiscriminator());
    }

    public boolean isOrderNote() {
        return CommentDiscriminator.ORDER_NOTE.equals(getDiscriminator());
    }

    public boolean isOrderResult() {
        return CommentDiscriminator.ORDER_RESULT.equals(getDiscriminator());
    }

    public boolean isParentClass(Class<?> clazz) {
        return clazz != null && clazz.getSimpleName().equals(getParentClassName());
    }

    public boolean isParentClassOrder() {
        return isParentClass(Order.class);
    }

    public boolean isPinned() {
        return pinned;
    }

    public boolean isProjectComment() {
        return CommentDiscriminator.PROJECT_COMMENT.equals(getDiscriminator());
    }

    @Override
    public boolean isReadable() {
        return super.isReadable() && getParent().isReadable();
    }

    public boolean isReplyEnabled() {
        return isCategoryComment() || getCurrentUser().hasRoleImplicit(RoleEnum.COMMENTMANAGER);
    }

    public boolean isReplyHistoryFlag() {
        return replyHistoryFlag;
    }

    @Override
    public boolean isSendMail() {
        return isSendMailExternals() && !isInternal() || isSendMailInternals() || !mailRecipientHelper.getUsers().isEmpty();
    }

    public boolean isSendMailExternals() {
        return sendMailExternals;
    }

    public boolean isSendMailInternals() {
        return sendMailInternals;
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(RoleEnum.ADMIN) || hasCurrentUserRoleEnum(getDefaultRequiredRole()) && (isCategoryResult() || isCategoryNote() || isLastCommentAndCreatedByUser()) && replies
            .isEmpty();
    }

    public void moveAttachments() {
        final Storage internalStorage = RepositoryHelper.getLocalStorage(true);
        final Storage externalStorage = RepositoryHelper.getLocalStorage(false);
        String oldPath, newPath;

        if (isInternal()) {
            oldPath = externalStorage.getBasePath();
            newPath = internalStorage.getBasePath();
        } else {
            oldPath = internalStorage.getBasePath();
            newPath = externalStorage.getBasePath();
        }

        final String relativeDirectoryPath = File.separator + getParent().getRelativeRepositoryPath() + File.separator + getDirectory();
        oldPath += relativeDirectoryPath;
        newPath += relativeDirectoryPath;

        RepositoryHelper.moveImports(new File(oldPath), new File(newPath));

        for (final Attachment attachment : getAttachments()) {
            attachment.setStorage(isInternal() ? internalStorage : externalStorage);
        }
    }

    public void removeAttachment(Attachment attachment) {
        getAttachments().remove(attachment);
    }

    public void replaceMacro(String macro, String value) {
        setComment(getComment().replaceAll(macro, value));
    }

    public void setAcknowledgedBy(Set<User> acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
    }

    public void setAttachments(Set<Attachment> attachments) {
        this.attachments = attachments;
    }

    public void setCommentTemplate(CommentTemplate commentTemplate) {
        this.commentTemplate = commentTemplate;
    }

    public void setDiscriminator(CommentDiscriminator discriminator) {
        this.discriminator = discriminator;
    }

    public void setInternal(boolean internal) {
        if (getId() > 0 && this.internal != internal) {
            setInternalChanged(!isInternalChanged());
        }
        this.internal = internal;
    }

    public void setInternalChanged(boolean internalChanged) {
        this.internalChanged = internalChanged;
    }

    public void setParent(AbstractEntity parent) {
        if (parent != null) {
            setParentClassName(parent.getTrimmedClassName());
        }
    }

    public void setParentClassName(String parentClassName) {
        this.parentClassName = StringHelper.format(parentClassName);
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public void setReplyHistoryFlag(boolean replyHistoryFlag) {
        this.replyHistoryFlag = replyHistoryFlag;
    }

    public void setReplyTo(Comment replyTo) {
        this.replyTo = replyTo;
    }

    public void setSendMailExternals(boolean sendMailExternals) {
        this.sendMailExternals = sendMailExternals;
    }

    public void setSendMailInternals(boolean sendMailInternals) {
        this.sendMailInternals = sendMailInternals;
    }

    public void setStarredBy(Set<User> starredBy) {
        this.starredBy = starredBy;
    }

    public void setSubject(String subject) {
        this.subject = StringHelper.format(subject);
    }

    public void setViewedBy(Set<User> viewedBy) {
        this.viewedBy = viewedBy;
    }

    public void setWorkunits(Set<Workunit> workunits) {
        this.workunits = workunits;
    }

    public void setWorkunitsAsList(List<Workunit> workunits) {
        this.workunits = (Set<Workunit>) CollectionHelper.asSet(workunits);
    }

    public void switchAcknowledgedBy(User user) {
        if (user != null) {
            if (getAcknowledgedBy().contains(user)) {
                getAcknowledgedBy().remove(user);
            } else {
                getAcknowledgedBy().add(user);
            }
        }
    }

    public void switchStarredBy(User user) {
        if (user != null) {
            if (getStarredBy().contains(user)) {
                getStarredBy().remove(user);
            } else {
                getStarredBy().add(user);
            }
        }
    }

    public void switchViewedBy(User user) {
        if (user != null) {
            if (getViewedBy().contains(user)) {
                getViewedBy().remove(user);
            } else {
                getViewedBy().add(user);
            }
        }
    }
}
