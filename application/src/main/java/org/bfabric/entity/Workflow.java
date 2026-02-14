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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.Messages;
import org.bfabric.entity.api.ContainerDependent;
import org.bfabric.entity.api.ShowScreen;
import org.bfabric.enums.RoleEnum;
import org.bfabric.util.CollectionHelper;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@XmlRootElement
@NamedQuery(name = "Workflow.findByCreatedByOrderById", query = "SELECT a FROM Workflow a WHERE a.createdBy = :createdBy ORDER BY a.id DESC")
public class Workflow extends AbstractBaseEntity implements ContainerDependent, ShowScreen {

    private static final long serialVersionUID = 1;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "containerid")
    @XmlIDREF
    private Container container;

    @ManyToMany
    @JoinTable(name = "workflowdataset", joinColumns = @JoinColumn(name = "workflowid"), inverseJoinColumns = @JoinColumn(name = "datasetid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "dataset")
    private Set<Dataset> datasets = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "workfloworderitem", joinColumns = @JoinColumn(name = "workflowid"), inverseJoinColumns = @JoinColumn(name = "orderitemid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<OrderItem> orderItems = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "workflowplate", joinColumns = @JoinColumn(name = "workflowid"), inverseJoinColumns = @JoinColumn(name = "plateid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "plate")
    private Set<Plate> plates = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "workflowsample", joinColumns = @JoinColumn(name = "workflowid"), inverseJoinColumns = @JoinColumn(name = "sampleid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "sample")
    private Set<Sample> samples = new HashSet<>();

    @OneToMany(mappedBy = "workflow", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE })
    @OrderBy("id")
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<WorkflowStep> workflowSteps = new HashSet<>();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflowtemplateid")
    @XmlIDREF
    private WorkflowTemplate workflowTemplate;

    @ManyToMany
    @JoinTable(name = "workflowworkunit", joinColumns = @JoinColumn(name = "workflowid"), inverseJoinColumns = @JoinColumn(name = "workunitid"))
    @OrderBy("id desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @XmlIDREF
    @XmlElement(name = "workunit")
    private Set<Workunit> workunits = new HashSet<>();

    public Workflow() {
        super();
    }

    @Override
    public Container getContainer() {
        return container;
    }

    public Set<Dataset> getDatasets() {
        return datasets;
    }

    public List<Dataset> getDatasetsAsList() {
        return CollectionHelper.asList(datasets);
    }

    public List<Dataset> getDatasetsTransitive() {
        Set<Dataset> datasetsTransitive = new HashSet<>();
        if (!getDatasets().isEmpty()) {
            datasetsTransitive.addAll(getDatasets());
        }
        if (!getWorkflowSteps().isEmpty()) {
            for (WorkflowStep workflowStep : getWorkflowSteps()) {
                if (!workflowStep.getDatasets().isEmpty()) {
                    datasetsTransitive.addAll(workflowStep.getDatasets());
                }
            }
        }
        return CollectionHelper.sortObjects(datasetsTransitive);
    }

    @Override
    public RoleEnum getDefaultRequiredRole() {
        return RoleEnum.CONTAINERMANAGER;
    }

    @Override
    public String getDisplayName() {
        return getId() + " - " + getWorkflowTemplate().getName();
    }

    @Override
    public String getEntitySpecifics() {
        StringBuilder summary = new StringBuilder(super.getEntitySpecifics());
        if (getWorkflowTemplate() != null) {
            addEntityInfoItem(summary, "workflowTemplate", getWorkflowTemplate().getName());
        }
        if (getWorkflowSteps() != null && !getWorkflowSteps().isEmpty()) {
            addEntityInfoItem(summary, "workflowSteps", getWorkflowSteps().size());
        }
        return summary.toString();
    }

    public String getNotAllOrderItemsSelectedHint() {
        return getOrderItems().size() != container.getOrderItems().size() ? Messages.get("notAllOrderItemsSelectedHint").replace("{0}", String.valueOf(getOrderItems().size()))
            .replace("{1}", String.valueOf(container.getOrderItems().size())) : null;
    }

    public Set<OrderItem> getOrderItems() {
        return orderItems;
    }

    public String getOrderItemsAsString() {
        return CollectionHelper.printNames(getOrderItems());
    }

    public Set<Plate> getPlates() {
        return plates;
    }

    public List<Plate> getPlatesAsList() {
        return CollectionHelper.asList(plates);
    }

    public List<Plate> getPlatesTransitive() {
        Set<Plate> platesTransitive = new HashSet<>();
        if (!getPlates().isEmpty()) {
            platesTransitive.addAll(getPlates());
        }
        if (!getWorkflowSteps().isEmpty()) {
            for (WorkflowStep workflowStep : getWorkflowSteps()) {
                if (!workflowStep.getPlates().isEmpty()) {
                    platesTransitive.addAll(workflowStep.getPlates());
                }
            }
        }
        return CollectionHelper.sortObjects(platesTransitive);
    }

    public Set<Sample> getSamples() {
        return samples;
    }

    public List<Sample> getSamplesAsList() {
        return CollectionHelper.asList(samples);
    }

    public List<Sample> getSamplesTransitive() {
        Set<Sample> samplesTransitive = new HashSet<>();
        if (!getSamples().isEmpty()) {
            samplesTransitive.addAll(getSamples());
        }
        if (!getWorkflowSteps().isEmpty()) {
            for (WorkflowStep workflowStep : getWorkflowSteps()) {
                if (!workflowStep.getSamples().isEmpty()) {
                    samplesTransitive.addAll(workflowStep.getSamples());
                }
            }
        }
        return CollectionHelper.sortObjects(samplesTransitive);
    }

    public Set<WorkflowStep> getWorkflowSteps() {
        return workflowSteps;
    }

    public WorkflowTemplate getWorkflowTemplate() {
        return workflowTemplate;
    }

    public Set<Workunit> getWorkunits() {
        return workunits;
    }

    public List<Workunit> getWorkunitsAsList() {
        return CollectionHelper.asList(workunits);
    }

    public List<Workunit> getWorkunitsTransitive() {
        Set<Workunit> workunitsTransitive = new HashSet<>();
        if (!getWorkunits().isEmpty()) {
            workunitsTransitive.addAll(getWorkunits());
        }
        if (!getWorkflowSteps().isEmpty()) {
            for (WorkflowStep workflowStep : getWorkflowSteps()) {
                if (!workflowStep.getWorkunits().isEmpty()) {
                    workunitsTransitive.addAll(workflowStep.getWorkunits());
                }
            }
        }
        return CollectionHelper.sortObjects(workunitsTransitive);
    }

    @Override
    public boolean isDeletable() {
        return isUpdatable() && getDatasets().isEmpty() && getSamples().isEmpty() && getWorkunits().isEmpty() && getPlates().isEmpty();
    }

    @Override
    public boolean isReadable() {
        return hasCurrentUserRoleEnum(getDefaultRequiredRole()) || hasCurrentUserRoleEnum(RoleEnum.CONTAINERREADER);
    }

    @Override
    public boolean isUpdatable() {
        return isCreatable() && getContainer().isUpdatable();
    }

    @Override
    public void setContainer(Container container) {
        this.container = container;
    }

    public void setDatasets(Set<Dataset> datasets) {
        this.datasets = datasets;
    }

    public void setDatasetsAsList(List<Dataset> datasets) {
        this.datasets = (Set<Dataset>) CollectionHelper.asSet(datasets);
    }

    public void setOrderItems(Set<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public void setPlates(Set<Plate> plates) {
        this.plates = plates;
    }

    public void setPlatesAsList(List<Plate> plates) {
        this.plates = (Set<Plate>) CollectionHelper.asSet(plates);
    }

    public void setSamples(Set<Sample> samples) {
        this.samples = samples;
    }

    public void setSamplesAsList(List<Sample> samples) {
        this.samples = (Set<Sample>) CollectionHelper.asSet(samples);
    }

    public void setWorkflowSteps(Set<WorkflowStep> workflowSteps) {
        this.workflowSteps = workflowSteps;
    }

    public void setWorkflowTemplate(WorkflowTemplate workflowTemplate) {
        this.workflowTemplate = workflowTemplate;
    }

    public void setWorkunits(Set<Workunit> workunits) {
        this.workunits = workunits;
    }

    public void setWorkunitsAsList(List<Workunit> workunits) {
        this.workunits = (Set<Workunit>) CollectionHelper.asSet(workunits);
    }

}
