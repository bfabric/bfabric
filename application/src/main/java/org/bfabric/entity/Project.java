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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.LogActionEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.exception.BfabricValidatorException;
import org.bfabric.indexer.IndexMapContent;
import org.bfabric.indexer.enums.IndexMapEnum;
import org.bfabric.service.CommentService;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.DateUtils;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@DynamicUpdate
@XmlRootElement
@NamedQuery(name = "Project.findByFirstExtensionReportPending", query = "SELECT a FROM Project a WHERE a.status = org.bfabric.enums.StatusEnum.RUNNING and a.extensionReport1 is null and a.extensionReport1ReminderSent = FALSE and a.startDate between :startDateWeek and :startDate")
@NamedQuery(name = "Project.findBySecondExtensionReportPending", query = "SELECT a FROM Project a WHERE a.status = org.bfabric.enums.StatusEnum.RUNNING and a.extensionReport2 is null and a.extensionReport2ReminderSent = FALSE and a.startDate between :startDateWeek and :startDate")
@NamedQuery(name = "Project.findByFinalExtensionReportPending", query = "SELECT a FROM Project a WHERE a.status IN (org.bfabric.enums.StatusEnum.RUNNING, org.bfabric.enums.StatusEnum.FINISHED) and a.extensionReport3 is null and a.extensionReport3ReminderSent = FALSE and a.startDate between :startDateWeek and :startDate")
@NamedQuery(name = "Project.doiCreated", query = "SELECT a FROM Project a WHERE a.doiCreated is not null")
@NamedQuery(name = "Project.doiCreatedAfterTimestamp", query = "SELECT a FROM Project a WHERE a.doiCreated is not null and a.doiCreated >= :timestamp")
public class Project extends Container {

    private static final long serialVersionUID = 1;

    @Transient
    protected Long fullOrdersSize;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    @XmlIDREF
    private Set<ProjectComment> comments = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE })
    @JoinColumn(name = "descriptionid")
    @NotNull
    private Attachment description;

    @NotNull
    @XmlElement
    private LocalDate endDate;

    @Transient
    private boolean endDateChanged;

    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE })
    @JoinColumn(name = "extensionreport1id")
    private Attachment extensionReport1;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean extensionReport1Approved;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean extensionReport1ReminderSent;

    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE })
    @JoinColumn(name = "extensionreport2id")
    private Attachment extensionReport2;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean extensionReport2Approved;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean extensionReport2ReminderSent;

    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE })
    @JoinColumn(name = "extensionreport3id")
    private Attachment extensionReport3;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean extensionReport3Approved;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean extensionReport3ReminderSent;

    @XmlElement
    private LocalDate finalDecisionDate;

    @XmlElement
    private LocalDateTime finishAnnouncedDate;

    @XmlElement
    private LocalDate finishDate;

    @Transient
    private List<Offer> offersOrdersIncluded;

    @OneToMany(mappedBy = "project")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Order> orders = new HashSet<>();

    @XmlElement
    private LocalDateTime privateAnnouncedDate;

    @XmlElement
    private LocalDateTime privateDate;

    @Transient
    private List<Comment> projectAndOrderComments;

    @Transient
    private List<Comment> projectAndOrderCommentsPinned;

    @XmlElement
    private LocalDateTime publishDate;

    @XmlElement
    private LocalDateTime publishGrantedDate;

    @Column(columnDefinition = "TEXT")
    @XmlElement
    private String reminderComment;

    @XmlElement
    private LocalDate reminderDate;

    @OneToMany(mappedBy = "project", cascade = { CascadeType.REMOVE }, orphanRemoval = true)
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<Review> reviews = new ArrayList<>();

    @NotNull
    @XmlElement
    private LocalDate startDate;

    public Project() {
    }

    @Override
    public Set<Mail> changeStatus(StatusEnum statusEnum) {
        Set<Mail> mails = new HashSet<>();
        setCreateAndAddStatus(statusEnum);

        switch (statusEnum) {
        case RUNNING:
            setFinalDecisionDate(LocalDate.now());
            setEndDate(LocalDate.now().plusYears(3));
            break;
        case PUBLISHED:
            setPublishDate(LocalDateTime.now());
            break;
        case FINISHED:
            setFinishDate(LocalDate.now());
            break;
        case PRIVATE:
            setPrivateDate(LocalDateTime.now());
            break;
        case REJECTED:
            setFinalDecisionDate(LocalDate.now());
            break;
        default:
            break;
        }

        return mails;
    }

    @Override
    public Set<Order> getAssociatedContainers() {
        return getOrders();
    }

    public Set<Order> getChargeableOrders() {
        Set<Order> chargeableOrders = new HashSet<>();
        for (Order order : getOrders()) {
            if (order.isChargeable()) {
                chargeableOrders.add(order);
            }
        }
        return chargeableOrders;
    }

    @Override
    public CommentDiscriminator getCommentDiscriminator() {
        return CommentDiscriminator.PROJECT_COMMENT;
    }

    @Override
    public Set<ProjectComment> getComments() {
        return comments;
    }

    @Override
    public List<Comment> getCommentsPinned(boolean isCommentManager, boolean includeOrderComments) {
        projectAndOrderCommentsPinned = CDI.current().select(CommentService.class).get().getProjectAndOrderCommentsPinnedByProject(this, isCommentManager);
        commentsPinnedCurrentUser = CDI.current().select(CommentService.class).get().getCommentsPinnedByParentAndType(this, getCommentDiscriminator(), isCommentManager);
        return includeOrderComments ? projectAndOrderCommentsPinned : commentsPinnedCurrentUser;
    }

    @Override
    public Long getCommentsTotalSize() {
        if (commentsTotalSize == null) {
            commentsTotalSize = getCommentsTotalSize(CDI.current().select(CommentService.class).get().getAllProjectAndOrderCommentsAndNotesAndResultsByProject(this, true));
        }
        return commentsTotalSize;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.CONTAINERMANAGER;
    }

    public Attachment getDescription() {
        return description;
    }

    @Override
    public BigDecimal getDiscount() {
        return BigDecimal.ZERO;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Attachment getExtensionReport(int year) {
        switch (year) {
        case 1:
            return getExtensionReport1();
        case 2:
            return getExtensionReport2();
        case 3:
            return getExtensionReport3();
        default:
            return null;
        }
    }

    public Attachment getExtensionReport1() {
        return extensionReport1;
    }

    public Attachment getExtensionReport2() {
        return extensionReport2;
    }

    public Attachment getExtensionReport3() {
        return extensionReport3;
    }

    public LocalDate getFinalDecisionDate() {
        return finalDecisionDate;
    }

    public LocalDateTime getFinishAnnouncedDate() {
        return finishAnnouncedDate;
    }

    public LocalDate getFinishDate() {
        return finishDate;
    }

    @Override
    public Long getFullSize(boolean includeOrderData) {
        if (fullSize == null) {
            fullSize = super.getFullSize(includeOrderData);

            // Project specific, i.e., description, extension reports one, two and three, and the orders belonging to it if includeOrders is true.
            if (getDescription() != null) {
                fullSize += getDescription().getSize();
            }
            if (getExtensionReport1() != null) {
                fullSize += getExtensionReport1().getSize();
            }
            if (getExtensionReport2() != null) {
                fullSize += getExtensionReport2().getSize();
            }
            if (getExtensionReport3() != null) {
                fullSize += getExtensionReport3().getSize();
            }
        }

        if (fullOrdersSize == null && includeOrderData) {
            fullOrdersSize = 0L;
            for (Order order : getOrders()) {
                fullOrdersSize += order.getFullSize(false);
            }
        }

        return fullSize + (includeOrderData ? Optional.ofNullable(fullOrdersSize).orElse(0L) : 0L);
    }

    @Override
    public IndexMapContent getIndexMapContent() throws Exception {
        IndexMapContent content = super.getIndexMapContent();

        content.add(IndexMapContentEnum.NAME, "p" + getId());
        content.add(IndexMapContentEnum.SUMMARY, getSummary());
        content.add(IndexMapContentEnum.STARTDATE, getStartDate());
        content.add(IndexMapContentEnum.ENDDATE, getEndDate());

        return content;
    }

    @Override
    public IndexMapEnum getIndexMapEnum() {
        return IndexMapEnum.PROJECT;
    }

    // NOTE: Each project must have a leader! IMPORTANT: Do not delete this method since it enforce the NotNull constraint.
    @Override
    @NotNull
    public User getLeader() {
        return super.getLeader();
    }

    public List<Offer> getOrderOffersIncluded() {
        if (offersOrdersIncluded == null) {
            Set<Offer> orderOffers = new HashSet<>();
            for (Order order : getOrders()) {
                orderOffers.addAll(order.getOffers());
            }
            orderOffers.addAll(getOffers());
            offersOrdersIncluded = CollectionHelper.sortObjects(orderOffers);
        }
        return offersOrdersIncluded;
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public Set<Order> getOrdersNonClosed() {
        Set<Order> ordersNonClosed = new HashSet<>();
        for (Order order : getOrders()) {
            if (!order.getStatus().equals(StatusEnum.CLOSED)) {
                ordersNonClosed.add(order);
            }
        }
        return ordersNonClosed;
    }

    public LocalDateTime getPrivateAnnouncedDate() {
        return privateAnnouncedDate;
    }

    public LocalDateTime getPrivateDate() {
        return privateDate;
    }

    public List<Comment> getProjectAndOrderComments() {
        if (projectAndOrderComments == null) {
            return projectAndOrderComments = CDI.current().select(CommentService.class).get().getProjectAndOrderCommentsByProject(this, hasCurrentUserRoleEnum(RoleEnum.COMMENTMANAGER));
        }
        return projectAndOrderComments;
    }

    public LocalDateTime getPublishDate() {
        return publishDate;
    }

    public LocalDateTime getPublishGrantedDate() {
        return publishGrantedDate;
    }

    public String getReminder() {
        StringBuilder reminder = new StringBuilder();
        if (getReminderDate() != null) {
            reminder.append(DateUtils.getDateAsFormattedString(getReminderDate())).append(" ");
        }
        if (getReminderComment() != null) {
            reminder.append(getReminderComment());
        }
        return reminder.toString();
    }

    public String getReminderComment() {
        return reminderComment;
    }

    public LocalDate getReminderDate() {
        return reminderDate;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public Sample getSelectedSample() {
        Sample ret = null;
        for (Sample sample : getSamples()) {
            if (sample.isChecked()) {
                ret = sample;
                break;
            }
        }
        return ret;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    @Override
    @NotBlank
    public String getSummary() {
        return super.getSummary();
    }

    @Override
    public boolean hasBeenAccepted() {
        if (hasBeenAccepted == null) {
            hasBeenAccepted = false;
            for (StandardContainerStatus state : getStates()) {
                if (StatusEnum.RUNNING.getLabel().equals(state.name)) {
                    hasBeenAccepted = true;
                    break;
                }
            }
        }
        return hasBeenAccepted;
    }

    @Override
    public boolean isAccepted() {
        return isRunning() || isFinished() || isPrivate() || isPublished();
    }

    public boolean isAcceptedButNotPrivateOrPublished() {
        return isRunning() || isFinished();
    }

    public boolean isAcceptedButNotPublished() {
        return isRunning() || isFinished() || isPrivate();
    }

    public boolean isApprovable() {
        return !isAccepted() && hasCurrentUserRoleEnum(RoleEnum.REVIEWER);
    }

    public boolean isAttributeUpdatable() {
        return hasCurrentUserRoleEnum(RoleEnum.ADMIN) || hasCurrentUserRoleEnum(getDefaultRequiredRole()) && !isPublished() || isManager(getCurrentUser()) && isPending() || getCurrentUsername()
            .equals(getRequester().getLogin()) && isPending();
    }

    @Override
    public boolean isBillingAddressUpdatable() {
        return isAttributeUpdatable();
    }

    @Override
    public boolean isChargeable() {
        return super.isChargeable() && !isRejected();
    }

    @Override
    public boolean isClosed() {
        return isPublished() && getPublishDate() != null && Period.between(getPublishDate().toLocalDate(), LocalDate.now()).getMonths() > 6;
    }

    public boolean isCommentAddable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || isMember() || hasCurrentUserRoleEnum(RoleEnum.REVIEWER) && isPendingOrReview();
    }

    @Override
    public boolean isComputerLoginEnabled() {
        return isAcceptedButNotPrivateOrPublished();
    }

    @Override
    public boolean isCreatable() {
        return true;
    }

    @Override
    public boolean isDataAccessEnabled() {
        return isAccepted();
    }

    @Override
    public boolean isDeletable() {
        return isPending() && hasCurrentUserRoleEnum(RoleEnum.ADMIN) && super.isDeletable();
    }

    @Override
    public boolean isDownloadable() {
        return super.isDownloadable() && !getStatus().equals(StatusEnum.PENDING) && !getStatus().equals(StatusEnum.REVIEW) && !getStatus().equals(StatusEnum.REJECTED);
    }

    public boolean isEndDateChanged() {
        return endDateChanged;
    }

    @Override
    public boolean isExtensible() {
        return isAcceptedButNotPrivateOrPublished() && (hasCurrentUserRoleEnum(getDefaultRequiredRole()) || isMember()) || isAccepted() && hasCurrentUserRoleEnum(RoleEnum.FEEDER);
    }

    public boolean isExtensionReport1Approved() {
        return extensionReport1Approved;
    }

    public boolean isExtensionReport1ReminderSent() {
        return extensionReport1ReminderSent;
    }

    public boolean isExtensionReport2Approved() {
        return extensionReport2Approved;
    }

    public boolean isExtensionReport2ReminderSent() {
        return extensionReport2ReminderSent;
    }

    public boolean isExtensionReport3Approved() {
        return extensionReport3Approved;
    }

    public boolean isExtensionReport3ReminderSent() {
        return extensionReport3ReminderSent;
    }

    public boolean isExtensionReportApprovable(int year) {
        return hasCurrentUserRoleEnum(RoleEnum.REVIEWMANAGER) && getExtensionReport(year) != null && !isExtensionReportApproved(year);
    }

    private boolean isExtensionReportApproved(int year) {
        if (year == 1) {
            return isExtensionReport1Approved();
        }
        if (year == 2) {
            return isExtensionReport2Approved();
        }
        return year == 3 && isExtensionReport3Approved();
    }

    private boolean isExtensionReportDue(int year) {
        return getStartDate().isBefore(LocalDate.now().minusYears(year).plusDays(1));
    }

    @Override
    public boolean isExtensionReportPending() {
        return isAcceptedButNotPrivateOrPublished() && (isExtensionReportPendingForYear(1) || isExtensionReportPendingForYear(2) || isExtensionReportPendingForYear(3)) && (hasCurrentUserRoleEnum(getDefaultRequiredRole()) || isMember());
    }

    public boolean isExtensionReportPendingForYear(int year) {
        return isExtensionReportDue(year) && getExtensionReport(year) == null && (hasCurrentUserRoleEnum(getDefaultRequiredRole()) || isMember());
    }

    @Override
    public boolean isExtensionReportReview() {
        return isAcceptedButNotPrivateOrPublished() && (isExtensionReportReviewForYear(1) || isExtensionReportReviewForYear(2) || isExtensionReportReviewForYear(3)) && hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public boolean isExtensionReportReviewForYear(int year) {
        return isExtensionReportDue(year) && getExtensionReport(year) != null && !isExtensionReportApproved(year) && (hasCurrentUserRoleEnum(getDefaultRequiredRole()) || isMember());
    }

    public boolean isFinalApprovable() {
        return hasCurrentUserRoleEnum(RoleEnum.REVIEWMANAGER) && isPendingOrReview();
    }

    public boolean isFinalRejectabtable() {
        return hasCurrentUserRoleEnum(RoleEnum.REVIEWMANAGER) && isPendingOrReview();
    }

    public boolean isFinishAnnouncable() {
        return hasCurrentUserRoleEnum(RoleEnum.REVIEWMANAGER) && isRunning() && getFinishAnnouncedDate() == null;
    }

    public boolean isFinishable() {
        return hasCurrentUserRoleEnum(RoleEnum.REVIEWMANAGER) && isRunning();
    }

    public boolean isOrderCreatable() {
        return isRunning() && (hasCurrentUserRoleEnum(getDefaultRequiredRole()) || isMember());
    }

    public boolean isPrivateAnnouncable() {
        return hasCurrentUserRoleEnum(RoleEnum.REVIEWMANAGER) && isFinished() && getPrivateAnnouncedDate() == null;
    }

    public boolean isPrivatizable() {
        return hasCurrentUserRoleEnum(RoleEnum.REVIEWMANAGER) && isFinished();
    }

    public boolean isPublishGrantable() {
        return hasCurrentUserRoleEnum(RoleEnum.REVIEWMANAGER) && isPrivate() && getPublishGrantedDate() == null;
    }

    public boolean isPublishable() {
        return (hasCurrentUserRoleEnum(RoleEnum.REVIEWMANAGER) || isMember()) && isPrivate() && getPublishGrantedDate() != null;
    }

    @Override
    public boolean isReadable() {
        return isPublished() || hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER) || isMember();
    }

    @Override
    public boolean isReadableExtensionReports() {
        return isAccepted() && (hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.REVIEWER) || isMember());
    }

    public boolean isReadableProjectInternals() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.REVIEWER) || isMember();
    }

    private boolean isReminderPast() {
        return reminderDate != null && getReminderDate().plusDays(1).isBefore(LocalDate.now());
    }

    public boolean isReminderResettable() {
        return isReminderPast() && hasCurrentUserRoleEnum(RoleEnum.REVIEWMANAGER);
    }

    @Override
    public boolean isRenderHasBookingsWithoutSAPNumberHint() {
        return isAcceptedButNotPublished() && hasBookingsWithoutSAPNumber();
    }

    @Override
    public boolean isRenderedAddWorkflowButton() {
        return hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER) && isAcceptedButNotPrivateOrPublished();
    }

    public boolean isRenderedFeedbackButton() {
        return isAcceptedButNotPublished() && super.isRenderedFeedbackButton();
    }

    public boolean isRenderedPublishAnnouncedDate() {
        return LocalDate.parse("2015-03-11", Constants.DATE_FORMATTER).atStartOfDay().isAfter(getPrivateAnnouncedDate());
    }

    public boolean isRequesterUpdatable() {
        return hasCurrentUserRoleEnum(RoleEnum.ADMIN) || hasCurrentUserRoleEnum(getDefaultRequiredRole()) && isPendingOrReview() || isManager(getCurrentUser()) && isPendingOrReview();
    }

    public boolean isReviewable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) && isPendingOrReview() || hasCurrentUserRoleEnum(RoleEnum.REVIEWER) && isReview();
    }

    public boolean isRunnable() {
        return hasCurrentUserRoleEnum(RoleEnum.REVIEWMANAGER) && (isFinished() || isPrivate());
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(RoleEnum.ADMIN) || hasCurrentUserRoleEnum(getDefaultRequiredRole()) && !isPublished() || isManager(getCurrentUser()) && !isPublished() && !isPrivate();
    }

    public void setComments(Set<ProjectComment> comments) {
        this.comments = comments;
    }

    public void setDescription(Attachment description) {
        this.description = description;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setEndDateChanged(boolean endDateChanged) {
        this.endDateChanged = endDateChanged;
    }

    public void setExtensionReport(int year, Attachment report) {
        if (year == 1) {
            setExtensionReport1(report);
        } else if (year == 2) {
            setExtensionReport2(report);
        } else if (year == 3) {
            setExtensionReport3(report);
        }
    }

    public void setExtensionReport1(Attachment extensionReport1) {
        this.extensionReport1 = extensionReport1;
    }

    private void setExtensionReport1Approved(boolean extensionReport1Approved) {
        this.extensionReport1Approved = extensionReport1Approved;
    }

    public void setExtensionReport1ReminderSent(boolean extensionReport1ReminderSent) {
        this.extensionReport1ReminderSent = extensionReport1ReminderSent;
    }

    public void setExtensionReport2(Attachment extensionReport2) {
        this.extensionReport2 = extensionReport2;
    }

    private void setExtensionReport2Approved(boolean extensionReport2Approved) {
        this.extensionReport2Approved = extensionReport2Approved;
    }

    public void setExtensionReport2ReminderSent(boolean extensionReport2ReminderSent) {
        this.extensionReport2ReminderSent = extensionReport2ReminderSent;
    }

    public void setExtensionReport3(Attachment extensionReport3) {
        this.extensionReport3 = extensionReport3;
    }

    private void setExtensionReport3Approved(boolean extensionReport3Approved) {
        this.extensionReport3Approved = extensionReport3Approved;
    }

    public void setExtensionReport3ReminderSent(boolean extensionReport3ReminderSent) {
        this.extensionReport3ReminderSent = extensionReport3ReminderSent;
    }

    public void setExtensionReportApproved(int year, boolean approved) {
        if (year == 1) {
            setExtensionReport1Approved(approved);
        } else if (year == 2) {
            setExtensionReport2Approved(approved);
        } else if (year == 3) {
            setExtensionReport3Approved(approved);
        }
    }

    private void setFinalDecisionDate(LocalDate finalDecisionDate) {
        this.finalDecisionDate = finalDecisionDate;
    }

    public void setFinishAnnouncedDate(LocalDateTime finishAnnouncedDate) {
        this.finishAnnouncedDate = finishAnnouncedDate;
    }

    public void setFinishDate(LocalDate finishDate) {
        this.finishDate = finishDate;
    }

    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }

    public void setPrivateAnnouncedDate(LocalDateTime privateAnnouncedDate) {
        this.privateAnnouncedDate = privateAnnouncedDate;
    }

    public void setPrivateDate(LocalDateTime privateDate) {
        this.privateDate = privateDate;
    }

    public void setPublishDate(LocalDateTime publishDate) {
        this.publishDate = publishDate;
    }

    public void setPublishGrantedDate(LocalDateTime publishGrantedDate) {
        this.publishGrantedDate = publishGrantedDate;
    }

    public void setReminder(String comment) {
        setReminderComment(comment);
        setReminderDate(LocalDate.now());
    }

    public void setReminderComment(String reminderComment) {
        this.reminderComment = StringHelper.formatText(reminderComment);
        if (reminderComment != null && !reminderComment.isEmpty() && getReminderDate() == null) {
            setReminderDate(LocalDate.now());
        }
    }

    public void setReminderDate(LocalDate reminderDate) {
        this.reminderDate = reminderDate;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    @Override
    public void setStatus(StatusEnum status) {
        if (!getStatus().equals(status) || getStates().isEmpty()) {
            setCreateAndAddStatus(status);
            if (StatusEnum.RUNNING.equals(status)) {
                // Clear the finish date in case the container is rolled back.
                setFinishDate(null);
            }
        }
    }

    public boolean submit() {
        boolean ret = false;
        if (getStatus() == null) {
            setCreateAndAddStatus(StatusEnum.PENDING);
            createEntityLog(LogActionEnum.SUBMIT);
            ret = true;
        }
        return ret;
    }

    public boolean validateEndDate(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        final LocalDate aEndDate = (LocalDate) value;
        final boolean validated = DateUtils.validateDateRange(getStartDate(), aEndDate);
        if (validated && aEndDate != null && !aEndDate.equals(getEndDate())) {
            setEndDateChanged(true);
        }
        return validated;
    }

    public boolean validateStartDate(FacesContext facesContext, UIComponent uiComponent, Object value) throws BfabricValidatorException {
        return DateUtils.validateDateRange((LocalDate) value, getEndDate());
    }
}
