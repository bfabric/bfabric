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
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.faces.event.ValueChangeEvent;
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
import javax.security.auth.message.AuthException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.HasParameters;
import org.bfabric.entity.api.HasSupervisor;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.ExecutableContextEnum;
import org.bfabric.enums.ResourceStatusEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.forms.MFExecutable;
import org.bfabric.service.UserService;
import org.bfabric.util.BfabricUploadedFile;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveExecutable;
import org.bfabric.xml.JAXBMarshaller;
import org.bfabric.xml.entity.XMLExecutable;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;

@Entity
@XmlRootElement
@NamedQuery(name = "Executable.findAllNonWorkunitExecutables", query = "SELECT a from Executable a WHERE a.context <> :context order by a.id DESC")
public class Executable extends AbstractResource implements HasParameters, HasSupervisor, ShowScreen {

    private static final long serialVersionUID = 1;

    @Column(columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    protected boolean enabled = true;

    @OneToMany(mappedBy = "applicationExecutable")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Workunit> applicationWorkunits = new HashSet<>();

    @OneToMany(mappedBy = "executable")
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Application> applications = new HashSet<>();

    @Size(max = 32)
    @NotNull
    @XmlElement
    private String context;

    @OneToMany(mappedBy = "executable", cascade = CascadeType.REMOVE)
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ExternalJob> externalJobs = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "masterexecutableid")
    @XmlIDREF
    private Executable masterExecutable;

    @OneToMany(mappedBy = "executable", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("key")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Parameter> parameters = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predecessorexecutableid")
    @XmlIDREF
    private Executable predecessor;

    @Size(max = 1024)
    @XmlElement
    private String program;

    @OneToMany(mappedBy = "masterExecutable", cascade = CascadeType.ALL)
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Executable> slaveExecutables = new HashSet<>();

    @OneToMany(mappedBy = "executable", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private List<ExecutableStatus> states = new ArrayList<>();

    @NotNull
    private LocalDateTime statusModified;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statusmodifiedbyid")
    @NotNull
    private User statusModifiedBy;

    @OneToMany(mappedBy = "executable")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Storage> storages = new HashSet<>();

    @OneToMany(mappedBy = "submitterExecutable")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Workunit> submitterWorkunits = new HashSet<>();

    @OneToMany(mappedBy = "executable")
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Submitter> submitters = new HashSet<>();

    @OneToMany(mappedBy = "predecessor")
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Executable> successors = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisorid")
    @NotNull
    @XmlIDREF
    private User supervisor;

    @Transient
    private boolean supervisorChanged;

    @Column(columnDefinition = "boolean DEFAULT true")
    @NotNull
    @XmlElement
    private boolean valid = true;

    @Size(max = 32)
    @XmlElement
    private String version;

    @ManyToOne
    @JoinColumn(name = "workunitid")
    @XmlIDREF
    private Workunit workunit;

    @OneToMany(mappedBy = "wrapperCreatorExecutable")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Executable> workunitExecutables = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wrappercreatorexecutableid")
    @XmlIDREF
    private Executable wrapperCreatorExecutable;

    @OneToMany(mappedBy = "wrapperCreatorExecutable")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Workunit> wrapperCreatorWorkunits = new HashSet<>();

    @OneToMany(mappedBy = "executable")
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<WrapperCreator> wrapperCreators = new HashSet<>();

    public Executable() {
        setStatus(ResourceStatusEnum.AVAILABLE);
    }

    public void addState() {
        if (getStates().isEmpty() || isStatusChanged()) {
            getStates().add(new ExecutableStatus(this, StatusEnum.get(getStatus())));
            setStatusModified();
        }
    }

    public Set<Mail> changeStatus(ResourceStatusEnum statusEnum) {
        Set<Mail> mails = new HashSet<>();
        setStatusAndAddState(statusEnum);

        // Explicit mail sending.
        if (isSendMail()) {
            switch (statusEnum) {
            case PENDING:
            case AVAILABLE:
                // mails.add(createMail(MailTypeEnum.RUN_FINISHED));
                break;
            default:
                break;
            }
        }

        return mails;
    }

    public void clearAndSetParameters(Set<Parameter> newParameters) {
        Set<Parameter> currentParameters = new HashSet<>(getParameters());
        for (Parameter parameter : currentParameters) {
            if (!newParameters.contains(parameter)) {
                getParameters().remove(parameter);
            }
        }
        getParameters().addAll(newParameters);
    }

    @Override
    public Set<Parameter> cloneParameters(Set<Parameter> parameters) {
        Set<Parameter> clonedParameters = new HashSet<>();
        for (Parameter parameter : parameters) {
            Parameter clonedParameter = parameter.clonePartial(this);
            clonedParameters.add(clonedParameter);
        }
        return clonedParameters;
    }

    public String computeDefaultExecutableFileName() {
        return getId() + "_" + getName().replaceAll("[^A-Za-z0-9.\\-]", "_");
    }

    public void exportAndDownloadXML() {
        download("executable_" + getId() + ".xml", JAXBMarshaller.getXmlAsText(new XMLExecutable(true, this)));
    }

    public void fileUploadListener(FileUploadEvent event) throws RuntimeException {
        getFileUploadHelper().listenerSingleUpload(event);
        BfabricUploadedFile configurationFile = getFileUploadHelper().getSingleUploadedFile();
        if (configurationFile != null) {
            try {
                XMLRequestParameterSaveExecutable xmlRequestSaveExecutable = (XMLRequestParameterSaveExecutable) JAXBMarshaller.unmarshal(configurationFile.getFileContent(), XMLRequestParameterSaveExecutable.class);
                MFExecutable mfExecutable = new MFExecutable(this, xmlRequestSaveExecutable);
                mfExecutable.apply();
            } catch (Exception e) {
                getFileUploadHelper().clearAllUploadData();
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String getAbsolutePathFM() {
        return getProgram() != null ? getProgram() : super.getAbsolutePathFM();
    }

    public Set<Workunit> getApplicationWorkunits() {
        return applicationWorkunits;
    }

    public Set<Application> getApplications() {
        return applications;
    }

    public String getBase64() {
        String absolutePath = getAbsolutePathFM();
        if (absolutePath != null && !absolutePath.isEmpty()) {
            File executableFile = new File(absolutePath);
            if (executableFile.exists() && executableFile.canRead()) {
                return StringHelper.encodeBase64(executableFile);
            }
        }
        return null;
    }

    public String getContext() {
        return context;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.EXECUTABLEMANAGER;
    }

    @Override
    public DefaultStreamedContent getDefaultStreamedContent() throws IOException, AuthException {
        return super.getDefaultStreamedContent();
    }

    public List<User> getEmployeesIncludingSupervisor(String filterString) {
        return CDI.current().select(UserService.class).get().getEmployeesFilteredIncludingUser(filterString, getSupervisor());
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (StringHelper.isNotEmpty(getContext())) {
            addEntityInfoItem(summary, "context", getContext());
        }
        if (getSupervisor() != null) {
            addEntityInfoItem(summary, "supervisor", getSupervisor().getName());
        }
        addEntityInfoItem(summary, "enabled", isEnabled());
        addEntityInfoItem(summary, "valid", isValid());
        if (getParameters() != null && !getParameters().isEmpty()) {
            addEntityInfoItem(summary, "parameters", getParameters().size());
        }
        if (StringHelper.isNotEmpty(getPath())) {
            addEntityInfoItem(summary, "path", getPath());
        }
        addEntityInfoItem(summary, "size", getSize());
        if (StringHelper.isNotEmpty(getVersion())) {
            addEntityInfoItem(summary, "version", getVersion());
        }
        if (getPredecessor() != null) {
            addEntityInfoItem(summary, "predecessor", getPredecessor().getName());
        }
        return summary.toString();
    }

    public ExecutableContextEnum getExecutableContext() {
        return ExecutableContextEnum.valueOf(context);
    }

    public Set<ExternalJob> getExternalJobs() {
        return externalJobs;
    }

    public Executable getMasterExecutable() {
        return masterExecutable;
    }

    public String getNameWithEnabledMessage() {
        return getName() + (isEnabled() ? Constants.EMPTY_STRING : " -> not enabled anymore!");
    }

    public List<StatusEnum> getNextStates() {
        List<StatusEnum> nextStates = new ArrayList<>();
        StatusEnum statusEnum = StatusEnum.valueOf(getStatus().name());
        switch (statusEnum) {
        case PENDING:
            nextStates.add(StatusEnum.AVAILABLE);
            break;
        case AVAILABLE:
            nextStates.add(StatusEnum.PENDING);
            break;
        default:
            break;
        }
        return nextStates;
    }

    @Override
    public Set<Parameter> getParameters() {
        return parameters;
    }

    @SuppressWarnings("unused")
    public Map<String, String> getParametersKeyValueMap() {
        Map<String, String> parametersKeyValueMap = new HashMap<>();
        for (Parameter parameter : getParameters()) {
            parametersKeyValueMap.put(parameter.getKey(), parameter.getValue());
        }
        return parametersKeyValueMap;
    }

    public String getPath() {
        return StringHelper.isNotEmpty(getProgram()) ? getProgram() : getRelativePath();
    }

    public Executable getPredecessor() {
        return predecessor;
    }

    public String getProgram() {
        return program;
    }

    @Override
    public String getRelativeRepositoryPath() {
        return getWorkunit() != null ? getWorkunit().getRelativeRepositoryPath() : super.getRelativeRepositoryPath();
    }

    public Set<Executable> getSlaveExecutables() {
        return slaveExecutables;
    }

    public AbstractStatus getState() {
        return getStates().isEmpty() ? null : getStates().get(getStates().size() - 1);
    }

    public List<ExecutableStatus> getStates() {
        return states;
    }

    public LocalDateTime getStatusModified() {
        return statusModified;
    }

    public User getStatusModifiedBy() {
        return statusModifiedBy;
    }

    public Set<Storage> getStorages() {
        return storages;
    }

    public Set<Workunit> getSubmitterWorkunits() {
        return submitterWorkunits;
    }

    public Set<Submitter> getSubmitters() {
        return submitters;
    }

    public Set<Executable> getSuccessors() {
        return successors;
    }

    @Override
    public User getSupervisor() {
        return supervisor;
    }

    public String getVersion() {
        return version;
    }

    public Workunit getWorkunit() {
        return workunit;
    }

    public Set<Executable> getWorkunitExecutables() {
        return workunitExecutables;
    }

    public int getWorkunitSize() {
        return getApplicationWorkunits().size() + getSubmitterWorkunits().size() + getWrapperCreatorWorkunits().size() + (getWorkunit() != null ? 1 : 0);
    }

    public Executable getWrapperCreatorExecutable() {
        return wrapperCreatorExecutable;
    }

    public Set<Workunit> getWrapperCreatorWorkunits() {
        return wrapperCreatorWorkunits;
    }

    public Set<WrapperCreator> getWrapperCreators() {
        return wrapperCreators;
    }

    public boolean hasNoDependents() {
        return getSuccessors().isEmpty() && getApplications().isEmpty() && getWrapperCreators().isEmpty() && getSubmitters().isEmpty() && getStorages().isEmpty() && getExternalJobs().isEmpty() && getSlaveExecutables().isEmpty() && getWorkunitExecutables().isEmpty() && getApplicationWorkunits().isEmpty() && getWrapperCreatorWorkunits().isEmpty() && getSubmitterWorkunits().isEmpty();
    }

    @Override
    public boolean isAdminOrSupervisor() {
        return hasCurrentUserRoleEnum(RoleEnum.ADMIN) || getSupervisor().isIdentityUser();
    }

    public boolean isApplicationExecutable() {
        return ExecutableContextEnum.APPLICATION.equals(getExecutableContext()) && hasCurrentUserRoleEnum(new Application().getDefaultRequiredRole());
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public boolean isAvailable() {
        return super.isAvailable();
    }

    public boolean isContextMaster() {
        return ExecutableContextEnum.MASTER.equals(getExecutableContext());
    }

    public boolean isContextWorkunit() {
        return ExecutableContextEnum.WORKUNIT.equals(getExecutableContext());
    }

    public boolean isContextWrapperCreator() {
        return ExecutableContextEnum.WRAPPERCREATOR.equals(getExecutableContext());
    }

    @Override
    public boolean isDeletable() {
        return getId() > 0 && (hasCurrentUserRoleEnum(RoleEnum.ADMIN) || hasCurrentUserRoleEnum(RoleEnum.MASTEREXECUTABLEMANAGER) || isIdentitySupervisor()) && hasNoDependents();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public boolean isExecutableFileDownloadable() {
        if (getRelativePath() == null) {
            return false;
        }

        File executableFile = new File(getAbsolutePathFM());
        if (!executableFile.exists() || !executableFile.canRead()) {
            return false;
        }

        boolean ret = true;
        switch (getExecutableContext()) {
        case MASTER:
            ret = hasCurrentUserRoleEnum(RoleEnum.ADMIN) || hasCurrentUserRoleEnum(RoleEnum.MASTEREXECUTABLEMANAGER) && isIdentitySupervisor();
            break;
        case STORAGE:
        case SUBMITTER:
        case WRAPPERCREATOR:
            ret = hasCurrentUserRoleEnum(RoleEnum.ADMIN) || isIdentitySupervisor();
            break;
        case APPLICATION:
        case WORKUNIT:
        default:
            break;
        }

        return ret;
    }

    private boolean isIdentitySupervisor() {
        return getSupervisor() != null && getSupervisor().isIdentityUser();
    }

    public boolean isMasterExecutableEditable() {
        boolean ret = false;

        if (isAdminOrSupervisor()) {
            switch (getExecutableContext()) {
            case STORAGE:
            case SUBMITTER:
            case WRAPPERCREATOR:
                ret = true;
                break;
            case APPLICATION:
            case MASTER:
            case WORKUNIT:
            default:
                break;
            }
        }

        return ret;
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.APPLICATIONMANAGER);
    }

    public boolean isRenderedSendEmailCheckbox() {
        return false; // getNextStates() != null;
    }

    public boolean isRollbackable() {
        return getStates().size() > 1;
    }

    public boolean isSaveButtonVisible() {
        return isManaged() || getFileUploadHelper().getSingleUploadedFile() != null;
    }

    @Override
    public boolean isStreamedContent() {
        return super.isStreamedContent();
    }

    public boolean isSupervisorChanged() {
        return supervisorChanged;
    }

    @Override
    public boolean isSupervisorEditable() {
        return !isManaged() || isAdminOrSupervisor();
    }

    @Override
    public boolean isSupervisorValid() {
        return !isEnabled() || getSupervisor() != null && getSupervisor().hasRoleImplicit(getDefaultRequiredRole());
    }

    @Override
    public boolean isUpdatable() {
        switch (getExecutableContext()) {
        case MASTER:
            return hasCurrentUserRoleEnum(RoleEnum.MASTEREXECUTABLEMANAGER);
        case WORKUNIT:
            return hasCurrentUserRoleEnum(RoleEnum.ADMIN);
        case SUBMITTER:
        case WRAPPERCREATOR:
        case STORAGE:
        case APPLICATION:
        default:
            return hasCurrentUserRoleEnum(getDefaultRequiredRole());
        }
    }

    public boolean isValid() {
        return valid;
    }

    public void rollbackStatus() {
        if (isRollbackable() && getStates().size() > 1) {
            getStates().remove(getStates().size() - 1);
            setStatus(ResourceStatusEnum.valueOf(getStates().get(getStates().size() - 1).getStatusEnum().name()));
        }
    }

    public void setContext(ExecutableContextEnum executableContext) {
        if (executableContext != null) {
            context = executableContext.name();
        } else {
            context = null;
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setMasterExecutable(Executable masterExecutable) {
        this.masterExecutable = masterExecutable;
    }

    public void setParameters(Set<Parameter> parameters) {
        getParameters().clear();
        if (parameters != null && !parameters.isEmpty()) {
            getParameters().addAll(cloneParameters(parameters));
        }
    }

    @Override
    public void setPending() {
        setStatusAndAddState(ResourceStatusEnum.PENDING);
    }

    public void setPredecessor(Executable predecessor) {
        this.predecessor = predecessor;
    }

    public void setProgram(String program) {
        this.program = StringHelper.format(program);
    }

    public void setStatusAndAddState(ResourceStatusEnum status) {
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

    public void setSupervisor(User supervisor) {
        this.supervisor = supervisor;
    }

    public void setSupervisorChanged(boolean supervisorChanged) {
        this.supervisorChanged = supervisorChanged;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setVersion(String version) {
        this.version = StringHelper.format(version);
    }

    public void setWorkunit(Workunit workunit) {
        this.workunit = workunit;
    }

    public void setWrapperCreatorExecutable(Executable wrapperCreatorExecutable) {
        this.wrapperCreatorExecutable = wrapperCreatorExecutable;
    }

    public void supervisorChangedListener(ValueChangeEvent event) {
        setSupervisorChanged(!(getSupervisor() == null && event.getNewValue() == null || getSupervisor() != null && getSupervisor().equals(event.getNewValue())));
    }
}