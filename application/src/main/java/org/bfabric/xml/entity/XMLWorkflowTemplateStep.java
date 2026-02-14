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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.bfabric.entity.WorkflowTemplateStep;

@XmlRootElement(name = "workflowtemplatestep")
public class XMLWorkflowTemplateStep extends XMLAbstractEnabledBaseEntity {

    @XmlElement
    private String expectedduration;

    @XmlElement
    private String runsperprocess;

    @XmlElement
    private XMLSamplePreparationProtocol samplepreparationprotocol;

    @XmlElement
    private String samplesperprocess;

    @XmlElement
    private XMLWorkflowTemplate workflowtemplate;

    public XMLWorkflowTemplateStep() {
    }

    public XMLWorkflowTemplateStep(WorkflowTemplateStep entity, boolean reference) {
        super(entity, reference);
    }

    public XMLWorkflowTemplateStep(WorkflowTemplateStep workflowTemplateStep) {
        super(workflowTemplateStep);
        if (workflowTemplateStep != null) {
            if (workflowTemplateStep.getWorkflowTemplate() != null) {
                setWorkflowtemplate(new XMLWorkflowTemplate(workflowTemplateStep.getWorkflowTemplate(), true));
            }
            setExpectedduration(String.valueOf(workflowTemplateStep.getExpectedDuration()));
            setRunsperprocess(String.valueOf(workflowTemplateStep.getRunsPerProcess()));
            setSamplesperprocess(String.valueOf(workflowTemplateStep.getSamplesPerProcess()));
            if (workflowTemplateStep.getSamplePreparationProtocol() != null) {
                setSamplepreparationprotocol(new XMLSamplePreparationProtocol(workflowTemplateStep.getSamplePreparationProtocol(), true));
            }
        }
    }

    public String getExpectedduration() {
        return expectedduration;
    }

    public String getRunsperprocess() {
        return runsperprocess;
    }

    public XMLSamplePreparationProtocol getSamplepreparationprotocol() {
        return samplepreparationprotocol;
    }

    public String getSamplesperprocess() {
        return samplesperprocess;
    }

    public XMLWorkflowTemplate getWorkflowtemplate() {
        return workflowtemplate;
    }

    public void setExpectedduration(String expectedduration) {
        this.expectedduration = expectedduration;
    }

    public void setRunsperprocess(String runsperprocess) {
        this.runsperprocess = runsperprocess;
    }

    public void setSamplepreparationprotocol(XMLSamplePreparationProtocol samplepreparationprotocol) {
        this.samplepreparationprotocol = samplepreparationprotocol;
    }

    public void setSamplesperprocess(String samplesperprocess) {
        this.samplesperprocess = samplesperprocess;
    }

    public void setWorkflowtemplate(XMLWorkflowTemplate workflowtemplate) {
        this.workflowtemplate = workflowtemplate;
    }
}
