/*
 *
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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Messages;
import org.bfabric.entity.Dataset;
import org.bfabric.entity.Plate;
import org.bfabric.entity.Sample;
import org.bfabric.entity.SamplePreparationProtocol;
import org.bfabric.entity.Workflow;
import org.bfabric.entity.WorkflowStep;
import org.bfabric.entity.Workunit;
import org.bfabric.enums.RoleEnum;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.CommentService;
import org.bfabric.service.DatasetService;
import org.bfabric.service.PlateService;
import org.bfabric.service.SamplePreparationProtocolService;
import org.bfabric.service.SampleService;
import org.bfabric.service.WorkflowStepService;
import org.bfabric.service.WorkunitService;
import org.bfabric.util.CollectionHelper;
import org.bfabric.util.FileUploadHelper;
import org.bfabric.util.StringHelper;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class WorkflowStepManager extends AbstractEntityManager<WorkflowStep> {

    private static final long serialVersionUID = 1;

    @Inject
    private CommentService commentService;

    @Inject
    private DatasetService datasetService;

    @Inject
    private FileUploadHelper fileUploadHelper;

    @Inject
    private PlateService plateService;

    @Inject
    private SamplePreparationProtocolService samplePreparationProtocolService;

    @Inject
    private SampleService sampleService;

    private Set<Dataset> selectedDatasets = new HashSet<>();

    private Set<Plate> selectedPlates = new HashSet<>();

    private Set<Sample> selectedSamples = new HashSet<>();

    private Set<Workunit> selectedWorkunits = new HashSet<>();

    @Param
    private Long workflowId;

    @Inject
    private WorkflowStepService workflowStepService;

    @Inject
    private WorkunitService workunitService;

    public WorkflowStepManager() {
        super(WorkflowStep.class);
    }

    public void addDatasetToSelection(Dataset dataset) {
        getSelectedDatasets().add(dataset);
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
        workflowStepService.assignDatasets(getWorkflowStep(), getSelectedDatasets());
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyUpdated"));
        return getShowScreenRedirectURL("datasets");
    }

    public String assignPlates() {
        workflowStepService.assignPlates(getWorkflowStep(), getSelectedPlates());
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyUpdated"));
        return getShowScreenRedirectURL("workunits");
    }

    public String assignSamples() {
        workflowStepService.assignSamples(getWorkflowStep(), getSelectedSamples());
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyUpdated"));
        return getShowScreenRedirectURL("samples");
    }

    public String assignWorkunits() {
        workflowStepService.assignWorkunits(getWorkflowStep(), getSelectedWorkunits());
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyUpdated"));
        return getShowScreenRedirectURL("workunits");
    }

    @Override
    public WorkflowStep createInstance() {
        final WorkflowStep workflowStep = super.createInstance();
        if (getWorkflowId() != null && getWorkflowId() > 0) {
            workflowStep.setWorkflow(entityService.find(Workflow.class, getWorkflowId()));
        }
        if (getIdentityManager().hasCurrentUserRoleEnum(RoleEnum.CONTAINERMANAGER)) {
            workflowStep.setSupervisor(getCurrentUser());
        }
        return workflowStep;
    }

    public List<Dataset> getDatasets(String filterString) {
        return datasetService.getDatasetsFilteredByContainersExcluding(filterString, getSelectedContainers(), getInstance().getDatasets());
    }

    public List<SamplePreparationProtocol> getFilteredEnabledSamplePreparationProtocolsIncluding(String filterString) {
        return samplePreparationProtocolService.getFilteredEnabledSamplePreparationProtocolsIncluding(filterString, getWorkflowStep().getSamplePreparationProtocol());
    }

    public List<Plate> getPlates(String filterString) {
        return plateService.getPlatesFilteredByContainersExcluding(filterString, getInstance().getPlates());
    }

    @Override
    public String getRedirectURLAfterCancelCreated() {
        return createRedirectShowScreenURL(getWorkflowStep().getWorkflow());
    }

    @Override
    public String getRedirectURLAfterRemove() {
        if (getRefererURL() != null && getRefererURL().contains("workflowstep/list")) {
            return getRedirectURLFromRefererUrl();
        }
        return createRedirectShowScreenURL(getWorkflowStep().getWorkflow());
    }

    @Override
    public String getRedirectURLAfterSave() {
        return createRedirectShowScreenURL(getWorkflowStep().getWorkflow());
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

    public Long getWorkflowId() {
        return workflowId;
    }

    @Produces
    @Named("workflowStep")
    public WorkflowStep getWorkflowStep() {
        return getInstance();
    }

    public List<Workunit> getWorkunits(String filterString) {
        return workunitService.getWorkunitsFilteredByContainersExcluding(filterString, getSelectedContainersAsList(), getInstance().getWorkunits());
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
        if (getWorkflowStep() != null) {
            getSelectedDatasets().addAll(getWorkflowStep().getDatasets());
            getSelectedSamples().addAll(getWorkflowStep().getSamples());
            getSelectedWorkunits().addAll(getWorkflowStep().getWorkunits());
            getSelectedPlates().addAll(getWorkflowStep().getPlates());
            if (isManaged()) {
                fileUploadHelper.setInitialAttachments(new HashSet<>(getWorkflowStep().getComment().getAttachments()));
            }
        }
    }

    @Override
    public WorkflowStep loadInstance() {
        final WorkflowStep workflowStep = super.loadInstance();
        setWorkflowId(workflowStep.getWorkflow().getId());
        return workflowStep;
    }

    @Override
    public String save() {
        try {
            LinkedHashMap<String, String> validationErrorMsg = new LinkedHashMap<>();

            if (getWorkflowStep().isSupervisorChanged() && !getWorkflowStep().isSupervisorValid()) {
                validationErrorMsg.put("supervisor", Messages.get("supervisorInvalidHint"));
            }

            if (!getWorkflowStep().getComment().isManaged() && StringHelper.isEmpty(getWorkflowStep().getComment().getComment()) && fileUploadHelper.getUploadedFiles().isEmpty()) {
                // Note: if transient (first) comment is empty, i.e., no comment text as well as no attachments, then do not save the comment!
                getWorkflowStep().getComments().remove(getWorkflowStep().getComment());
                getWorkflowStep().setComment(null);
            } else {
                validationErrorMsg = commentService.isValid(getWorkflowStep().getComment(), fileUploadHelper.getUploadedFiles());
            }

            if (validationErrorMsg.isEmpty()) {
                Set<String> errorMsg = workflowStepService.save(getWorkflowStep(), fileUploadHelper.getUploadedFiles());
                if (errorMsg.isEmpty()) {
                    return postSave(true, false);
                }
                getFacesMessagesManager().bufferWarningClear("WorkflowStep saved but following error(s) occurred: " + CollectionHelper.print(errorMsg));
                return getRedirectURLAfterSave();
            }
            handleValidationErrors(validationErrorMsg);
        } catch (Exception e) {
            e.printStackTrace();
            getFacesMessagesManager().printError(e.getMessage());
        }
        return null;
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

    public void setWorkflowId(Long workflowId) {
        this.workflowId = workflowId;
    }
}
