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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(name = "workflowtemplate_name_unique", columnNames = { "name" }) })
@XmlRootElement
public class WorkflowTemplate extends AbstractEnabledBaseEntity implements ShowScreen {

    private static final long serialVersionUID = 1;

    @ManyToMany
    @JoinTable(name = "workflowtemplateservicetype", joinColumns = @JoinColumn(name = "workflowtemplateid"), inverseJoinColumns = @JoinColumn(name = "servicetypeid"))
    @OrderBy("name")
    @NotNull
    @XmlIDREF
    @XmlElement(name = "servicetype")
    private Set<ServiceType> serviceTypes = new HashSet<>();

    @OneToMany(mappedBy = "workflowTemplate", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE })
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<WorkflowTemplateStep> workflowTemplateSteps = new HashSet<>();

    @OneToMany(mappedBy = "workflowTemplate")
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<WorkflowTemplateStep> workflowTemplateStepsReverseOrdered = new HashSet<>();

    @OneToMany(mappedBy = "workflowTemplate", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<Workflow> workflows = new HashSet<>();

    public WorkflowTemplate() {
        super();
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.CONTAINERMANAGER;
    }

    @Override
    @Size(max = 512)
    public String getDescription() {
        return super.getDescription();
    }

    @Override
    public String getEntitySpecifics() {
        final StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getWorkflowTemplateSteps() != null && !getWorkflowTemplateSteps().isEmpty()) {
            addEntityInfoItem(summary, "workflowTemplateSteps", getWorkflowTemplateSteps().size());
        }
        return summary.toString();
    }

    public WorkflowTemplateStep getLastWorkflowTemplateStep() {
        if (!workflowTemplateStepsReverseOrdered.isEmpty()) {
            return workflowTemplateStepsReverseOrdered.stream().findFirst().get();
        }
        return null;
    }

    public Set<ServiceType> getServiceTypes() {
        return serviceTypes;
    }

    public List<ServiceType> getServiceTypesAsList() {
        return new ArrayList<>(getServiceTypes());
    }

    public Set<WorkflowTemplateStep> getWorkflowTemplateSteps() {
        return workflowTemplateSteps;
    }

    public Set<Workflow> getWorkflows() {
        return workflows;
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getWorkflows().isEmpty();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER);
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable();
    }

    public void setServiceTypes(Set<ServiceType> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    public void setServiceTypesAsList(List<ServiceType> serviceTypes) {
        this.serviceTypes = new HashSet<>(serviceTypes);
    }

    public void setWorkflowTemplateSteps(Set<WorkflowTemplateStep> workflowTemplateSteps) {
        this.workflowTemplateSteps = workflowTemplateSteps;
    }

    public void setWorkflows(Set<Workflow> workflows) {
        this.workflows = workflows;
    }
}
