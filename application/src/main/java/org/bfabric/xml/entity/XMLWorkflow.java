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

package org.bfabric.xml.entity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.Workflow;
import org.bfabric.entity.WorkflowStep;

@XmlRootElement(name = "workflow")
public class XMLWorkflow extends XMLAbstractBaseEntity {

    @XmlElement
    private XMLContainer container;

    @XmlElement
    private List<XMLWorkflowStep> workflowstep = new ArrayList<>();

    @XmlElement
    private XMLWorkflowTemplate workflowtemplate;

    public XMLWorkflow() {
    }

    public XMLWorkflow(Workflow entity, boolean reference) {
        super(entity, reference);
    }

    public XMLWorkflow(Workflow workflow) {
        super(workflow);
        if (workflow != null) {
            if (workflow.getContainer() != null) {
                setContainer(new XMLContainer(workflow.getContainer(), true));
            }
            if (workflow.getWorkflowTemplate() != null) {
                setWorkflowtemplate(new XMLWorkflowTemplate(workflow.getWorkflowTemplate(), true));
            }
            if (workflow.getWorkflowSteps() != null && !workflow.getWorkflowSteps().isEmpty()) {
                for (WorkflowStep workflowStep : workflow.getWorkflowSteps()) {
                    getWorkflowstep().add(new XMLWorkflowStep(workflowStep, true));
                }
            }
        }
    }

    public XMLContainer getContainer() {
        return container;
    }

    public List<XMLWorkflowStep> getWorkflowstep() {
        return workflowstep;
    }

    public XMLWorkflowTemplate getWorkflowtemplate() {
        return workflowtemplate;
    }

    public void setContainer(XMLContainer container) {
        this.container = container;
    }

    public void setWorkflowstep(List<XMLWorkflowStep> workflowstep) {
        this.workflowstep = workflowstep;
    }

    public void setWorkflowtemplate(XMLWorkflowTemplate workflowtemplate) {
        this.workflowtemplate = workflowtemplate;
    }
}
