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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.Constants;
import org.bfabric.Messages;
import org.bfabric.entity.Application;
import org.bfabric.entity.Container;
import org.bfabric.entity.Dataset;
import org.bfabric.entity.ImportResource;
import org.bfabric.entity.Job;
import org.bfabric.entity.Pageflow;
import org.bfabric.entity.PageflowStep;
import org.bfabric.entity.Resource;
import org.bfabric.entity.Workflow;
import org.bfabric.entity.WorkflowStep;
import org.bfabric.entity.Workunit;
import org.bfabric.enums.StatusEnum;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.list.PageflowList;
import org.bfabric.list.PageflowStepList;
import org.bfabric.service.ExternalJobService;
import org.bfabric.service.JobService;
import org.bfabric.service.WorkflowService;
import org.bfabric.service.WorkflowStepService;
import org.bfabric.service.WorkunitService;
import org.bfabric.util.FileUploadHelper;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class WorkunitManager extends AbstractContainerDependentEntityManager<Workunit> {

    private static final long serialVersionUID = 1;

    private static final Logger logger = Logger.getLogger(WorkunitManager.class.getName());

    @Inject
    WorkflowService workflowService;

    @Inject
    WorkflowStepService workflowStepService;

    @Param
    private Long applicationId;

    @Param
    private String applicationType;

    @Param
    private String creationType;

    @Inject
    private ExternalJobService externalJobService;

    @Inject
    private FileUploadHelper fileUploadHelper;

    @Inject
    private JobService jobService;

    private Pageflow pageflow = null;

    @Inject
    private PageflowList pageflowList;

    private PageflowStep pageflowStep = null;

    @Inject
    private PageflowStepList pageflowStepList;

    @Inject
    private ResourceHelper resourceHelper;

    @Param
    private Long selectedDatasetId;

    @Param
    private Long selectedWorkunitId;

    private Container workunitContainer;

    @Inject
    private WorkunitService workunitService;

    public WorkunitManager() {
        super(Workunit.class);
        setWorkunitContainer();
    }

    public String assignSamples() {
        workunitService.assignSamples(getWorkunit());
        getFacesMessagesManager().bufferWarningClear(Messages.get("successfullyAssigned"));
        return getRedirectURLAfterSave();
    }

    public void back() {
        if (getPageflow() != null) {
            PageflowStep previousPageflowStep = getPageflow().getPreviousPageflowStep(getPageflowStep());
            if (previousPageflowStep != null) {
                setPageflowStep(previousPageflowStep);
            }
        }
    }

    public String cancelUnarchive() {
        Job job = getWorkunit().getLastUnarchiveJob();
        if (job != null) {
            job.setStatus(StatusEnum.CANCELED);
            jobService.save(job);
            getFacesMessagesManager().bufferWarningClear(Messages.get("unarchiveCanceled"));
        }
        return getShowScreenRedirectURL("details");
    }

    @Override
    protected Workunit createInstance() {
        Workunit workunit = null;
        setWorkunitContainer();
        if (getApplicationId() != null) {
            final Application application = entityService.find(Application.class, getApplicationId());
            try {
                switch (getCreationType()) {
                case Constants.CREATION_FROM_SCRATCH:
                    workunit = new Workunit(application, workunitContainer);
                    initPageflow(application.getPageflow(), null);
                    break;
                case Constants.CREATION_FROM_SELECTED_WORKUNIT:
                    workunit = new Workunit(application, workunitContainer);
                    getSelectedInputResources().addAll(((Workunit) entityService.fetch(Workunit.class, getSelectedWorkunitId())).getResources());
                    initPageflow(application.getPageflow(), getPageflowStepEditWorkunit());
                    break;
                case Constants.CREATION_FROM_INPUT_DATASET:
                    workunit = new Workunit(application, (Dataset) entityService.fetch(Dataset.class, getSelectedDatasetId()), workunitContainer);
                    initPageflow(application.getPageflow(), getPageflowStepEditWorkunit());
                    break;
                case Constants.CREATION_FROM_INPUT_RESOURCES:
                    if (FacesContext.getCurrentInstance().getExternalContext().getFlash().containsKey("selectedResources")) {
                        workunit = new Workunit(application, workunitContainer);
                    }
                    getSelectedInputResources().addAll((Set<Resource>) FacesContext.getCurrentInstance().getExternalContext().getFlash().get("selectedResources"));
                    for (final Resource resource : getSelectedInputResources()) {
                        resource.uncheck();
                    }
                    initPageflow(application.getPageflow(), null);
                    break;
                default:
                    break;
                }
            } catch (final InvalidDataException ide) {
                logger.severe(ide.getMessage());
            }
        }

        return workunit;
    }

    public Application getApplication() {
        return getWorkunit().getApplication();
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public String getCreationType() {
        return creationType;
    }

    public Set<ImportResource> getMarkedImportResources() {
        return resourceHelper.getMarkedImportResources();
    }

    public Set<Resource> getMarkedInputResources() {
        return resourceHelper.getMarkedInputResources();
    }

    public Pageflow getPageflow() {
        return pageflow;
    }

    public Pageflow getPageflowDataset() {
        return pageflowList.getPageflowByName(Constants.DATASET);
    }

    public PageflowStep getPageflowStep() {
        return pageflowStep;
    }

    public PageflowStep getPageflowStepEditWorkunit() {
        return pageflowStepList.getPageflowStepByName("editWorkunit");
    }

    public PageflowStep getPageflowStepImportResourcesAssignSamples() {
        return pageflowStepList.getPageflowStepByName("importResourcesAssignSamples");
    }

    public PageflowStep getPageflowStepImportResourcesSelect() {
        return pageflowStepList.getPageflowStepByName("importResourcesSelect");
    }

    public PageflowStep getPageflowStepImportWorkunitUpload() {
        return pageflowStepList.getPageflowStepByName("importWorkunitUpload");
    }

    public PageflowStep getPageflowStepSelectDataset() {
        return pageflowStepList.getPageflowStepByName("selectDataset");
    }

    public PageflowStep getPageflowStepSelectResources() {
        return pageflowStepList.getPageflowStepByName("selectResources");
    }

    @Override
    public String getRedirectURLAfterCancelCreated() {
        if (getSelectedWorkunitId() != null) {
            return createRedirectShowScreenURL(Workunit.class.getSimpleName(), getSelectedWorkunitId(), "applications", null);
        }
        if (getSelectedDatasetId() != null) {
            return createRedirectShowScreenURL(Dataset.class.getSimpleName(), getSelectedDatasetId(), "applications", null);
        }
        if (Constants.CREATION_FROM_INPUT_RESOURCES.equals(getCreationType())) {
            if (getCurrentUser().getSelectedResourceBasket().getId() > 0) {
                return createRedirectShowScreenURL(getCurrentUser().getSelectedResourceBasket());
            }
            getSessionManager().redirectToContainer(getContextContainer().getId());
        }
        final HashMap<String, String> fParams = new HashMap<>();
        fParams.put("applicationType", getApplicationType());
        return createRedirectURL("application/run", null, null, fParams);
    }

    public Long getSelectedDatasetId() {
        return selectedDatasetId;
    }

    public Set<ImportResource> getSelectedImportResources() {
        return resourceHelper.getSelectedImportResources();
    }

    public Set<Resource> getSelectedInputResources() {
        return resourceHelper.getSelectedInputResources();
    }

    public Long getSelectedWorkunitId() {
        return selectedWorkunitId;
    }

    public List<WorkflowStep> getWorkflowSteps(String filterString) {
        return workflowStepService.getWorkflowStepsFilteredExcluding(filterString, getInstance().getWorkflowSteps());
    }

    public List<Workflow> getWorkflows(String filterString) {
        return workflowService.getWorkflowsFilteredExcluding(filterString, getInstance().getWorkflows());
    }

    @Produces
    @Named("workunit")
    public Workunit getWorkunit() {
        return getInstance();
    }

    private void initPageflow(Pageflow pageflow, PageflowStep pageflowStep) {
        if (pageflow != null) {
            setPageflow(pageflow);
            if (pageflowStep != null) {
                setPageflowStep(pageflowStep);
            } else {
                setPageflowStep(pageflow.getFirstPageflowStep());
            }
        } else {
            getSessionManager().redirectToContainer(getContextContainer().getId());
        }
    }

    public boolean isMovable() {
        return !isManaged();
    }

    public boolean isRerunnable() {
        return getWorkunit() != null && getWorkunit().isRerunnable() && getPageflow() == null;
    }

    @Override
    public Workunit loadInstance() {
        if (getCreationType() != null && (getCreationType().equals(Constants.CREATION_FROM_INPUT_DATASET) || getCreationType().equals(Constants.CREATION_FROM_RERUNNING))) {
            final Workunit templateWorkunit = super.loadInstance();
            setWorkunitContainer();
            final Workunit workunit = new Workunit(templateWorkunit, workunitContainer);
            if (getCreationType().equals(Constants.CREATION_FROM_RERUNNING)) {
                getSelectedInputResources().addAll(templateWorkunit.getInputResources());
            }
            initPageflow(workunit.getApplication().getPageflow(), getPageflowStepEditWorkunit());
            return workunit;
        }
        return super.loadInstance();
    }

    public String markDeleted() {
        workunitService.markDeleted(getWorkunit());
        return getShowScreenRedirectURL("details");
    }

    public void navigateSelect(Dataset inputDataset) {
        if (getPageflow() != null) {
            if (getPageflow().getName().equals(getPageflowDataset().getName())) {
                setPageflowStep(getPageflow().getNextPageflowStep(getPageflowStep()));
            } else {
                getSessionManager().redirectToContainer(getContextContainer().getId());
            }
            getWorkunit().setInputDataset(inputDataset);
        }
    }

    public void next() {
        if (getPageflow() != null) {
            PageflowStep nextPageflowStep = getPageflow().getNextPageflowStep(getPageflowStep());
            if (nextPageflowStep != null) {
                setPageflowStep(nextPageflowStep);
            } else {
                getSessionManager().redirectToContainer(getContextContainer().getId());
            }
        }
    }

    public String resubmit() {
        externalJobService.resubmit(getWorkunit());
        return getShowScreenRedirectURL();
    }

    @Override
    public String save() {
        LinkedHashMap<String, String> validationErrorMsg = workunitService.isValid(getWorkunit(), fileUploadHelper.getUploadedFiles());
        if (validationErrorMsg.isEmpty()) {
            setCreated(!isManaged());
            workunitService.save(getWorkunit(), null, getSelectedInputResources(), getSelectedImportResources(), fileUploadHelper.getUploadedFiles(), true);
            return postSave(true, false);
        }

        handleValidationErrors(validationErrorMsg);
        fileUploadHelper.clearAllUploadData();
        return null;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public void setPageflow(Pageflow pageflow) {
        this.pageflow = pageflow;
    }

    public void setPageflowStep(PageflowStep pageflowStep) {
        this.pageflowStep = pageflowStep;
    }

    public void setWorkunitContainer() {
        workunitContainer = getContextContainer();
        if (workunitContainer != null && !workunitContainer.isExtensible()) {
            if (workunitContainer.isWorkunitCreatable()) {
                workunitContainer = workunitContainer.getProject();
            } else {
                workunitContainer = null;
            }
        }
    }

    public String unarchive() {
        workunitService.unarchive(getWorkunit(), getCurrentUser());
        getFacesMessagesManager().bufferWarningClear(Messages.get("unarchiveRequested"));
        return getShowScreenRedirectURL("details");
    }
}