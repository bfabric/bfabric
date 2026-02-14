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

import java.math.BigDecimal;

import org.bfabric.entity.WorkflowTemplate;
import org.bfabric.entity.WorkflowTemplateStep;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveWorkflowTemplateStep;

public class MFWorkflowTemplateStep extends AbstractMF {

    private final WorkflowTemplateStep workflowTemplateStep;

    private final XMLRequestParameterSaveWorkflowTemplateStep xmlRequestSaveWorkflowTemplateStep;

    public MFWorkflowTemplateStep(WorkflowTemplateStep workflowTemplateStep, XMLRequestParameterSaveWorkflowTemplateStep xmlRequestSaveWorkflowTemplateStep) {
        this.workflowTemplateStep = workflowTemplateStep;
        this.xmlRequestSaveWorkflowTemplateStep = xmlRequestSaveWorkflowTemplateStep;
    }

    @Override
    public synchronized void apply() throws Exception {
        getWorkflowTemplateStep().setName(getName());
        getWorkflowTemplateStep().setEnabled(getEnabled());
        getWorkflowTemplateStep().setDescription(getDescription());
        getWorkflowTemplateStep().setExpectedDuration(getExpectedDuration());
        getWorkflowTemplateStep().setRunsPerProcess(getRunsPerProcess());
        getWorkflowTemplateStep().setSamplesPerProcess(getSamplesPerProcess());
        getWorkflowTemplateStep().setWorkflowTemplate(getWorkflowTemplate());
    }

    public String getDescription() {
        if (getXmlRequestSaveWorkflowTemplateStep().getDescription() != null) {
            return getXmlRequestSaveWorkflowTemplateStep().getDescription();
        }
        return getWorkflowTemplateStep().getDescription();
    }

    public boolean getEnabled() throws InvalidDataException {
        if (getXmlRequestSaveWorkflowTemplateStep().getEnabled() != null) {
            return MFHelper.booleanValueOf("enabled", getXmlRequestSaveWorkflowTemplateStep().getEnabled());
        }
        return getWorkflowTemplateStep().isEnabled();
    }

    public BigDecimal getExpectedDuration() throws InvalidDataException {
        if (getXmlRequestSaveWorkflowTemplateStep().getExpectedduration() != null) {
            return MFHelper.bigDecimalValueOf("expectedduration", getXmlRequestSaveWorkflowTemplateStep().getExpectedduration());
        }
        return getWorkflowTemplateStep().getExpectedDuration();
    }

    public String getName() {
        if (getXmlRequestSaveWorkflowTemplateStep().getName() != null) {
            return getXmlRequestSaveWorkflowTemplateStep().getName();
        }
        return getWorkflowTemplateStep().getName();
    }

    public Integer getRunsPerProcess() throws InvalidDataException {
        if (getXmlRequestSaveWorkflowTemplateStep().getRunsperprocess() != null) {
            return MFHelper.integerValueOf("runsperprocess", getXmlRequestSaveWorkflowTemplateStep().getRunsperprocess());
        }
        return getWorkflowTemplateStep().getRunsPerProcess();
    }

    public Integer getSamplesPerProcess() throws InvalidDataException {
        if (getXmlRequestSaveWorkflowTemplateStep().getSamplesperprocess() != null) {
            return MFHelper.integerValueOf("samplesperprocess", getXmlRequestSaveWorkflowTemplateStep().getSamplesperprocess());
        }
        return getWorkflowTemplateStep().getSamplesPerProcess();
    }

    public WorkflowTemplate getWorkflowTemplate() throws InvalidDataException {
        if (getXmlRequestSaveWorkflowTemplateStep().getWorkflowtemplateid() != null) {
            return (WorkflowTemplate) fetch(WorkflowTemplate.class, MFHelper.positiveLongValueOf("workflowtemplateid", getXmlRequestSaveWorkflowTemplateStep().getWorkflowtemplateid()));
        }
        return getWorkflowTemplateStep().getWorkflowTemplate();
    }

    public WorkflowTemplateStep getWorkflowTemplateStep() {
        return workflowTemplateStep;
    }

    public XMLRequestParameterSaveWorkflowTemplateStep getXmlRequestSaveWorkflowTemplateStep() {
        return xmlRequestSaveWorkflowTemplateStep;
    }
}
