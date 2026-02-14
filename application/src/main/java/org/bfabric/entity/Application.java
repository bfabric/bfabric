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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.faces.event.ValueChangeEvent;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
import org.bfabric.entity.api.TechnologiesDependent;
import org.bfabric.enums.ExecutableContextEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.indexer.IndexHelper;
import org.bfabric.service.ImportResourceService;
import org.bfabric.service.JobService;
import org.bfabric.util.StringHelper;
import org.bfabric.util.UriHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
@NamedQuery(name = "Application.findAll", query = "SELECT a FROM Application a ORDER BY a.id DESC")
@NamedQuery(name = "Application.findByStorage", query = "SELECT a FROM Application a WHERE a.storage = :storage ORDER BY a.name")
@NamedQuery(name = "Application.findAllPotentialSucceeding", query = "SELECT a FROM Application a WHERE a.applicationType.name <> 'import' ORDER BY a.id DESC")
@NamedQuery(name = "Application.findByValidityCheckRequired", query = "SELECT a FROM Application a WHERE a.webUrl is not null and a.hidden = false and a.enabled = true and (a.validityChecked is null or a.validityChecked <= :validityChecked) order by a.id")
public class Application extends AbstractAssociatedToExecutableEntity implements TechnologiesDependent {

    private static final long serialVersionUID = 1;

    protected LocalDateTime validityChecked;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean annotationRequired = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicationCategoryId")
    @XmlIDREF
    private ApplicationCategory applicationCategory;

    @OneToMany(mappedBy = "application", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE })
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ApplicationTestLog> applicationTestLog = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicationTypeId")
    @NotNull
    @XmlIDREF
    private ApplicationType applicationType;

    @XmlElement
    private Boolean archiving;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "datasetTemplateid")
    @XmlIDREF
    private DatasetTemplate datasetTemplate;

    @Size(max = 64)
    @XmlElement
    private String entityClassName;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean forEmployeesOnly = false;

    @Transient
    private Boolean hasNonExpiredImportResources;

    @Size(max = 256)
    @XmlElement
    private String help;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean hidden = false;

    @OneToMany(mappedBy = "application", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE })
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ImportResource> importResources = new HashSet<>();

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean importResourcesRequired = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumentid")
    @XmlIDREF
    private Instrument instrument;

    @Transient
    private Job job;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean notifyApplicationSupervisor;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean notifyContainerMember;

    @Size(max = 256)
    @XmlElement
    private String outputFileFormat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pageflowId")
    @XmlIDREF
    private Pageflow pageflow;

    @OneToMany(mappedBy = "application", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE }, orphanRemoval = true)
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Parameter> parameters = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "applicationinputfilter", joinColumns = @JoinColumn(name = "applicationid"), inverseJoinColumns = @JoinColumn(name = "applicationinputid"))
    @OrderBy(value = "id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "precedingApplication")
    private Set<Application> precedingApplications = new HashSet<>();

    @OneToMany(mappedBy = "succeedingWebApp")
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Application> precedingWebApps = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predecessorapplicationid")
    @XmlIDREF
    private Application predecessor;

    @Size(max = 256)
    @XmlElement
    private String resourceRelativePathFilter;

    @Transient
    private Boolean runnable = null;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storageid")
    @XmlIDREF
    private Storage storage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitterid")
    @XmlIDREF
    private Submitter submitter;

    @ManyToMany
    @JoinTable(name = "applicationinputfilter", joinColumns = @JoinColumn(name = "applicationinputid"), inverseJoinColumns = @JoinColumn(name = "applicationid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "succeedingApplication")
    private Set<Application> succeedingApplications = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "succeedingWebAppId")
    @XmlIDREF
    private Application succeedingWebApp;

    @OneToMany(mappedBy = "predecessor")
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Application> successors = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "applicationtechnology", joinColumns = @JoinColumn(name = "applicationid"), inverseJoinColumns = @JoinColumn(name = "technologyid"))
    @OrderBy("name")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Technology> technologies = new HashSet<>();

    @XmlElement(name = "technologies")
    private String technologiesAsString;

    @Size(max = 1024)
    @XmlElement
    @NotBlank
    private String webUrl;

    @OneToMany(mappedBy = "application")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Workunit> workunits = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wrappercreatorid")
    @XmlIDREF
    private WrapperCreator wrapperCreator;

    public Application() {
    }

    public void checkValidity() {
        if (isWebApp()) {
            setValidityChecked(LocalDateTime.now());
            if (isValidUrl()) {
                Boolean existsWebUrl = UriHelper.existsUrl(getWebUrl());
                setValid(existsWebUrl == null || existsWebUrl);
            } else {
                setValid(false);
            }
        }
    }

    @Override
    public Application clone() throws CloneNotSupportedException {
        Application clone = (Application) super.clone();

        clone.importResources = new HashSet<>();
        clone.workunits = new HashSet<>();
        clone.successors = new HashSet<>();
        clone.succeedingApplications = new HashSet<>();

        clone.precedingApplications = new HashSet<>();
        if (!getPrecedingApplications().isEmpty()) {
            clone.precedingApplications.addAll(getPrecedingApplications());
        }

        clone.parameters = new HashSet<>();
        for (Parameter parameter : getParameters()) {
            clone.parameters.add(parameter.clone());
        }

        return clone;
    }

    public void createWebAppJob(AbstractEntity clientEntity, User user) {
        job = CDI.current().select(JobService.class).get().createWebAppJob(this, clientEntity, user);
        if (clientEntity != null) {
            createExecutionInvokedEntityLog(clientEntity);
        }
        getSessionManager().redirectInNewTab(job != null ? job.getUrl() : webUrl);
    }

    public void datasetTemplateChanged(ValueChangeEvent event) {
        setDatasetTemplate((DatasetTemplate) event.getNewValue());
    }

    @Override
    public void fixDependencies() {
        super.fixDependencies();
        resetFields();
    }

    public ApplicationCategory getApplicationCategory() {
        return applicationCategory;
    }

    public String getApplicationCategoryName() {
        return applicationCategory != null ? applicationCategory.getName() : "";
    }

    public Set<ApplicationTestLog> getApplicationTestLog() {
        return applicationTestLog;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public Boolean getArchiving() {
        return archiving;
    }

    public DatasetTemplate getDatasetTemplate() {
        return datasetTemplate;
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.APPLICATIONMANAGER;
    }

    public Set<Application> getDescendants() {
        final Set<Application> descendants = new HashSet<>();
        return getDescendants(descendants);
    }

    public Set<Application> getDescendants(Set<Application> descendants) {
        for (final Application application : getSuccessors()) {
            if (!descendants.contains(application)) {
                descendants.add(application);
                application.getDescendants(descendants);
            }
        }
        return descendants;
    }

    public String getEntityClassName() {
        return entityClassName;
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getApplicationType() != null) {
            addEntityInfoItem(summary, "type", getApplicationType().getName());
        }
        if (getTechnologiesAsString() != null) {
            addEntityInfoItem(summary, "technologies", getTechnologiesAsString());
        }
        if (getPageflow() != null) {
            addEntityInfoItem(summary, "pageflow", getPageflow().getName());
        }
        addEntityInfoItem(summary, "enabled", isEnabled());
        addEntityInfoItem(summary, "valid", isValid());
        addEntityInfoItem(summary, "hidden", isHidden());
        if (getArchiving() != null) {
            addEntityInfoItem(summary, "archiving", getArchiving());
        }
        addEntityInfoItem(summary, "forEmployeesOnly", isForEmployeesOnly());
        addEntityInfoItem(summary, "notifyApplicationSupervisor", isNotifyApplicationSupervisor());
        addEntityInfoItem(summary, "notifyContainerMember", isNotifyContainerMember());
        addEntityInfoItem(summary, "annotationRequired", isAnnotationRequired());
        addEntityInfoItem(summary, "importResources", isImportResourcesRequired());
        if (StringHelper.isNotEmpty(getEntityClassName())) {
            addEntityInfoItem(summary, "entityClassName", getEntityClassName());
        }
        if (StringHelper.isNotEmpty(getHelp())) {
            addEntityInfoItem(summary, "help", getHelp());
        }
        if (getExecutable() != null) {
            addEntityInfoItem(summary, "executable", getExecutable().getName());
        }
        if (getWrapperCreator() != null) {
            addEntityInfoItem(summary, "wrapperCreator", getWrapperCreator().getName());
        }
        if (getSubmitter() != null) {
            addEntityInfoItem(summary, "submitter", getSubmitter().getName());
        }
        if (getPredecessor() != null) {
            addEntityInfoItem(summary, "predecessor", getPredecessor().getName());
        }
        if (getStorage() != null) {
            addEntityInfoItem(summary, "storage", getStorage().getName());
        }
        if (getInstrument() != null) {
            addEntityInfoItem(summary, "instrument", getInstrument().getName());
        }
        addEntityInfoItems(summary, getCustomAttributes());
        return summary.toString();
    }

    public String getHelp() {
        return help;
    }

    public Set<ImportResource> getImportResources() {
        return importResources;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public String getOutputFileFormat() {
        return outputFileFormat;
    }

    public Pageflow getPageflow() {
        return pageflow;
    }

    @Override
    public Set<Parameter> getParameters() {
        return parameters;
    }

    public Set<Parameter> getParameters(ExecutableContextEnum executableContextEnum) {
        if (executableContextEnum == null) {
            return getParameters();
        }
        final Set<Parameter> parametersByContext = new HashSet<>();
        for (final Parameter parameter : getParameters()) {
            if (parameter.getContext().equals(executableContextEnum)) {
                parametersByContext.add(parameter);
            }
        }
        return parametersByContext;
    }

    public Set<Parameter> getParametersInUseByExecutableContext(ExecutableContextEnum executableContextEnum) {
        final Set<Parameter> parametersInUse = new HashSet<>();
        for (final Parameter parameter : getParameters(executableContextEnum)) {
            if (parameter.isInUse()) {
                parametersInUse.add(parameter);
            }
        }
        return parametersInUse;
    }

    public Map<String, String> getParametersKeyValueMap() {
        Map<String, String> parametersKeyValueMap = new HashMap<>();
        for (Parameter parameter : getParameters()) {
            parametersKeyValueMap.put(parameter.getKey(), parameter.getValue());
        }
        return parametersKeyValueMap;
    }

    public Set<Application> getPrecedingApplications() {
        return precedingApplications;
    }

    public Set<Application> getPrecedingWebApps() {
        return precedingWebApps;
    }

    public Application getPredecessor() {
        return predecessor;
    }

    public String getResourceRelativePathFilter() {
        return resourceRelativePathFilter;
    }

    public Boolean getRunnable() {
        return runnable;
    }

    public Storage getStorage() {
        return storage;
    }

    public Submitter getSubmitter() {
        return submitter;
    }

    public Set<Application> getSucceedingApplications() {
        return succeedingApplications;
    }

    public Application getSucceedingWebApp() {
        return succeedingWebApp;
    }

    public Set<Application> getSuccessors() {
        return successors;
    }

    @Override
    public Set<Technology> getTechnologies() {
        return technologies;
    }

    @Override
    public String getTechnologiesAsString() {
        return technologiesAsString;
    }

    public LocalDateTime getValidityChecked() {
        return validityChecked;
    }

    public String getValidityCheckedAsText() {
        return getValidityChecked() != null ? Constants.DATE_FORMATTER.format(getValidityChecked()) : "";
    }

    public String getWebUrl() {
        return webUrl;
    }

    public String getWebUrlWithToken(AbstractEntity entity, Job job) {
        StringBuilder requestParameter = new StringBuilder();
        if (entity != null) {
            StringBuilder tokenParameter = new StringBuilder("applicationId=").append(getId());
            if (job != null) {
                tokenParameter.append(",jobId=").append(job.getId());
            }
            requestParameter.append("?").append(entity.getTokenIncludingParameter(tokenParameter.toString()));
        }
        return webUrl + requestParameter;
    }

    public Set<Workunit> getWorkunits() {
        return workunits;
    }

    public WrapperCreator getWrapperCreator() {
        return wrapperCreator;
    }

    @Override
    public boolean hasNoDependents() {
        return getWorkunits().isEmpty() && getImportResources().isEmpty() && getSucceedingApplications().isEmpty();
    }

    private boolean hasNonExpiredImportResources(Container container) {
        if (hasNonExpiredImportResources == null) {
            hasNonExpiredImportResources = CDI.current().select(ImportResourceService.class).get().hasNonExpiredImportResourcesByContainerAndApplication(container, this);
        }
        return hasNonExpiredImportResources;
    }

    @Override
    public void indexDependents() {
        IndexHelper.indexEntities(getWorkunits());
    }

    public boolean isAnalysis() {
        return getApplicationType() != null && getApplicationType().getName().equals(Constants.APPLICATION_TYPE_ANALYSIS);
    }

    public boolean isAnnotationRequired() {
        return annotationRequired;
    }

    public boolean isForEmployeesOnly() {
        return forEmployeesOnly;
    }

    public boolean isHelpAvailable() {
        return StringHelper.isNotEmpty(getHelp()) && getHelp().contains("http");
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isImport() {
        return getApplicationType() != null && getApplicationType().getName().equals(Constants.APPLICATION_TYPE_IMPORT);
    }

    public boolean isImportResourcesRequired() {
        return importResourcesRequired;
    }

    public boolean isLinkImport() {
        return isImport() && getExecutable() == null;
    }

    public boolean isNotifyApplicationSupervisor() {
        return notifyApplicationSupervisor;
    }

    public boolean isNotifyContainerMember() {
        return notifyContainerMember;
    }

    public boolean isPreceding(Application application) {
        return getSucceedingApplications().contains(application);
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    public boolean isRunnable() {
        return isRunnable(getCurrentUser().getLastContainer());
    }

    public boolean isRunnable(Container container) {
        return container != null && container.hasBeenAccepted() && isEnabled() && (!isHidden() || isAdminOrSupervisor()) && (hasCurrentUserRoleEnum(RoleEnum.EMPLOYEE) || !isForEmployeesOnly() && getCurrentUser().hasExtensibleContainer()) && (isWebApp() && getWebUrl() != null || isAnalysis() || isImport() && container.isWorkunitCreatable() && (!isImportResourcesRequired() || hasNonExpiredImportResources(container)));
    }

    @Override
    public boolean isUpdatable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole());
    }

    public boolean isValidUrl() {
        return UriHelper.isValidUrl(getWebUrl());
    }

    public boolean isWebApp() {
        return getApplicationType() != null && getApplicationType().getName().equals(Constants.APPLICATION_TYPE_WEBAPP);
    }

    public void pageflowChanged(ValueChangeEvent event) {
        setPageflow((Pageflow) event.getNewValue());
    }

    public void resetFields() {
        if (isWebApp()) {
            setPageflow(null);
            setInstrument(null);
            setAnnotationRequired(false);
            setImportResourcesRequired(false);
            setStorage(null);
            setOutputFileFormat(null);
            setResourceRelativePathFilter(null);
            setExecutable(null);
            setWrapperCreatorAndParameters(null);
            setSubmitterAndParameters(null);
            getParameters().clear();
            if (!Dataset.class.getSimpleName().equalsIgnoreCase(getEntityClassName())) {
                setDatasetTemplate(null);
            }
        } else {
            setEntityClassName(null);
            setWebUrl(null);
            if (isAnalysis()) {
                setInstrument(null);
                setAnnotationRequired(false);
                setImportResourcesRequired(false);
            }
            for (final Parameter parameter : getParameters()) {
                parameter.setApplication(this);
            }
        }
    }

    public void setAnnotationRequired(boolean annotationRequired) {
        this.annotationRequired = isImport() && annotationRequired;
    }

    public void setApplicationCategory(ApplicationCategory applicationCategory) {
        this.applicationCategory = applicationCategory;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
        if (applicationType != null && getPageflow() == null) {
            // Set default pageflow when it is not set already.
            setPageflow(applicationType.getDefaultPageflow());
        }
    }

    public void setArchiving(Boolean archiving) {
        this.archiving = archiving;
    }

    public void setDatasetTemplate(DatasetTemplate datasetTemplate) {
        this.datasetTemplate = datasetTemplate;
    }

    public void setEntityClassName(String entityClassName) {
        this.entityClassName = StringHelper.format(entityClassName);
    }

    @Override
    public void setExecutable(Executable executable) {
        this.executable = executable;
        setExecutableParameters();
    }

    public void setExecutableAndParameters(Executable executable) {
        setExecutable(executable);
        setExecutableParameters();
    }

    public void setExecutableParameters() {
        if (getExecutable() != null) {
            getParameters().addAll(calculateParameters(getExecutable(), getParameters(ExecutableContextEnum.APPLICATION)));
        }
    }

    public void setForEmployeesOnly(boolean forEmployeesOnly) {
        this.forEmployeesOnly = forEmployeesOnly;
    }

    public void setHelp(String help) {
        this.help = StringHelper.format(help);
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void setImportResources(Set<ImportResource> importResources) {
        this.importResources = importResources;
    }

    public void setImportResourcesRequired(boolean importResourcesRequired) {
        this.importResourcesRequired = isImport() && importResourcesRequired;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public void setNotifyApplicationSupervisor(boolean notifyApplicationSupervisor) {
        this.notifyApplicationSupervisor = notifyApplicationSupervisor;
    }

    public void setNotifyContainerMember(boolean sendMailNotification) {
        this.notifyContainerMember = sendMailNotification;
    }

    public void setOutputFileFormat(String outputFileFormat) {
        this.outputFileFormat = StringHelper.format(outputFileFormat);
        if (this.outputFileFormat != null && this.outputFileFormat.startsWith(".")) {
            this.outputFileFormat = this.outputFileFormat.substring(1);
        }
    }

    public void setPageflow(Pageflow pageflow) {
        this.pageflow = pageflow;
    }

    @Override
    public void setParameters(Set<Parameter> parameters) {
        getParameters(ExecutableContextEnum.APPLICATION).clear();
        if (parameters != null && !parameters.isEmpty()) {
            getParameters().addAll(cloneParameters(parameters));
        }
    }

    public void setPrecedingApplications(Set<Application> precedingApplications) {
        this.precedingApplications = precedingApplications;
    }

    public void setPredecessor(Application predecessor) {
        this.predecessor = predecessor;
    }

    public void setResourceRelativePathFilter(String resourceRelativePathFilter) {
        this.resourceRelativePathFilter = StringHelper.format(resourceRelativePathFilter);
    }

    public void setRunnable(Boolean runnable) {
        this.runnable = runnable;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public void setSubmitter(Submitter submitter) {
        this.submitter = submitter;
    }

    public void setSubmitterAndParameters(Submitter submitter) {
        setSubmitter(submitter);
        setSubmitterParameters();
    }

    public void setSubmitterParameters() {
        getParameters().removeAll(getParameters(ExecutableContextEnum.SUBMITTER));
        if (getSubmitter() != null) {
            getParameters().addAll(cloneParameters(getSubmitter().getParametersInUse()));
        }
    }

    public void setSucceedingApplications(Set<Application> succeedingApplications) {
        this.succeedingApplications = succeedingApplications;
    }

    public void setSucceedingWebApp(Application succeeding) {
        this.succeedingWebApp = succeeding;
    }

    public void setSuccessors(Set<Application> successors) {
        this.successors = successors;
    }

    @Override
    public void setTechnologies(Set<Technology> technologies) {
        this.technologies = technologies;
        setTechnologiesAsString();
    }

    @Override
    public void setTechnologiesAsString(String technologiesAsString) {
        this.technologiesAsString = technologiesAsString;
    }

    public void setValidityChecked(LocalDateTime validityChecked) {
        this.validityChecked = validityChecked;
    }

    public void setWebUrl(String webUrl) {
        boolean checkValidity = true;
        String newUrl = StringHelper.format(webUrl);
        if (newUrl == null || newUrl.equals(this.webUrl)) {
            checkValidity = false;
        }
        this.webUrl = newUrl;
        if (checkValidity) {
            checkValidity();
        }
    }

    public void setWorkunits(Set<Workunit> workunits) {
        this.workunits = workunits;
    }

    public void setWrapperCreator(WrapperCreator wrapperCreator) {
        this.wrapperCreator = wrapperCreator;
    }

    public void setWrapperCreatorAndParameters(WrapperCreator wrapperCreator) {
        setWrapperCreator(wrapperCreator);
        setWrapperCreatorParameters();
    }

    public void setWrapperCreatorParameters() {
        getParameters().removeAll(getParameters(ExecutableContextEnum.WRAPPERCREATOR));
        if (getWrapperCreator() != null) {
            getParameters().addAll(cloneParameters(getWrapperCreator().getParametersInUse()));
        }
    }

    public void typeChanged(ValueChangeEvent event) {
        setApplicationType((ApplicationType) event.getNewValue());
        setPageflow(null);
    }
}