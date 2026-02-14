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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ParentDependent;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
public class WorkflowTemplateStep extends AbstractOrderedEnabledNamedBaseEntity implements ShowScreen, ParentDependent {

    private static final long serialVersionUID = 1;

    @NotNull
    @DecimalMin("0")
    @Digits(integer = 10, fraction = 2)
    @XmlElement
    private BigDecimal expectedDuration = BigDecimal.ZERO;

    @Min(0)
    @XmlElement
    private int runsPerProcess = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "samplepreparationprotocolid")
    @XmlIDREF
    private SamplePreparationProtocol samplePreparationProtocol;

    @Min(0)
    @XmlElement
    private int samplesPerProcess = 0;

    @OneToMany(mappedBy = "workflowTemplateStep", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<WorkflowStep> workflowSteps = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflowtemplateid")
    @NotNull
    @XmlIDREF
    private WorkflowTemplate workflowTemplate;

    public WorkflowTemplateStep() {
        super();
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.CONTAINERMANAGER;
    }

    @Override
    @NotBlank
    @Size(max = 1024)
    public String getDescription() {
        return super.getDescription();
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getWorkflowTemplate() != null) {
            addEntityInfoItem(summary, "workflowTemplate", getWorkflowTemplate().getName());
        }
        if (getExpectedDuration() != null) {
            addEntityInfoItem(summary, "expectedDuration", getExpectedDuration());
        }
        addEntityInfoItem(summary, "runsPerProcess", getRunsPerProcess());
        addEntityInfoItem(summary, "samplesPerProcess", getSamplesPerProcess());
        if (getSamplePreparationProtocol() != null) {
            addEntityInfoItem(summary, "samplePreparationProtocol", getSamplePreparationProtocol().getName());
        }
        return summary.toString();
    }

    public BigDecimal getExpectedDuration() {
        return expectedDuration;
    }

    @Override
    public AbstractBaseEntity getParent() {
        return getWorkflowTemplate();
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

    public int getRunsPerProcess() {
        return runsPerProcess;
    }

    public SamplePreparationProtocol getSamplePreparationProtocol() {
        return samplePreparationProtocol;
    }

    public int getSamplesPerProcess() {
        return samplesPerProcess;
    }

    public Set<WorkflowStep> getWorkflowSteps() {
        return workflowSteps;
    }

    public WorkflowTemplate getWorkflowTemplate() {
        return workflowTemplate;
    }

    @Override
    public boolean isDeletable() {
        // Can be deleted if it is updatable and not used by any workflow step.
        return isUpdatable() && getWorkflowSteps().isEmpty();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER);
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public void setExpectedDuration(BigDecimal expectedDuration) {
        this.expectedDuration = expectedDuration;
    }

    @Override
    public void setParent(AbstractEntity parent) {
        setWorkflowTemplate((WorkflowTemplate) parent);
    }

    public void setRunsPerProcess(int runsPerProcess) {
        this.runsPerProcess = runsPerProcess;
    }

    public void setSamplePreparationProtocol(SamplePreparationProtocol samplePreparationProtocol) {
        this.samplePreparationProtocol = samplePreparationProtocol;
    }

    public void setSamplesPerProcess(int samplesPerProcess) {
        this.samplesPerProcess = samplesPerProcess;
    }

    public void setWorkflowSteps(Set<WorkflowStep> workflowSteps) {
        this.workflowSteps = workflowSteps;
    }

    public void setWorkflowTemplate(WorkflowTemplate workflowTemplate) {
        this.workflowTemplate = workflowTemplate;
    }
}
