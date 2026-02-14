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

import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.inject.Produces;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bfabric.entity.SamplePreparationProtocol;
import org.bfabric.entity.WorkflowTemplate;
import org.bfabric.entity.WorkflowTemplateStep;
import org.bfabric.interceptors.MeasureCalls;
import org.bfabric.service.SamplePreparationProtocolService;
import org.omnifaces.cdi.Param;

@MeasureCalls
@Named
@ViewScoped
public class WorkflowTemplateStepManager extends AbstractEntityManager<WorkflowTemplateStep> {

    private static final Logger logger = Logger.getLogger(WorkflowTemplateStepManager.class.getName());

    private static final long serialVersionUID = 1;

    @Inject
    private SamplePreparationProtocolService samplePreparationProtocolService;

    @Param
    private Long workflowTemplateId;

    public WorkflowTemplateStepManager() {
        super(WorkflowTemplateStep.class);
    }

    @Override
    protected WorkflowTemplateStep createInstance() {
        final WorkflowTemplateStep workflowTemplateStep = new WorkflowTemplateStep();
        if (workflowTemplateId != null) {
            final WorkflowTemplate workflowTemplate = entityService.find(WorkflowTemplate.class, workflowTemplateId);
            workflowTemplateStep.setWorkflowTemplate(workflowTemplate);
        }
        return workflowTemplateStep;
    }

    public List<SamplePreparationProtocol> getFilteredEnabledSamplePreparationProtocolsIncluding(String filterString) {
        return samplePreparationProtocolService.getFilteredEnabledSamplePreparationProtocolsIncluding(filterString, getWorkflowTemplateStep().getSamplePreparationProtocol());
    }

    @Override
    public String getRedirectURLAfterSave() {
        return createRedirectShowScreenURL(getWorkflowTemplateStep().getWorkflowTemplate());
    }

    public Long getWorkflowTemplateId() {
        return workflowTemplateId;
    }

    @Produces
    @Named("workflowTemplateStep")
    public WorkflowTemplateStep getWorkflowTemplateStep() {
        return getInstance();
    }

    @Override
    public String save() {
        try {
            return super.save();
        } catch (final Exception e) {
            getFacesMessagesManager().printError(e.getLocalizedMessage());
            logger.severe("Save WorkflowTemplateStep throws " + e);
            return null;
        }
    }

    public void setWorkflowTemplateId(Long workflowTemplateId) {
        this.workflowTemplateId = workflowTemplateId;
    }
}
