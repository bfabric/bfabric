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

import org.bfabric.entity.ServiceType;
import org.bfabric.entity.WorkflowTemplate;
import org.bfabric.entity.WorkflowTemplateStep;

@XmlRootElement(name = "workflowtemplate")
public class XMLWorkflowTemplate extends XMLAbstractEnabledBaseEntity {

    @XmlElement
    private List<XMLServiceType> servicetype = new ArrayList<>();

    @XmlElement
    private List<XMLWorkflowTemplateStep> workflowtemplatestep = new ArrayList<>();

    public XMLWorkflowTemplate() {
    }

    public XMLWorkflowTemplate(WorkflowTemplate entity, boolean reference) {
        super(entity, reference);
    }

    public XMLWorkflowTemplate(WorkflowTemplate workflowTemplate) {
        super(workflowTemplate);
        if (workflowTemplate != null) {
            if (workflowTemplate.getServiceTypes() != null) {
                for (ServiceType serviceType : workflowTemplate.getServiceTypes()) {
                    getServicetype().add(new XMLServiceType(serviceType, true));
                }
            }
            if (workflowTemplate.getWorkflowTemplateSteps() != null) {
                for (WorkflowTemplateStep step : workflowTemplate.getWorkflowTemplateSteps()) {
                    getWorkflowtemplatestep().add(new XMLWorkflowTemplateStep(step, true));
                }
            }
        }
    }

    public List<XMLServiceType> getServicetype() {
        return servicetype;
    }

    public List<XMLWorkflowTemplateStep> getWorkflowtemplatestep() {
        return workflowtemplatestep;
    }

    public void setServicetype(List<XMLServiceType> servicetype) {
        this.servicetype = servicetype;
    }

    public void setWorkflowtemplatestep(List<XMLWorkflowTemplateStep> workflowtemplatestep) {
        this.workflowtemplatestep = workflowtemplatestep;
    }

}
