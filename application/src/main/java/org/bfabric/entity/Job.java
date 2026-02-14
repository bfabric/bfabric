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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.LogActionEnum;
import org.bfabric.enums.LogStatusEnum;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.service.UserService;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.StringHelper;
import org.bfabric.util.UriHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
@NamedQuery(name = "Job.findUnarchiveByParent", query = "SELECT a FROM Job a WHERE a.parentClassName = :parentClassName and a.parentId = :parentId and a.action = org.bfabric.enums.LogActionEnum.UNARCHIVE ORDER BY id DESC")
@NamedQuery(name = "Job.findUnarchiveByParentAndStatusNew", query = "SELECT a FROM Job a WHERE a.action = org.bfabric.enums.LogActionEnum.UNARCHIVE and a.status = org.bfabric.enums.StatusEnum.NEW ORDER BY id DESC")
public class Job extends AbstractParentDependentBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @Column(columnDefinition = "TEXT")
    @XmlElement
    protected String url;

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private LogActionEnum action;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<JobLog> jobLogs = new ArrayList<>();

    @Transient
    private StatusEnum oldStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requesterid")
    @XmlIDREF
    private User requester;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<JobStatus> states = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private StatusEnum status;

    @ManyToMany
    @JoinTable(name = "workunitjob", joinColumns = @JoinColumn(name = "jobid"), inverseJoinColumns = @JoinColumn(name = "workunitid"))
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Workunit> workunits = new HashSet<>();

    public Job() {
        setStatus(StatusEnum.NEW);
    }

    public Job(LogActionEnum logActionEnum, User user, AbstractEntity parent) {
        this();
        setAction(logActionEnum);
        setRequester(user);
        setParent(parent);
    }

    public void addState() {
        if (getStates().isEmpty() || isStatusChanged()) {
            getStates().add(new JobStatus(this, getStatus()));
        }
    }

    public void appendLog(String logText) {
        if (StringHelper.isNotEmpty(logText)) {
            getJobLogs().add(new JobLog(this, logText, LogActionEnum.UPDATE, LogStatusEnum.DONE));
        }
    }

    public Set<Mail> changeStatus(StatusEnum statusEnum) {
        Set<Mail> mails = new HashSet<>();
        setStatusAndAddState(statusEnum);
        return mails;
    }

    public Mail createMail(MailTypeEnum mailTypeEnum) {
        Mail mail = new Mail();
        mail.setParent(this);
        mail.setType(mailTypeEnum, getParent().getClassLabel() + " " + getParent().getId());
        return mail;
    }

    public Mail createMailToArchiveManager(MailTypeEnum mailTypeEnum) {
        Mail mail = createMail(mailTypeEnum);
        mail.setRecipients(CDI.current().select(UserService.class).get().getArchiveManager());
        return mail;
    }

    public Mail createMailUnarchive() {
        return createMailToArchiveManager(MailTypeEnum.UNARCHIVE);
    }

    public Mail createMailUnarchiveCanceled() {
        return createMailToArchiveManager(MailTypeEnum.UNARCHIVE_CANCELED);
    }

    public Mail createMailUnarchiveFailed() {
        return createMailToArchiveManager(MailTypeEnum.UNARCHIVE_FAILED);
    }

    public Mail createMailUnarchived() {
        Mail mail = createMail(MailTypeEnum.UNARCHIVED);
        mail.setRecipient(getRequester());
        return mail;
    }

    public LogActionEnum getAction() {
        return action;
    }

    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.FEEDER;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getRequester() != null) {
            addEntityInfoItem(summary, "requester", getRequester().getName());
        }
        if (getAction() != null) {
            addEntityInfoItem(summary, "action", getAction().getLabel());
        }
        if (getStatus() != null) {
            addEntityInfoItem(summary, "status", getStatus().getLabel());
        }
        if (getLog() != null) {
            addEntityInfoItem(summary, "log", getLog());
        }
        return summary.toString();
    }

    public String getJobLogAsString(JobLog jobLog) {
        if (jobLog != null) {
            return "[" + jobLog.getCreatedByFull() + ", " + jobLog.getAction() + ", " + jobLog.getStatus() + "] " + StringHelper.lineSeparator + jobLog.getLog();
        }
        return Constants.EMPTY_STRING;
    }

    public List<JobLog> getJobLogs() {
        return jobLogs;
    }

    public String getLog() {
        return jobLogs.stream().map(this::getJobLogAsString).reduce((log1, log2) -> log1 + StringHelper.lineSeparator + log2)
            .orElse(Constants.EMPTY_STRING);
    }

    public List<StatusEnum> getNextStates() {
        List<StatusEnum> nextStates = new ArrayList<>();
        StatusEnum statusEnum = getStatus();
        switch (statusEnum) {
        case NEW:
        case RUNNING:
            nextStates.add(StatusEnum.DONE);
            nextStates.add(StatusEnum.FAILED);
            nextStates.add(StatusEnum.CANCELED);
            break;
        case DONE:
        case FAILED:
        case CANCELED:
        default:
            break;
        }
        return nextStates;
    }

    public StatusEnum getOldStatus() {
        return oldStatus;
    }

    public User getRequester() {
        return requester;
    }

    public List<JobStatus> getStates() {
        return states;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    public Set<Workunit> getWorkunits() {
        return workunits;
    }

    public List<Workunit> getWorkunitsAsList() {
        return CollectionHelper.asList(workunits);
    }

    @Override
    public boolean isCreatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.EMPLOYEE);
    }

    @Override
    public boolean isDeletable() {
        return hasCurrentUserRoleEnum(RoleEnum.ADMIN);
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.EMPLOYEE);
    }

    public boolean isRollbackable() {
        return getStates().size() > 1;
    }

    public boolean isStatusChanged() {
        return getStatus() != null && !getStatus().equals(getOldStatus()) || getOldStatus() != null && !getOldStatus().equals(getStatus());
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public boolean isValidUrl() {
        return UriHelper.isValidUrl(getUrl());
    }

    public void rollbackStatus() {
        if (isRollbackable()) {
            getStates().remove(getStates().size() - 1);
            setStatus(getStates().get(getStates().size() - 1).getStatusEnum());
        }
    }

    public void setAction(LogActionEnum action) {
        this.action = action;
    }

    public void setOldStatus(StatusEnum oldStatus) {
        this.oldStatus = oldStatus;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public void setStatusAndAddState(StatusEnum status) {
        setOldStatus(getStatus());
        setStatus(status);
        addState();
    }

    public void setUrl(String url) {
        this.url = StringHelper.format(url);
    }

    public void setWorkunits(Set<Workunit> workunits) {
        this.workunits = workunits;
    }

    public void setWorkunitsAsList(List<Workunit> workunits) {
        this.workunits = (Set<Workunit>) CollectionHelper.asSet(workunits);
    }
}