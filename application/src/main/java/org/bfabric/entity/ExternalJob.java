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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
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
import org.bfabric.enums.ExternalJobClientClassEnum;
import org.bfabric.enums.LogActionEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.util.StringHelper;
import org.bfabric.util.UriHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
@NamedQuery(name = "ExternalJob.findByClient", query = "SELECT a FROM ExternalJob a WHERE a.clientEntityClassName = :clientEntityClassName and a.clientEntityId = :clientEntityId ORDER BY a.created DESC")
@NamedQuery(name = "ExternalJob.findByClientAndExecutableContext", query = "SELECT a FROM ExternalJob a WHERE a.clientEntityClassName = :clientEntityClassName and a.clientEntityId = :clientEntityId AND a.executable.context = :context ORDER BY a.created DESC")
@NamedQuery(name = "ExternalJob.findDistinctActions", query = "SELECT DISTINCT a.action FROM ExternalJob a WHERE a.action IS NOT NULL ORDER BY a.action")
public class ExternalJob extends AbstractBaseEntity implements ShowScreen, NotEntityLoggable {

    private static final long serialVersionUID = 1;

    @Size(max = 64)
    @NotBlank
    @XmlElement
    protected String action;

    @Size(max = 64)
    @NotNull
    @XmlElement
    protected String clientEntityClassName;

    @NotNull
    @XmlElement
    protected long clientEntityId;

    @Column(columnDefinition = "TEXT")
    protected String log;

    @Transient
    protected StatusEnum oldStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executableid")
    @NotNull
    @XmlIDREF
    private Executable executable;

    @OneToMany(mappedBy = "externalJob", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<ExternalJobStatus> states = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private StatusEnum status = StatusEnum.NEW;

    @NotNull
    private LocalDateTime statusModified;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statusmodifiedbyid")
    @NotNull
    private User statusModifiedBy;

    public ExternalJob() {
        setStatus(StatusEnum.NEW);
    }

    public ExternalJob(StatusEnum status) {
        setStatusAndAddState(status);
    }

    public ExternalJob(ExternalJobClientClassEnum externalJobClientClassEnum, AbstractEntity clientEntity, LogActionEnum action, Executable executable) {
        this(StatusEnum.NEW);
        if (externalJobClientClassEnum != null) {
            setClientEntityClassName(externalJobClientClassEnum.getClientClassName());
        }
        if (clientEntity != null) {
            setClientEntityId(clientEntity.getId());
        }
        if (action != null) {
            setAction(action.toString());
        }
        setExecutable(executable);
    }

    public ExternalJob(User user, Executable executable) {
        this(ExternalJobClientClassEnum.USER, user, LogActionEnum.UPDATE, executable);
    }

    public ExternalJob(Container container, Executable executable) {
        this(ExternalJobClientClassEnum.CONTAINER, container, LogActionEnum.UPDATE, executable);
    }

    public ExternalJob(Workunit workunit, LogActionEnum action, Executable executable) {
        this(ExternalJobClientClassEnum.WORKUNIT, workunit, action, executable);
    }

    private static String getLogFooter() {
        return "---" + StringHelper.lineSeparator;
    }

    private static String getLogHeader(String headerMessage) {
        return "[" + LocalDateTime.now() + "] " + headerMessage + "." + StringHelper.lineSeparator;
    }

    public void addState() {
        if (getStates().isEmpty() || isStatusChanged()) {
            getStates().add(new ExternalJobStatus(this, getStatus()));
            setStatusModified();
        }
    }

    public void appendReadLogInfo() {
        String updatedLog = getLog() != null ? getLog() : Constants.EMPTY_STRING;
        updatedLog += getLogHeader("fetched by " + getCurrentUsername());
        updatedLog += getLogFooter();
        setLog(updatedLog);
    }

    public void appendUpdateLogInfo(String logText) {
        if (StringHelper.isNotEmpty(logText)) {
            String updatedLog = getLog() != null ? getLog() : Constants.EMPTY_STRING;
            updatedLog += getLogHeader("updated by " + getCurrentUsername());
            updatedLog += logText + StringHelper.lineSeparator;
            updatedLog += getLogFooter();
            setLog(updatedLog);
        }
    }

    public Set<Mail> changeStatus(StatusEnum statusEnum) {
        Set<Mail> mails = new HashSet<>();
        setStatusAndAddState(statusEnum);
        return mails;
    }

    public String getAction() {
        return action;
    }

    public String getClientEntityClassName() {
        return clientEntityClassName;
    }

    public String getClientEntityClassNameLower() {
        return getClientEntityClassName() != null ? getClientEntityClassName().toLowerCase() : null;
    }

    public long getClientEntityId() {
        return clientEntityId;
    }

    public String getClientEntityLink() {
        return UriHelper.getUrlShowScreen(getClientEntityClassNameLower());
    }

    public Container getContainer() {
        return clientEntityClassName != null && clientEntityClassName.equals(ExternalJobClientClassEnum.CONTAINER.getClientClassName()) ? getEntityService()
            .find(Container.class, clientEntityId) :
            null;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (StringHelper.isNotEmpty(getClientEntityClassName())) {
            addEntityInfoItem(summary, "entityClass", getClientEntityClassName());
        }
        addEntityInfoItem(summary, "entityId", getClientEntityId());
        if (StringHelper.isNotEmpty(getAction())) {
            addEntityInfoItem(summary, "action", getAction());
        }
        if (getExecutable() != null) {
            addEntityInfoItem(summary, "executable", getExecutable().getName());
            addEntityInfoItem(summary, "executableContext", getExecutable().getExecutableContext());
        }
        if (getStatus() != null) {
            addEntityInfoItem(summary, "status", getStatus());
        }
        return summary.toString();
    }

    public Executable getExecutable() {
        return executable;
    }

    public String getLog() {
        return log;
    }

    public List<StatusEnum> getNextStates() {
        List<StatusEnum> nextStates = new ArrayList<>();
        StatusEnum statusEnum = getStatus();
        switch (statusEnum) {
        case NEW:
            nextStates.add(StatusEnum.SUBMITTED);
            nextStates.add(StatusEnum.RUNNING);
            break;
        case RUNNING:
            nextStates.add(StatusEnum.FAILED);
            nextStates.add(StatusEnum.DONE);
            break;
        case FAILED:
            nextStates.add(StatusEnum.RESUBMITTED);
            nextStates.add(StatusEnum.RUNNING);
            break;
        case RESUBMITTED:
        case SUBMITTED:
            nextStates.add(StatusEnum.RUNNING);
            nextStates.add(StatusEnum.FAILED);
            break;
        case DONE:
        default:
            break;
        }

        return nextStates;
    }

    public StatusEnum getOldStatus() {
        return oldStatus;
    }

    public List<ExternalJobStatus> getStates() {
        return states;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public User getUser() {
        return clientEntityClassName != null && clientEntityClassName.equals(User.class.getSimpleName()) ? getEntityService().find(User.class, clientEntityId) : null;
    }

    public Workunit getWorkunit() {
        return clientEntityClassName != null && clientEntityClassName.equals(ExternalJobClientClassEnum.WORKUNIT.getClientClassName()) ? getEntityService().find(Workunit.class, clientEntityId) :
            null;
    }

    @Override
    public boolean isDeletable() {
        return false;
    }

    public boolean isReadable() {
        return hasCurrentUserRoleEnum(RoleEnum.EXTERNALJOBREADER);
    }

    public boolean isRenderedRefresh() {
        return getStatus() != StatusEnum.DONE && getStatus() != StatusEnum.FAILED && getStatus() != StatusEnum.FINISHED;
    }

    public boolean isRenderedSendEmailCheckbox() {
        // return getNextStates() != null && getNextStates().contains(StatusEnum.NEW);
        return false;
    }

    public boolean isRollbackable() {
        return false;
    }

    public boolean isStatusChanged() {
        return getStatus() != null && !getStatus().equals(getOldStatus()) || getOldStatus() != null && !getOldStatus().equals(getStatus());
    }

    public void setAction(String action) {
        this.action = StringHelper.format(action);
    }

    public void setClientEntityClassName(ExternalJobClientClassEnum externalJobClientClassEnum) {
        if (externalJobClientClassEnum != null) {
            setClientEntityClassName(externalJobClientClassEnum.getClientClassName());
        }
    }

    public void setClientEntityClassName(String clientEntityClassName) {
        this.clientEntityClassName = clientEntityClassName;
    }

    public void setClientEntityId(long clientEntityId) {
        this.clientEntityId = clientEntityId;
    }

    public void setExecutable(Executable executable) {
        this.executable = executable;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public void setOldStatus(StatusEnum oldStatus) {
        this.oldStatus = oldStatus;
    }

    public void setStates(List<ExternalJobStatus> states) {
        this.states = states;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public void setStatusAndAddState(StatusEnum status) {
        setOldStatus(getStatus());
        setStatus(status);
        addState();
    }

    public void setStatusModified(LocalDateTime statusModified) {
        this.statusModified = statusModified;
    }

    public void setStatusModified() {
        statusModified = LocalDateTime.now();
        statusModifiedBy = getCurrentUser();
    }

    public void setStatusModifiedBy(User statusModifiedBy) {
        this.statusModifiedBy = statusModifiedBy;
    }
}
