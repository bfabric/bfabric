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

package org.bfabric.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.Container;
import org.bfabric.entity.Dataset;
import org.bfabric.entity.OrderItem;
import org.bfabric.entity.Plate;
import org.bfabric.entity.Sample;
import org.bfabric.entity.Workflow;
import org.bfabric.entity.WorkflowTemplate;
import org.bfabric.entity.Workunit;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.DatasetService;
import org.bfabric.service.PlateService;
import org.bfabric.service.SampleService;
import org.bfabric.service.WorkflowService;
import org.bfabric.service.WorkflowTemplateService;
import org.bfabric.service.WorkunitService;
import org.omnifaces.cdi.Param;
import org.primefaces.model.DualListModel;

@MeasureCalls
@Named
@ViewScoped
public class WorkflowManager extends AbstractEntityManager<Workflow> {

    private static final long serialVersionUID = 1;

    @Param
    private Long containerId;

    @Inject
    private DatasetService datasetService;

    private Container lastContainer;

    private DualListModel<OrderItem> orderItemsModel;

    @Inject
    private PlateService plateService;

    @Inject
    private SampleService sampleService;

    private Set<Dataset> selectedDatasets = new HashSet<>();

    private Set<Plate> selectedPlates = new HashSet<>();

    private Set<Sample> selectedSamples = new HashSet<>();

    private Set<Workunit> selectedWorkunits = new HashSet<>();

    @Inject
    private WorkflowService workflowService;

    @Inject
    private WorkflowTemplateService workflowTemplateService;

    @Inject
    private WorkunitService workunitService;

    public WorkflowManager() {
        super(Workflow.class);
    }

    public void addPlateToSelection(Plate plate) {
        getSelectedPlates().add(plate);
    }

    public void addSampleToSelection(Sample sample) {
        getSelectedSamples().add(sample);
    }

    public void addWorkunitToSelection(Workunit workunit) {
        getSelectedWorkunits().add(workunit);
    }

    public String assignDatasets() {
        workflowService.assignDatasets(getWorkflow(), getSelectedDatasets());
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyUpdated"));
        return getShowScreenRedirectURL("datasets");
    }

    public String assignPlates() {
        workflowService.assignPlates(getWorkflow(), getSelectedPlates());
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyUpdated"));
        return getShowScreenRedirectURL("plates");
    }

    public String assignSamples() {
        workflowService.assignSamples(getWorkflow(), getSelectedSamples());
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyUpdated"));
        return getShowScreenRedirectURL("samples");
    }

    public String assignWorkunits() {
        workflowService.assignWorkunits(getWorkflow(), getSelectedWorkunits());
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyUpdated"));
        return getShowScreenRedirectURL("workunits");
    }

    @Override
    protected Workflow createInstance() {
        final Workflow workflow = super.createInstance();
        if (getContainerId() != null && getContainerId() > 0) {
            workflow.setContainer(entityService.find(Container.class, getContainerId()));
        }
        return workflow;
    }

    public Long getContainerId() {
        return containerId;
    }

    public List<Dataset> getDatasets(String filterString) {
        return datasetService.getDatasetsFilteredByContainersExcluding(filterString, getSelectedContainers(), getInstance().getDatasets());
    }

    public List<WorkflowTemplate> getEnabledWorkflowTemplatesByContainer(String filterString) {
        return workflowTemplateService.getEnabledWorkflowTemplatesByContainerIncludingFiltered(filterString, getWorkflow().getWorkflowTemplate(), getWorkflow().getContainer());
    }

    public Container getLastContainer() {
        return lastContainer;
    }

    public DualListModel<OrderItem> getOrderItemsModel() {
        if (orderItemsModel == null) {
            // Initialize the source list and remove entities already contained in the target list to prevent duplicates.
            final ArrayList<OrderItem> sourceList = new ArrayList<>();
            if (getWorkflow().getContainer().getOrderItems() != null && !getWorkflow().getContainer().getOrderItems().isEmpty()) {
                sourceList.addAll(getWorkflow().getContainer().getOrderItems());
            }
            if (getWorkflow().getOrderItems() != null && !getWorkflow().getOrderItems().isEmpty()) {
                sourceList.removeAll(getWorkflow().getOrderItems());
            }
            // Initialize target list.
            final ArrayList<OrderItem> targetList = new ArrayList<>();
            if (getWorkflow().getOrderItems() != null && !getWorkflow().getOrderItems().isEmpty()) {
                targetList.addAll(getWorkflow().getOrderItems());
            }
            // Initialize the DualListModel.
            orderItemsModel = new DualListModel<>(sourceList, targetList);
        }
        return orderItemsModel;
    }

    public List<Plate> getPlates(String filterString) {
        return plateService.getPlatesFilteredByContainersExcluding(filterString, getInstance().getPlates());
    }

    @Override
    public String getRedirectURLAfterRemove() {
        if (getRefererURL() != null && getRefererURL().contains("workflow/list")) {
            return getRedirectURLFromRefererUrl();
        }
        return createRedirectShowScreenURL(getLastContainer(), "workflows", null);
    }

    public List<Sample> getSamples(String filterString) {
        return sampleService.getSamplesFilteredByContainersExcluding(filterString, getSelectedContainersAsList(), getInstance().getSamples());
    }

    public Set<Dataset> getSelectedDatasets() {
        return selectedDatasets;
    }

    public Set<Plate> getSelectedPlates() {
        return selectedPlates;
    }

    public Set<Sample> getSelectedSamples() {
        return selectedSamples;
    }

    public Set<Workunit> getSelectedWorkunits() {
        return selectedWorkunits;
    }

    @Produces
    @Named("workflow")
    public Workflow getWorkflow() {
        return getInstance();
    }

    public List<Workunit> getWorkunits(String filterString) {
        return workunitService.getWorkunitsFilteredByContainersExcluding(filterString, getSelectedContainersAsList(), getInstance().getWorkunits());
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (getWorkflow() != null) {
            setLastContainer(getWorkflow().getContainer());
            getSelectedDatasets().addAll(getWorkflow().getDatasets());
            getSelectedSamples().addAll(getWorkflow().getSamples());
            getSelectedWorkunits().addAll(getWorkflow().getWorkunits());
            getSelectedPlates().addAll(getWorkflow().getPlates());
        }
    }

    @Override
    public String save() {
        if (orderItemsModel != null) {
            getWorkflow().setOrderItems(new HashSet<>(orderItemsModel.getTarget()));
        }
        return super.save();
    }

    public void setLastContainer(Container lastContainer) {
        this.lastContainer = lastContainer;
    }

    public void setOrderItemsModel(DualListModel<OrderItem> orderItemsModel) {
        this.orderItemsModel = orderItemsModel;
    }

    public void setSelectedDatasets(Set<Dataset> selectedDatasets) {
        this.selectedDatasets = selectedDatasets;
    }

    public void setSelectedPlates(Set<Plate> selectedPlates) {
        this.selectedPlates = selectedPlates;
    }

    public void setSelectedSamples(Set<Sample> selectedSamples) {
        this.selectedSamples = selectedSamples;
    }

    public void setSelectedWorkunits(Set<Workunit> selectedWorkunits) {
        this.selectedWorkunits = selectedWorkunits;
    }
}
