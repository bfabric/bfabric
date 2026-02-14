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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.HasParameters;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.ExecutableContextEnum;
import org.bfabric.enums.IndexMapContentEnum;
import org.bfabric.enums.LogActionEnum;
import org.bfabric.enums.MailTypeEnum;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.enums.WorkunitStatusEnum;
import org.bfabric.indexer.IndexHelper;
import org.bfabric.indexer.IndexMapContent;
import org.bfabric.indexer.api.Indexable;
import org.bfabric.indexer.enums.IndexMapEnum;
import org.bfabric.service.DatasetService;
import org.bfabric.service.ExternalJobService;
import org.bfabric.service.JobService;
import org.bfabric.service.WorkunitService;
import org.bfabric.util.BfabricUploadedFile;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.NumberUtils;
import org.bfabric.util.RepositoryHelper;
import org.bfabric.util.StringHelper;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@DynamicUpdate
@XmlRootElement
@NamedQuery(name = "Workunit.findBySampleId", query = "SELECT DISTINCT resource.workunit FROM Sample a JOIN a.resources resource WHERE a.id = :sampleId")
@NamedQuery(name = "Workunit.findSamplesReassignable", query = "SELECT DISTINCT a FROM Workunit a JOIN a.resources resource WHERE a.container.status not in (org.bfabric.enums.StatusEnum.CANCELED, org.bfabric.enums.StatusEnum.PRIVATE, org.bfabric.enums.StatusEnum.PUBLISHED ) and a.status not in (org.bfabric.enums.WorkunitStatusEnum.FAILED, org.bfabric.enums.WorkunitStatusEnum.AVAILABLE) and not exists(select r2.id from a.resources r2 where r2.status <> org.bfabric.enums.ResourceStatusEnum.AVAILABLE) order by a.id desc")
@NamedQuery(name = "Workunit.countResourcesUnassigned", query = "SELECT count(a) from Resource a WHERE a.workunit = :workunit AND a.sample IS NULL")
@NamedQuery(name = "Workunit.findSucceedingWorkunitsByWorkunitId", query = "SELECT a FROM Workunit a join a.inputResources inputResource WHERE inputResource.workunit.id = :workunitId")
public class Workunit extends AbstractContainerDependentEntity implements Indexable, HasParameters {

    private static final long serialVersionUID = 1;

    @ManyToMany
    @JoinTable(name = "commentworkunit", joinColumns = @JoinColumn(name = "workunitid"), inverseJoinColumns = @JoinColumn(name = "commentid"))
    @LazyCollection(LazyCollectionOption.EXTRA)
    private final Set<Comment> associatedComments = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "workunitjob", joinColumns = @JoinColumn(name = "workunitid"), inverseJoinColumns = @JoinColumn(name = "jobid"))
    @LazyCollection(LazyCollectionOption.EXTRA)
    private final Set<Job> workunitJobs = new HashSet<>();

    @Transient
    protected WorkunitStatusEnum oldStatus;

    @Transient
    private Boolean annotated;

    @Column(updatable = false, insertable = false)
    private long annotatedResourcesCount;

    @Column(updatable = false, insertable = false)
    private boolean annotationRequired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicationid")
    @NotNull
    @XmlIDREF
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicationexecutableid")
    @XmlIDREF
    private Executable applicationExecutable;

    @XmlElement
    private Boolean archiving;

    @Column(updatable = false, insertable = false)
    private long availableResourcesCount;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    @XmlIDREF
    private Set<WorkunitComment> comments = new HashSet<>();

    @OneToOne(mappedBy = "workunit", cascade = { CascadeType.REMOVE })
    @XmlIDREF
    private Dataset dataset;

    @Column(columnDefinition = "boolean DEFAULT false")
    @NotNull
    @XmlElement
    private boolean exportable = false;

    @Transient
    private List<ExternalJob> externalJobs;

    @Transient
    private List<ExternalJob> externalJobsOfWorkunitExecutables;

    @Transient
    private Boolean hasAssociatedComments;

    @Transient
    private Boolean hasSucceedingDatasets;

    @Transient
    private Boolean hasSucceedingWorkunits;

    @ManyToMany
    @JoinTable(name = "workunitimportresource", joinColumns = @JoinColumn(name = "workunitid"), inverseJoinColumns = @JoinColumn(name = "importresourceid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<ImportResource> importResources = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inputdatasetid")
    @XmlIDREF
    private Dataset inputDataset;

    @ManyToMany
    @JoinTable(name = "workunitinput", joinColumns = @JoinColumn(name = "workunitid"), inverseJoinColumns = @JoinColumn(name = "resourceid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "inputResource")
    private Set<Resource> inputResources = new HashSet<>();

    @Transient
    private Boolean isRenderedTree;

    @Transient
    private Job lastUnarchiveJob;

    @Transient
    private String log;

    @Transient
    private boolean notifyApplicationSupervisor;

    @Transient
    private boolean notifyContainerMember;

    @OneToMany(mappedBy = "workunit", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE }, orphanRemoval = true)
    @OrderBy("key")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Parameter> parameters = new HashSet<>();

    @Size(max = 64)
    private String progress;

    @OneToMany(mappedBy = "workunit", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE }, orphanRemoval = true)
    @OrderBy("name desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "resource")
    private Set<Resource> resources = new HashSet<>();

    @Column(updatable = false, insertable = false)
    private long resourcesCount;

    @Column(updatable = false, insertable = false)
    private long size;

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private WorkunitStatusEnum status = WorkunitStatusEnum.PENDING;

    @Transient
    private Submitter submitter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitterexecutableid")
    @XmlIDREF
    private Executable submitterExecutable;

    @Transient
    private List<Dataset> succeedingDatasets;

    @Transient
    private Integer succeedingDatasetsCount;

    @Transient
    private List<Workunit> succeedingWorkunits;

    @Transient
    private Integer succeedingWorkunitsCount;

    @ManyToMany
    @JoinTable(name = "workflowstepworkunit", joinColumns = @JoinColumn(name = "workunitid"), inverseJoinColumns = @JoinColumn(name = "workflowstepid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<WorkflowStep> workflowSteps = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "workflowworkunit", joinColumns = @JoinColumn(name = "workunitid"), inverseJoinColumns = @JoinColumn(name = "workflowid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Workflow> workflows = new HashSet<>();

    @OneToMany(mappedBy = "workunit", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE }, orphanRemoval = true)
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Executable> workunitExecutables = new HashSet<>();

    @Transient
    private WrapperCreator wrapperCreator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wrappercreatorexecutableid")
    @XmlIDREF
    private Executable wrapperCreatorExecutable;

    public Workunit() {
    }

    public Workunit(Application application, Container container) {
        setApplication(application);
        resetApplicationParameters();
        setContainer(container);
        setComputedName();
    }

    public Workunit(Application application, Dataset inputDataset, Container container) {
        setApplication(application);
        setInputDataset(inputDataset);
        setContainer(container);
        setComputedName();
    }

    public Workunit(Workunit workunit, Container container) {
        if (workunit != null) {
            setContainer(container);
            setDescription(workunit.getDescription());
            setExportable(workunit.isExportable());
            getImportResources().addAll(workunit.getImportResources());
            setInputDataset(workunit.getInputDataset());
            setName(workunit.getName());
            setApplication(workunit.getApplication());
            // Important: clear all default parameters before cloning the parameters from the workunit to be rerun!
            getParameters().clear();
            getParameters().addAll(cloneParameters(workunit.getParameters()));
        }
    }

    @Override
    public Workunit clone() throws CloneNotSupportedException {
        Workunit clone = (Workunit) super.clone();
        clone.importResources = new HashSet<>();
        clone.inputResources = new HashSet<>();
        clone.parameters = new HashSet<>();
        clone.resources = new HashSet<>();
        clone.oldStatus = null;
        return clone;
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

    public Set<ImportResource> createImportResourcesFromUploadedFiles(Set<BfabricUploadedFile> uploadedFiles) throws IOException {
        Set<ImportResource> createdImportResources = new HashSet<>();
        for (BfabricUploadedFile uploadedFile : uploadedFiles) {
            ImportResource importResource = new ImportResource(this, uploadedFile);
            RepositoryHelper.createImport(importResource);
            importResource.setAvailable();
            createdImportResources.add(importResource);
            getImportResources().add(importResource);
        }
        return createdImportResources;
    }

    public Mail createMail(MailTypeEnum mailTypeEnum) {
        Mail mail = new Mail();
        mail.setParent(getContainer());
        mail.setType(mailTypeEnum, getContainer().getEntityName() + " " + getContainer().getId());
        mail.setSubject(mail.getSubject() + " " + getId() + " " + getStatus());
        if (isNotifyApplicationSupervisor() && getApplication().getSupervisor() != null) {
            mail.addRecipient(getApplication().getSupervisor());
        }
        if (isNotifyContainerMember()) {
            mail.addRecipients(getContainer().getMembersTransitive());
        }
        mail.setInput("workunit", this);
        mail.setInput("container", getContainer());
        mail.setInput("containerLabel", getContainer().getClassLabelLowerCase());
        return mail;
    }

    public void createResourcesFromImportResources(Set<ImportResource> selectedImportResources) {
        if (selectedImportResources != null && !selectedImportResources.isEmpty()) {
            getImportResources().addAll(selectedImportResources);
            Set<Resource> newResources = new HashSet<>();
            for (ImportResource importResource : selectedImportResources) {
                newResources.add(new Resource(this, importResource));
            }
            setResources(newResources);
        }
    }

    public long getAnnotatedResourcesCount() {
        return annotatedResourcesCount;
    }

    public Application getApplication() {
        return application;
    }

    public Executable getApplicationExecutable() {
        return applicationExecutable;
    }

    public Boolean getArchiving() {
        return archiving;
    }

    public Set<Comment> getAssociatedComments() {
        return associatedComments;
    }

    public long getAvailableResourcesCount() {
        return availableResourcesCount;
    }

    @Override
    public CommentDiscriminator getCommentDiscriminator() {
        return CommentDiscriminator.WORKUNIT_COMMENT;
    }

    public Set<WorkunitComment> getComments() {
        return comments;
    }

    public String getComputedName() {
        String computedName = Constants.EMPTY_STRING;
        if (getApplication() != null) {
            computedName = getApplication().getName();
        }
        if (getDataset() != null) {
            computedName = getDataset().getName() + " " + computedName;
        }
        if (computedName.length() > Constants.MAX_LENGTH_NAME) {
            computedName = computedName.substring(0, Constants.MAX_LENGTH_NAME);
        }
        return computedName;
    }

    public WorkunitStatusEnum getComputedStatus() {
        if (getStatus() != null) {
            if (WorkunitStatusEnum.PROCESSING.equals(getStatus())) {
                return getStatus();
            }

            if (getResources().isEmpty() && !getStatus().isBasedOnResourceStatus()) {
                return getStatus();
            }
        }
        if (!getResources().isEmpty()) {
            WorkunitStatusEnum computedStatus = WorkunitStatusEnum.PROCESSING;
            for (Resource resource : getResources()) {
                if (resource.isFailed()) {
                    return WorkunitStatusEnum.FAILED;
                } else if (resource.isDeleted()) {
                    computedStatus = WorkunitStatusEnum.DELETED;
                } else if (resource.isExpired() && !computedStatus.equals(WorkunitStatusEnum.DELETED)) {
                    computedStatus = WorkunitStatusEnum.EXPIRED;
                } else if (resource.isArchived() && !(computedStatus.equals(WorkunitStatusEnum.DELETED) || computedStatus.equals(WorkunitStatusEnum.EXPIRED))) {
                    computedStatus = WorkunitStatusEnum.ARCHIVED;
                } else if (resource.isPending() && !(computedStatus.equals(WorkunitStatusEnum.DELETED) || computedStatus.equals(WorkunitStatusEnum.EXPIRED) || computedStatus.equals(WorkunitStatusEnum.ARCHIVED))) {
                    computedStatus = WorkunitStatusEnum.PENDING;
                }
            }
            if (!computedStatus.equals(WorkunitStatusEnum.PROCESSING)) {
                return computedStatus;
            }
            if (isImport()) {
                if (!getApplication().isAnnotationRequired()) {
                    return WorkunitStatusEnum.AVAILABLE;
                }
                for (Resource resource : getResources()) {
                    if (!resource.isAnnotated()) {
                        return WorkunitStatusEnum.IMPORTED;
                    }
                }
            }
            return WorkunitStatusEnum.AVAILABLE;
        }
        return WorkunitStatusEnum.PENDING;
    }

    @SuppressWarnings("unused")
    public String getDataFolder() {
        List<Resource> filteredResources = getResources().stream().filter(r -> {
                String path = r.getRelativePath();
                return path != null && path.contains("/");
            })
            .collect(Collectors.toList());
        if (filteredResources.isEmpty()) {
            return null;
        }
        List<String> paths = filteredResources.stream().map(Resource::getRelativePath).collect(Collectors.toList());
        String commonPrefix = paths.get(0);
        for (String path : paths) {
            while (!path.startsWith(commonPrefix)) {
                int lastSlash = commonPrefix.lastIndexOf("/");
                if (lastSlash == -1) {
                    return null;
                }
                commonPrefix = commonPrefix.substring(0, lastSlash);
            }
        }
        Resource resourceWithSlash = filteredResources.get(0);
        String url = resourceWithSlash.getUriDownloadHttp();
        return (url != null && url.contains("/")) ? url.substring(0, url.lastIndexOf("/")) : null;
    }

    public Dataset getDataset() {
        return dataset;
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getStatus() != null) {
            addEntityInfoItem(summary, "status", getStatus());
        }
        if (getApplication() != null) {
            addEntityInfoItem(summary, "application", getApplication().getName());
        }
        if (getDataset() != null) {
            addEntityInfoItem(summary, "dataset", getDataset().getDisplayName());
        }
        if (StringHelper.isNotEmpty(getReportResourceName())) {
            addEntityInfoItem(summary, "report", getReportResourceName());
        }
        if (getContainer() != null) {
            addEntityInfoItem(summary, getContainer().getClassLabelLowerCase(), getContainer().getId());
        }
        addEntityInfoItem(summary, "resources", getResources().size());
        addEntityInfoItem(summary, "availableResources", getAvailableResourcesCount());
        if (!getInputResources().isEmpty()) {
            addEntityInfoItem(summary, "inputResources", getInputResources().size());
        }
        if (getInputDataset() != null) {
            addEntityInfoItem(summary, "inputDataset", getInputDataset().getDisplayName());
        }
        addEntityInfoItem(summary, "exportable", isExportable());
        if (getArchiving() != null) {
            addEntityInfoItem(summary, "archiving", getArchiving());
        }
        return summary.toString();
    }

    public Set<Executable> getExecutables() {
        Set<Executable> executables = new HashSet<>();
        if (isApplicationExecutableVisible()) {
            executables.add(getApplicationExecutable());
        }
        if (getWrapperCreatorExecutable() != null) {
            executables.add(getWrapperCreatorExecutable());
        }
        if (getSubmitterExecutable() != null) {
            executables.add(getSubmitterExecutable());
        }
        executables.addAll(getWorkunitExecutables());
        return executables;
    }

    public List<ExternalJob> getExternalJobs() {
        if (externalJobs == null) {
            externalJobs = CDI.current().select(ExternalJobService.class).get().getExternalJobsByClientEntity(this);
        }
        return externalJobs;
    }

    public List<ExternalJob> getExternalJobsOfWorkunitExecutables() {
        if (externalJobsOfWorkunitExecutables == null) {
            externalJobsOfWorkunitExecutables = CDI.current().select(ExternalJobService.class).get().getExternalJobsByClientEntityAndExecutableContext(this, ExecutableContextEnum.WORKUNIT);
        }
        return externalJobsOfWorkunitExecutables;
    }

    public Set<ImportResource> getImportResources() {
        return importResources;
    }

    @Override
    public void getIndexFields(Collection<String> fields) {
        fields.addAll(getIndexListingFields());
    }

    @Override
    public List<String> getIndexListingFields() {
        List<String> fields = super.getIndexListingFields();
        fields.add(IndexMapContentEnum.STATUS.getField());
        fields.add(IndexMapContentEnum.SIZE.getField());
        fields.add(IndexMapContentEnum.APPLICATION.getField());
        fields.add(IndexMapContentEnum.TECHNOLOGY.getField());
        fields.add(IndexMapContentEnum.RESOURCES.getField());
        fields.add(IndexMapContentEnum.AVAILABLERESOURCES.getField());
        fields.add(IndexMapContentEnum.PROGRESS.getField());
        fields.add(IndexMapContentEnum.ARCHIVING.getField());
        fields.add(IndexMapContentEnum.WORKUNITPARAMETER.getField());
        return fields;
    }

    @Override
    public IndexMapContent getIndexMapContent() throws Exception {
        IndexMapContent content = super.getIndexMapContent();

        if (getApplication() != null) {
            content.add(IndexMapContentEnum.APPLICATION, getApplication().getName());
            if (!getApplication().getTechnologies().isEmpty()) {
                for (Technology technology : getApplication().getTechnologies()) {
                    content.add(IndexMapContentEnum.TECHNOLOGY, technology.getName());
                }
            }
        }
        for (Parameter parameter : getParameters()) {
            content.add(IndexMapContentEnum.WORKUNITPARAMETER, parameter.getKey());
            content.add(IndexMapContentEnum.WORKUNITPARAMETER, parameter.getValue());
        }
        content.add(IndexMapContentEnum.STATUS, getStatus());
        content.add(IndexMapContentEnum.SIZE, getSize(), getPrintSize());
        content.add(IndexMapContentEnum.RESOURCES, getResourcesCount());
        content.add(IndexMapContentEnum.AVAILABLERESOURCES, getAvailableResourcesCount());
        if (getProgress() != null) {
            content.add(IndexMapContentEnum.PROGRESS, getProgress());
        }
        if (getArchiving() != null) {
            content.add(IndexMapContentEnum.ARCHIVING, getArchiving());
        }

        return content;
    }

    @Override
    public IndexMapEnum getIndexMapEnum() {
        return IndexMapEnum.WORKUNIT;
    }

    public Dataset getInputDataset() {
        return inputDataset;
    }

    public Resource getInputResource() {
        if (getInputResources().size() == 1) {
            return (Resource) getInputResources().toArray()[0];
        }
        return null;
    }

    public Set<Resource> getInputResources() {
        return inputResources;
    }

    public Job getLastUnarchiveJob() {
        if (lastUnarchiveJob == null) {
            lastUnarchiveJob = CDI.current().select(JobService.class).get().getLastUnarchiveJob(this);
        }
        return lastUnarchiveJob;
    }

    public Link getLink() {
        return getLinks().size() == 1 ? getLinks().get(0) : null;
    }

    public String getLog() {
        if (log == null) {
            log = getExternalJobsOfWorkunitExecutables().stream().findFirst().map(ExternalJob::getLog).orElse(null);
        }
        return log;
    }

    public WorkunitStatusEnum getOldStatus() {
        return oldStatus;
    }

    @Override
    public Set<Parameter> getParameters() {
        return parameters;
    }

    public Set<Parameter> getParameters(ExecutableContextEnum executableContextEnum) {
        if (executableContextEnum != null) {
            Set<Parameter> parametersByContext = new HashSet<>();
            for (Parameter parameter : getParameters()) {
                if (parameter.getContext().equals(executableContextEnum)) {
                    parametersByContext.add(parameter);
                }
            }
            return parametersByContext;
        }
        return getParameters() != null ? getParameters() : new HashSet<>();
    }

    public Map<String, String> getParametersKeyValueMap() {
        Map<String, String> parametersKeyValueMap = new HashMap<>();
        for (Parameter parameter : getParameters()) {
            parametersKeyValueMap.put(parameter.getKey(), parameter.getValue());
        }
        return parametersKeyValueMap;
    }

    public List<Parameter> getParametersOptional(ExecutableContextEnum executableContextEnum) {
        List<Parameter> parametersOptional = new ArrayList<>();
        for (Parameter parameter : getParameters(executableContextEnum)) {
            if (!parameter.isRequired()) {
                parametersOptional.add(parameter);
            }
        }
        return CollectionHelper.sortObjects(parametersOptional);
    }

    public List<Parameter> getParametersRequired(ExecutableContextEnum executableContextEnum) {
        List<Parameter> parametersRequired = new ArrayList<>();
        for (Parameter parameter : getParameters(executableContextEnum)) {
            if (parameter.isRequired()) {
                parametersRequired.add(parameter);
            }
        }
        return CollectionHelper.sortObjects(parametersRequired);
    }

    public String getPrintSize() {
        return NumberUtils.getPrintSize(getSize());
    }

    public String getProgress() {
        return progress;
    }

    private Resource getReportResource() {
        for (Resource resource : getResources()) {
            if (resource.getReport() != null) {
                return resource;
            }
        }
        return null;
    }

    private Resource getReportResourceLegacy() {
        Resource reportResource = null;
        if (getInputDataset() != null) {
            Iterator<Resource> resourceIter = getResources().iterator();
            if (resourceIter.hasNext()) {
                reportResource = resourceIter.next();
            }
        }
        return reportResource;
    }

    public String getReportResourceLink() {
        Resource reportResource = getReportResource();

        if (reportResource != null) {
            return reportResource.getUriDownloadHttp();
        }

        reportResource = getReportResourceLegacy();
        if (reportResource != null) {
            String reportResourceLink = reportResource.getUriDownloadHttp();
            if (reportResourceLink != null) {
                String[] retArray = reportResourceLink.split("/");
                retArray[retArray.length - 1] = "00index.html";
                StringBuilder value = new StringBuilder();
                Iterator<String> valueListIterator = Arrays.asList(retArray).iterator();
                while (valueListIterator.hasNext()) {
                    value.append(valueListIterator.next());
                    if (valueListIterator.hasNext()) {
                        value.append("/");
                    }
                }

                return value.toString();
            }
        }

        return null;
    }

    public String getReportResourceName() {
        Resource reportResource = getReportResource();

        if (reportResource != null) {
            return reportResource.getReport();
        }

        reportResource = getReportResourceLegacy();
        if (reportResource != null) {
            return getApplication().getName() + " Result " + reportResource.getId();
        }

        return null;
    }

    public Resource getResource() {
        if (getResources().size() == 1) {
            return (Resource) getResources().toArray()[0];
        }
        return null;
    }

    public Set<Resource> getResources() {
        return resources;
    }

    public List<Resource> getResourcesAsList() {
        return CollectionHelper.asList(getResources());
    }

    public long getResourcesCount() {
        return resourcesCount;
    }

    public String getRowStyleClass() {
        if (isAvailable()) {
            return Constants.BACKGROUND_COLOR_GREEN;
        }
        if (isFailed()) {
            return Constants.BACKGROUND_COLOR_RED;
        }
        if (isDeleted()) {
            return Constants.BACKGROUND_COLOR_RED;
        }
        if (isExpired()) {
            return Constants.BACKGROUND_COLOR_RED;
        }
        if (isImported()) {
            return Constants.BACKGROUND_COLOR_BLUE;
        }
        if (isProcessing()) {
            return Constants.BACKGROUND_COLOR_ORANGE;
        }
        if (isPending()) {
            return Constants.BACKGROUND_COLOR_ORANGE;
        }
        if (isArchived()) {
            return Constants.BACKGROUND_COLOR_BROWN;
        }
        return Constants.EMPTY_STRING;
    }

    @Override
    public List<Application> getRunnableApplications() {
        if (runnableApplications == null) {
            runnableApplications = new ArrayList<>();
            for (Application succeedingApplication : getApplication().getSucceedingApplications()) {
                if (!succeedingApplication.isWebApp() && succeedingApplication.isRunnable()) {
                    runnableApplications.add(succeedingApplication);
                }
            }
        }
        return runnableApplications;
    }

    public List<Dataset> getSelectableDatasets() {
        if (getContainer() != null) {
            if (getApplication().getDatasetTemplate() == null) {
                return getContainer().getAssociatedDatasets();
            }
            return getContainer().getAssociatedDatasets().stream().filter(dataset -> getApplication().getDatasetTemplate().equals(dataset.getDatasetTemplate())).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public Long getSize() {
        return size;
    }

    public WorkunitStatusEnum getStatus() {
        return status;
    }

    public WorkunitStatusEnum getStatusExternalJobUpdate() {
        if (isAnyExecutableFailed()) {
            return WorkunitStatusEnum.FAILED;
        }
        if (isAllWorkunitExecutablesDone()) {
            return getComputedStatus();
        }
        return getStatus();
    }

    public Executable getStorageExecutable() {
        return !getResources().isEmpty() ? new ArrayList<>(getResources()).get(0).getStorage().getExecutable() : null;
    }

    public Submitter getSubmitter() {
        return submitter;
    }

    public Executable getSubmitterExecutable() {
        return submitterExecutable;
    }

    public List<Dataset> getSucceedingDatasets() {
        if (succeedingDatasets == null) {
            succeedingDatasets = CDI.current().select(DatasetService.class).get().getSucceedingDatasetsByWorkunitId(getId());
        }
        return succeedingDatasets;
    }

    public int getSucceedingDatasetsCount() {
        if (succeedingDatasetsCount == null) {
            succeedingDatasetsCount = getSucceedingDatasets().size();
        }
        return succeedingDatasetsCount;
    }

    public Application getSucceedingWebApp() {
        return getApplication().getSucceedingWebApp();
    }

    public String getSucceedingWebAppUrl() {
        return getSucceedingWebApp() == null ? null : getSucceedingWebApp().getWebUrl();
    }

    public List<Workunit> getSucceedingWorkunits() {
        if (succeedingWorkunits == null) {
            succeedingWorkunits = CDI.current().select(WorkunitService.class).get().getSucceedingWorkunitsByWorkunitId(getId());
        }
        return succeedingWorkunits;
    }

    public int getSucceedingWorkunitsCount() {
        if (succeedingWorkunitsCount == null) {
            succeedingWorkunitsCount = getSucceedingWorkunits().size();
        }
        return succeedingWorkunitsCount;
    }

    public Set<WorkflowStep> getWorkflowSteps() {
        return workflowSteps;
    }

    public List<WorkflowStep> getWorkflowStepsAsList() {
        return CollectionHelper.asList(workflowSteps);
    }

    public Set<Workflow> getWorkflows() {
        return workflows;
    }

    public List<Workflow> getWorkflowsAsList() {
        return CollectionHelper.asList(workflows);
    }

    public Set<Executable> getWorkunitExecutables() {
        return workunitExecutables;
    }

    public Set<Job> getWorkunitJobs() {
        return workunitJobs;
    }

    public WrapperCreator getWrapperCreator() {
        return wrapperCreator;
    }

    public Executable getWrapperCreatorExecutable() {
        return wrapperCreatorExecutable;
    }

    public boolean hasAssociatedComments() {
        if (hasAssociatedComments == null) {
            hasAssociatedComments = !getAssociatedComments().isEmpty();
        }
        return hasAssociatedComments;
    }

    public boolean hasJunkResources() {
        for (Resource resource : getResources()) {
            if (resource.isJunk()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSucceedingDatasets() {
        if (hasSucceedingDatasets == null) {
            hasSucceedingDatasets = getSucceedingDatasetsCount() > 0;

        }
        return hasSucceedingDatasets;
    }

    public boolean hasSucceedingWorkunits() {
        if (hasSucceedingWorkunits == null) {
            hasSucceedingWorkunits = getSucceedingWorkunitsCount() > 0;
        }
        return hasSucceedingWorkunits;
    }

    @Override
    public void indexDependents() {
        IndexHelper.indexEntities(getResources());
    }

    public boolean isAllWorkunitExecutablesDone() {
        boolean done = !getExternalJobsOfWorkunitExecutables().isEmpty();
        for (ExternalJob externalJob : getExternalJobsOfWorkunitExecutables()) {
            if (!StatusEnum.DONE.equals(externalJob.getStatus())) {
                // Found one external job with status other than "done".
                done = false;
                break;
            }
        }
        return done;
    }

    public boolean isAnnotated() {
        if (annotated == null) {
            annotated = CDI.current().select(WorkunitService.class).get().isAnnotated(this);
        }
        return annotated;
    }

    public boolean isAnnotationRequired() {
        return annotationRequired;
    }

    public boolean isAnyExecutableFailed() {
        for (ExternalJob externalJob : getExternalJobs()) {
            if (StatusEnum.FAILED.equals(externalJob.getStatus())) {
                // Found one external job with status "failed".
                return true;
            }
        }
        return false;
    }

    public boolean isAnyExecutablePending() {
        for (ExternalJob externalJob : getExternalJobs()) {
            if (externalJob.getExecutable() != null && !StatusEnum.isFinished(externalJob.getStatus())) {
                // Found one external job with status not "finished" yet.
                return true;
            }
        }
        return false;
    }

    public boolean isApplicationExecutableVisible() {
        return getApplicationExecutable() != null && hasCurrentUserRoleEnum(RoleEnum.APPLICATIONMANAGER);
    }

    public boolean isArchived() {
        return WorkunitStatusEnum.ARCHIVED.equals(getStatus());
    }

    public boolean isAssignSamplesButtonRendered() {
        return !getResources().isEmpty() && isSamplesAssignable();
    }

    public boolean isAvailable() {
        return WorkunitStatusEnum.AVAILABLE.equals(getStatus());
    }

    @Override
    public boolean isCreatable() {
        return true;
    }

    @Override
    public boolean isDeletable() {
        return getId() > 0 && getContainer() != null && getContainer().isExtensible() && isPurgeable();
    }

    public boolean isDeleted() {
        return WorkunitStatusEnum.DELETED.equals(getStatus());
    }

    public boolean isDownloadButtonRendered() {
        return Resource.isDownloadButtonRendered(getResources());
    }

    public boolean isDownloadManagerDownloadButtonRendered() {
        return Resource.isDownloadManagerDownloadButtonRendered(getResources());
    }

    public boolean isExpired() {
        return WorkunitStatusEnum.EXPIRED.equals(getStatus());
    }

    public boolean isExportable() {
        return exportable;
    }

    @Override
    public boolean isExtensible() {
        return getContainer() != null && getContainer().isExtensible();
    }

    public boolean isFailed() {
        return WorkunitStatusEnum.FAILED.equals(getStatus());
    }

    public boolean isImport() {
        return getApplication() != null && getApplication().isImport();
    }

    public boolean isImported() {
        return WorkunitStatusEnum.IMPORTED.equals(getStatus());
    }

    public boolean isLinkImport() {
        return getApplication() != null && getApplication().isLinkImport();
    }

    public boolean isMarkableDeleted() {
        return isAvailable() && hasCurrentUserRoleEnum(RoleEnum.STORAGEMANAGER);
    }

    public boolean isNoResourceAnnotated() {
        for (Resource resource : getResources()) {
            if (resource.getSample() != null) {
                return false;
            }
        }
        return true;
    }

    public boolean isNotifyApplicationSupervisor() {
        return notifyApplicationSupervisor;
    }

    public boolean isNotifyContainerMember() {
        return notifyContainerMember;
    }

    public boolean isPending() {
        return WorkunitStatusEnum.PENDING.equals(getStatus());
    }

    public boolean isProcessing() {
        return WorkunitStatusEnum.PROCESSING.equals(getStatus());
    }

    public boolean isPurgeable() {
        return getWorkflows().isEmpty() && getWorkflowSteps().isEmpty() && !hasAssociatedComments() && !hasSucceedingWorkunits() && !hasSucceedingDatasets();
    }

    @Override
    public boolean isReadable() {
        return getContainer() != null && getContainer().isReadable();
    }

    public boolean isRenderedCancelUnarchive() {
        return getConfiguration().isUnarchiveEnabled() && getLastUnarchiveJob() != null && getLastUnarchiveJob().getStatus().equals(StatusEnum.NEW) && getLastUnarchiveJob().getRequester()
            .equals(getCurrentUser());
    }

    public boolean isRenderedRefresh() {
        return isPending() || isAnyExecutablePending();
    }

    public boolean isRenderedTree() {
        if (isRenderedTree == null) {
            isRenderedTree = !getSucceedingWorkunits().isEmpty() || !getSucceedingDatasets().isEmpty() || !getAssociatedDatasets().isEmpty();
        }
        return isRenderedTree;
    }

    public boolean isRenderedUnarchive() {
        return getConfiguration().isUnarchiveEnabled() && isArchived() && (getLastUnarchiveJob() == null || getLastUnarchiveJob().getStatus()
            .equals(StatusEnum.DONE) || getLastUnarchiveJob().getStatus().equals(StatusEnum.CANCELED));
    }

    public boolean isReplacingSubmitterAllowed() {
        return hasCurrentUserRoleEnum(RoleEnum.ADMIN) || hasCurrentUserRoleEnum(RoleEnum.APPLICATIONMANAGER);
    }

    public boolean isReplacingWrapperCreatorAllowed() {
        return hasCurrentUserRoleEnum(RoleEnum.ADMIN) || hasCurrentUserRoleEnum(RoleEnum.APPLICATIONMANAGER);
    }

    public boolean isReportResourceAccessible() {
        Resource reportResource = getReportResource();
        if (reportResource == null) {
            reportResource = getReportResourceLegacy();
        }

        return reportResource != null && reportResource.getUriDownloadHttp() != null && isAvailable();
    }

    public boolean isRerunnable() {
        // If 1) workunit must have been created by analysis application, 2) application is not hidden (in the meanwhile), 3) user is allowed to rerun in principle (is employee or if not the application is not employees only), 4) workunit is updatable (i.e., the user has the access rights to create the workunit in this project)
        return getApplication() != null && !getApplication().isHidden() && getApplication().isAnalysis() && (hasCurrentUserRoleEnum(RoleEnum.EMPLOYEE) || !getApplication()
            .isForEmployeesOnly() && getCurrentUser().hasExtensibleContainer());
    }

    public boolean isResourceFilesDeletable() {
        return getApplication().isAnalysis();
    }

    public boolean isResubmitButtonVisible() {
        for (ExternalJob externalJob : getExternalJobsOfWorkunitExecutables()) {
            if (StatusEnum.FAILED.equals(externalJob.getStatus())) {
                return true;
            }
        }
        return false;
    }

    public boolean isSamplesAssignable() {
        return isUpdatable() && isImport() && (!isAvailable() || !hasSucceedingWorkunits() && !hasSucceedingDatasets());
    }

    public boolean isStatusChanged() {
        return getStatus() != null && !getStatus().equals(getOldStatus()) || getOldStatus() != null && !getOldStatus().equals(getStatus());
    }

    public boolean isSubmitterVisibleEditScreen() {
        return !isManaged() && getApplicationExecutable() != null && (isReplacingSubmitterAllowed() || !getParameters(ExecutableContextEnum.SUBMITTER).isEmpty());
    }

    public boolean isSucceedingWebAppRunnable() {
        Application succeedingWebApp = getSucceedingWebApp();
        return succeedingWebApp != null && succeedingWebApp.isRunnable();
    }

    @Override
    public boolean isUpdatable() {
        return getContainer() != null && getContainer().isExtensible();
    }

    public boolean isWrapperCreatorVisibleEditScreen() {
        return !isManaged() && getApplicationExecutable() != null && (isReplacingWrapperCreatorAllowed() || !getParameters(ExecutableContextEnum.WRAPPERCREATOR).isEmpty());
    }

    @Override
    protected void preRemove() {
        super.preRemove();

        // Remove resources from the storage.
        removeResourcesFromStorage();

        // Remove resources of the workunit from the corresponding resourceBaskets.
        removeResourcesFromResourceBaskets();
    }

    public void removeResourcesFromResourceBaskets() {
        for (Resource resource : getResources()) {
            for (ResourceBasket resourceBasket : resource.getResourceBaskets()) {
                resourceBasket.getResources().remove(resource);
            }
        }
    }

    public void removeResourcesFromStorage() {
        if (isResourceFilesDeletable() && getStorageExecutable() != null) {
            CDI.current().select(ExternalJobService.class).get().createAndExecuteExternalJob(this, LogActionEnum.DELETE, getStorageExecutable());
        }
    }

    public void resetApplicationParameters() {
        getParameters().removeAll(getParameters());
        if (getApplication() != null) {
            getParameters().addAll(cloneParameters(getApplication().getParametersInUse()));
        }
    }

    public void resetParameters() {
        for (Parameter parameter : getParameters()) {
            parameter.setWorkunit(this);
            parameter.setParentAllowsModification(false);
        }
    }

    public void resetStatus() {
        setStatus(getComputedStatus());
    }

    public String runSucceedingWebApp() {
        if (isSucceedingWebAppRunnable()) {
            getSucceedingWebApp().createWebAppJob(this, getCurrentUser());
            return getSucceedingWebAppUrl();
        }
        return null;
    }

    public void setApplication(Application application) {
        if (application != null && !application.equals(this.application) || this.application != null && !this.application.equals(application)) {
            this.application = application;
            if (application != null) {
                setApplicationExecutable(application.getExecutable());
                setSubmitter(application.getSubmitter());
                setWrapperCreator(application.getWrapperCreator());
            }
            resetApplicationParameters();
        }
    }

    public void setApplicationExecutable(Executable applicationExecutable) {
        this.applicationExecutable = applicationExecutable;
    }

    public void setArchiving(Boolean archiving) {
        this.archiving = archiving;
    }

    public void setComments(Set<WorkunitComment> comments) {
        this.comments = comments;
    }

    public void setComputedName() {
        super.setName(getComputedName());
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public void setExportable(boolean exportable) {
        this.exportable = exportable;
    }

    public void setImportResources(Set<ImportResource> importResources) {
        this.importResources = importResources;
    }

    public void setInputDataset(Dataset inputDataset) {
        this.inputDataset = inputDataset;
    }

    public void setInputResources(Set<Resource> inputResources) {
        this.inputResources = inputResources;
    }

    @Override
    public void setName(final String name) {
        // Note: Invoke this method after having set the application and input dataset attribute to compute the name if the given name is empty. When the name has changed, indexDependents is set and thus the indexer will index all dependent entities.
        if (StringHelper.isNotEmpty(name)) {
            super.setName(name);
        } else {
            setComputedName();
        }
    }

    public void setNotify() {
        if (isStatusChanged() && getApplication() != null) {
            setNotifyApplicationSupervisor(getApplication().isNotifyApplicationSupervisor());
            setNotifyContainerMember(WorkunitStatusEnum.AVAILABLE.equals(getStatus()) && getApplication().isNotifyContainerMember());
        }
    }

    public void setNotifyApplicationSupervisor(boolean notifyApplicationSupervisor) {
        this.notifyApplicationSupervisor = notifyApplicationSupervisor;
    }

    public void setNotifyContainerMember(boolean notifyContainerMember) {
        this.notifyContainerMember = notifyContainerMember;
    }

    public void setOldStatus(WorkunitStatusEnum oldStatus) {
        if (this.oldStatus == null) {
            this.oldStatus = oldStatus;
        }
    }

    public void setParameters(Set<Parameter> parameters) {
        getParameters().clear();
        if (parameters != null && !parameters.isEmpty()) {
            getParameters().addAll(cloneParameters(parameters));
        }
    }

    public void setPending() {
        setStatus(WorkunitStatusEnum.PENDING);
    }

    public void setProgress(String progress) {
        this.progress = StringHelper.format(progress);
    }

    public void setResources(Set<Resource> resources) {
        this.resources = resources;
    }

    public void setStatus(WorkunitStatusEnum status) {
        setOldStatus(getStatus());
        this.status = status;
        if (!WorkunitStatusEnum.PROCESSING.equals(status)) {
            setProgress(null);
        }
    }

    public void setStatusExternalJobUpdate() {
        setStatus(getStatusExternalJobUpdate());
    }

    public void setSubmitter(Submitter submitter) {
        this.submitter = submitter;
        if (getSubmitter() != null) {
            setSubmitterExecutable(getSubmitter().getExecutable());
        }
    }

    public void setSubmitterAndParameters(Submitter submitter) {
        setSubmitter(submitter);
        setSubmitterParameters();
    }

    public void setSubmitterExecutable(Executable submitterExecutable) {
        this.submitterExecutable = submitterExecutable;
    }

    public void setSubmitterParameters() {
        getParameters().removeAll(getParameters(ExecutableContextEnum.SUBMITTER));
        if (getSubmitter() != null) {
            getParameters().addAll(cloneParameters(getSubmitter().getParametersInUse()));
        }
    }

    public void setWorkflowSteps(Set<WorkflowStep> workflowSteps) {
        this.workflowSteps = workflowSteps;
    }

    public void setWorkflowStepsAsList(List<WorkflowStep> workflowSteps) {
        this.workflowSteps = (Set<WorkflowStep>) CollectionHelper.asSet(workflowSteps);
    }

    public void setWorkflows(Set<Workflow> workflows) {
        this.workflows = workflows;
    }

    public void setWorkflowsAsList(List<Workflow> workflows) {
        this.workflows = (Set<Workflow>) CollectionHelper.asSet(workflows);
    }

    public void setWorkunitExecutables(Set<Executable> workunitExecutables) {
        this.workunitExecutables = workunitExecutables;
    }

    public void setWrapperCreator(WrapperCreator wrapperCreator) {
        this.wrapperCreator = wrapperCreator;
        if (getWrapperCreator() != null) {
            setWrapperCreatorExecutable(getWrapperCreator().getExecutable());
        }
    }

    public void setWrapperCreatorAndParameters(WrapperCreator wrapperCreator) {
        setWrapperCreator(wrapperCreator);
        setWrapperCreatorParameters();
    }

    public void setWrapperCreatorExecutable(Executable wrapperCreatorExecutable) {
        this.wrapperCreatorExecutable = wrapperCreatorExecutable;
    }

    public void setWrapperCreatorParameters() {
        getParameters().removeAll(getParameters(ExecutableContextEnum.WRAPPERCREATOR));
        if (getWrapperCreator() != null) {
            getParameters().addAll(cloneParameters(getWrapperCreator().getParametersInUse()));
        }
    }

    public void triggerApplicationExecution() {
        if (getApplicationExecutable() != null) {
            if (getWrapperCreatorExecutable() != null && getSubmitterExecutable() != null) {
                CDI.current().select(ExternalJobService.class).get().createAndExecuteExternalJob(this, LogActionEnum.CREATE, getWrapperCreatorExecutable());
            } else {
                CDI.current().select(ExternalJobService.class).get().createAndExecuteExternalJob(this, LogActionEnum.SUBMIT, getSubmitterExecutable());
            }
        }
    }
}