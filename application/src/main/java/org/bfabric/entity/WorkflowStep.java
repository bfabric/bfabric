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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Constants;
import org.bfabric.entity.api.ParentDependent;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.CommentDiscriminator;
import org.bfabric.enums.RoleEnum;
import org.bfabric.enums.StatusEnum;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.DateUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
public class WorkflowStep extends AbstractSupervisorBaseEntity implements ShowScreen, ParentDependent {

    private static final long serialVersionUID = 1;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @OrderBy("created DESC")
    @XmlIDREF
    private final Set<WorkflowStepComment> comments = new HashSet<>();

    @OneToMany(mappedBy = "parent")
    @OrderBy("created")
    private final Set<WorkflowStepComment> commentsOrderedByCreated = new HashSet<>();

    @Transient
    private WorkflowStepComment comment;

    @ManyToMany
    @JoinTable(name = "workflowstepdataset", joinColumns = @JoinColumn(name = "workflowstepid"), inverseJoinColumns = @JoinColumn(name = "datasetid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "dataset")
    private Set<Dataset> datasets = new HashSet<>();

    private LocalDateTime endDateTime = LocalDateTime.now();

    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal expectedDuration;

    @ManyToMany
    @JoinTable(name = "workflowstepplate", joinColumns = @JoinColumn(name = "workflowstepid"), inverseJoinColumns = @JoinColumn(name = "plateid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "plate")
    private Set<Plate> plates = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "samplepreparationprotocolid")
    @XmlIDREF
    private SamplePreparationProtocol samplePreparationProtocol;

    @ManyToMany
    @JoinTable(name = "workflowstepsample", joinColumns = @JoinColumn(name = "workflowstepid"), inverseJoinColumns = @JoinColumn(name = "sampleid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "sample")
    private Set<Sample> samples = new HashSet<>();

    private LocalDateTime startDateTime = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @NotNull
    @XmlElement
    private StatusEnum status = StatusEnum.DONE;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflowid")
    @XmlIDREF
    private Workflow workflow;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflowtemplatestepid")
    @XmlIDREF
    private WorkflowTemplateStep workflowTemplateStep;

    @ManyToMany
    @JoinTable(name = "workflowstepworkunit", joinColumns = @JoinColumn(name = "workflowstepid"), inverseJoinColumns = @JoinColumn(name = "workunitid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "workunit")
    private Set<Workunit> workunits = new HashSet<>();

    public WorkflowStep() {
        super();
    }

    public WorkflowStepComment getComment() {
        if (comment == null) {
            Optional<WorkflowStepComment> workflowStepComment = getCommentsOrderedByCreated().stream().findFirst();
            if (workflowStepComment.isPresent()) {
                comment = workflowStepComment.get();
            } else {
                comment = new WorkflowStepComment(this);
                getComments().add(comment);
            }
        }
        return comment;
    }

    @Override
    public CommentDiscriminator getCommentDiscriminator() {
        return CommentDiscriminator.WORKFLOW_STEP_COMMENT;
    }

    public Set<WorkflowStepComment> getComments() {
        return comments;
    }

    public Set<WorkflowStepComment> getCommentsOrderedByCreated() {
        return commentsOrderedByCreated;
    }

    public Set<Dataset> getDatasets() {
        return datasets;
    }

    public List<Dataset> getDatasetsAsList() {
        return CollectionHelper.asList(datasets);
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.CONTAINERMANAGER;
    }

    @Override
    public String getDisplayName() {
        return getId() + " - " + getWorkflowTemplateStep().getWorkflowTemplate().getName() + " - " + getWorkflowTemplateStep().getName();
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    @XmlElement(name = "enddate")
    public String getEndDateTimeAsText() {
        return Constants.DATETIME_FORMATTER.format(getEndDateTime());
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getWorkflowTemplateStep() != null) {
            addEntityInfoItem(summary, "workflowTemplateStep", getWorkflowTemplateStep().getName());
            if (getWorkflowTemplateStep().getWorkflowTemplate() != null) {
                addEntityInfoItem(summary, "workflowTemplate", getWorkflowTemplateStep().getWorkflowTemplate().getName());
            }
        }
        if (getWorkflow() != null) {
            addEntityInfoItem(summary, "workflow", getWorkflow().getDisplayName());
        }
        if (getStatus() != null) {
            addEntityInfoItem(summary, "status", getStatus());
        }
        if (getStartDateTime() != null) {
            addEntityInfoItem(summary, "startDateTime", getStartDateTimeAsText());
        }
        if (getEndDateTime() != null) {
            addEntityInfoItem(summary, "endDateTime", DateUtils.getDateAsFormattedString(getEndDateTime()));
        }
        if (getExpectedDuration() != null) {
            addEntityInfoItem(summary, "expectedDuration", getExpectedDuration());
        }
        if (getSamplePreparationProtocol() != null) {
            addEntityInfoItem(summary, "samplePreparationProtocol", getSamplePreparationProtocol().getName());
        }
        addEntityInfoItems(summary, getCustomAttributes());
        return summary.toString();
    }

    public BigDecimal getExpectedDuration() {
        return expectedDuration;
    }

    public String getFullDisplayName() {
        return getDisplayName() + " - " + getCreatedBy() + " - " + getCreatedFormattedAsDateString();
    }

    public String getName() {
        return getWorkflowTemplateStep().getName();
    }

    @Override
    public AbstractBaseEntity getParent() {
        return getWorkflow();
    }

    @Override
    public String getParentClassName() {
        return getParent() != null ? getParent().getTrimmedClassName() : null;
    }

    @Override
    public Long getParentId() {
        return getParent() != null ? getParent().getId() : null;
    }

    @Override
    public String getParentUrlShowScreen() {
        return getParent() != null ? getParent().getUrlShowScreen() : null;
    }

    public Set<Plate> getPlates() {
        return plates;
    }

    public List<Plate> getPlatesAsList() {
        return CollectionHelper.asList(plates);
    }

    @Override
    public String getRelativeRepositoryPath() {
        return getWorkflow().getContainer().getRelativeRepositoryPath();
    }

    public SamplePreparationProtocol getSamplePreparationProtocol() {
        return samplePreparationProtocol;
    }

    public Set<Sample> getSamples() {
        return samples;
    }

    public List<Sample> getSamplesAsList() {
        return CollectionHelper.asList(samples);
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    @XmlElement(name = "startdate")
    public String getStartDateTimeAsText() {
        return Constants.DATETIME_FORMATTER.format(getStartDateTime());
    }

    public StatusEnum getStatus() {
        return status;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public WorkflowTemplateStep getWorkflowTemplateStep() {
        return workflowTemplateStep;
    }

    public Set<Workunit> getWorkunits() {
        return workunits;
    }

    public List<Workunit> getWorkunitsAsList() {
        return CollectionHelper.asList(workunits);
    }

    @Override
    public boolean isCreatable() {
        return getWorkflow() != null && getWorkflow().isUpdatable();
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER);
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public void setComment(WorkflowStepComment comment) {
        this.comment = comment;
    }

    public void setDatasets(Set<Dataset> datasets) {
        this.datasets = datasets;
    }

    public void setDatasetsAsList(List<Dataset> datasets) {
        this.datasets = (Set<Dataset>) CollectionHelper.asSet(datasets);
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public void setExpectedDuration(BigDecimal expectedDuration) {
        this.expectedDuration = expectedDuration;
    }

    @Override
    public void setParent(AbstractEntity parent) {
        setWorkflow((Workflow) parent);
    }

    public void setPlates(Set<Plate> plates) {
        this.plates = plates;
    }

    public void setPlatesAsList(List<Plate> plates) {
        this.plates = (Set<Plate>) CollectionHelper.asSet(plates);
    }

    public void setSamplePreparationProtocol(SamplePreparationProtocol samplePreparationProtocol) {
        this.samplePreparationProtocol = samplePreparationProtocol;
    }

    public void setSamples(Set<Sample> samples) {
        this.samples = samples;
    }

    public void setSamplesAsList(List<Sample> samples) {
        this.samples = (Set<Sample>) CollectionHelper.asSet(samples);
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public void setWorkflowTemplateStep(WorkflowTemplateStep workflowTemplateStep) {
        this.workflowTemplateStep = workflowTemplateStep;
        if (workflowTemplateStep != null && getExpectedDuration() == null) {
            setExpectedDuration(workflowTemplateStep.getExpectedDuration());
        }
    }

    public void setWorkunits(Set<Workunit> workunits) {
        this.workunits = workunits;
    }

    public void setWorkunitsAsList(List<Workunit> workunits) {
        this.workunits = (Set<Workunit>) CollectionHelper.asSet(workunits);
    }

    public void workflowTemplateStepChangeListener(ValueChangeEvent event) {
        if (event.getNewValue() != null) {
            WorkflowTemplateStep newWorkflowTemplateStep = (WorkflowTemplateStep) event.getNewValue();
            setExpectedDuration(newWorkflowTemplateStep.getExpectedDuration());
            setSamplePreparationProtocol(newWorkflowTemplateStep.getSamplePreparationProtocol());
        }
    }
}
