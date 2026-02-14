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

package org.bfabric.forms;

import org.bfabric.entity.Container;
import org.bfabric.entity.Workflow;
import org.bfabric.entity.WorkflowTemplate;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveWorkflow;

public class MFWorkflow extends AbstractMF {

    private final Workflow workflow;

    private final XMLRequestParameterSaveWorkflow xmlRequestSaveWorkflow;

    public MFWorkflow(Workflow workflow, XMLRequestParameterSaveWorkflow xmlRequestSaveWorkflow) {
        this.workflow = workflow;
        this.xmlRequestSaveWorkflow = xmlRequestSaveWorkflow;
    }

    @Override
    public synchronized void apply() throws Exception {
        getWorkflow().setContainer(getContainer());
        getWorkflow().setWorkflowTemplate(getWorkflowTemplate());
        getWorkflow().setCustomAttributes(getXmlRequestSaveWorkflow().getCustomattribute());
    }

    public Container getContainer() throws InvalidDataException {
        Long containerId = MFHelper.positiveLongValueOf("containerid", getXmlRequestSaveWorkflow().getContainerid());
        Container ret = getWorkflow().getContainer();
        if (containerId != null && (ret == null || !containerId.equals(ret.getId()))) {
            ret = (Container) fetch(Container.class, containerId);
        }
        return ret;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public WorkflowTemplate getWorkflowTemplate() throws InvalidDataException {
        Long workflowTemplateId = MFHelper.positiveLongValueOf("workflowtemplateid", getXmlRequestSaveWorkflow().getWorkflowtemplateid());
        WorkflowTemplate ret = getWorkflow().getWorkflowTemplate();
        if (ret == null || !workflowTemplateId.equals(ret.getId())) {
            ret = (WorkflowTemplate) fetch(WorkflowTemplate.class, workflowTemplateId);
        }
        return ret;
    }

    public XMLRequestParameterSaveWorkflow getXmlRequestSaveWorkflow() {
        return xmlRequestSaveWorkflow;
    }
}
