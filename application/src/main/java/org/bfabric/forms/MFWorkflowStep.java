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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.bfabric.entity.Dataset;
import org.bfabric.entity.Sample;
import org.bfabric.entity.User;
import org.bfabric.entity.Workflow;
import org.bfabric.entity.WorkflowStep;
import org.bfabric.entity.WorkflowTemplateStep;
import org.bfabric.entity.Workunit;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.util.StringHelper;
import org.bfabric.webservice.request.parameter.XMLRequestParameterSaveWorkflowStep;

public class MFWorkflowStep extends AbstractMF {

    private final WorkflowStep workflowStep;

    private final XMLRequestParameterSaveWorkflowStep xmlRequestSaveWorkflowStep;

    public MFWorkflowStep(WorkflowStep workflowStep, XMLRequestParameterSaveWorkflowStep xmlRequestSaveWorkflowStep) {
        this.workflowStep = workflowStep;
        this.xmlRequestSaveWorkflowStep = xmlRequestSaveWorkflowStep;
    }

    @Override
    public synchronized void apply() throws Exception {
        getWorkflowStep().setWorkflow(getWorkflow());
        getWorkflowStep().setWorkflowTemplateStep(getWorkflowTemplateStep());
        getWorkflowStep().setStartDateTime(getStartDateTime());
        getWorkflowStep().setEndDateTime(getEndDateTime());
        getWorkflowStep().setExpectedDuration(getExpectedDuration());
        getWorkflowStep().setDatasets(getDatasets());
        getWorkflowStep().setSamples(getSamples());
        getWorkflowStep().setSupervisor(getSupervisor());
        getWorkflowStep().setWorkunits(getWorkunits());
        getWorkflowStep().setCustomAttributes(getXmlRequestSaveWorkflowStep().getCustomattribute());
    }

    public Set<Dataset> getDatasets() throws InvalidDataException {
        if (getXmlRequestSaveWorkflowStep().getDatasetid() != null) {
            Set<Dataset> datasets = new HashSet<>();
            for (String datasetId : getXmlRequestSaveWorkflowStep().getDatasetid()) {
                if (StringHelper.isNotEmpty(datasetId)) {
                    datasets.add((Dataset) fetch(Dataset.class, MFHelper.positiveLongValueOf("datasetid", datasetId)));
                }
            }
            return datasets;
        }
        return getWorkflowStep().getDatasets();
    }

    public LocalDateTime getEndDateTime() throws InvalidDataException {
        if (getXmlRequestSaveWorkflowStep().getEnddatetime() != null) {
            return MFHelper.dateTimeValueOf("enddatetime", getXmlRequestSaveWorkflowStep().getEnddatetime());
        }
        return getWorkflowStep().getEndDateTime();
    }

    public BigDecimal getExpectedDuration() throws InvalidDataException {
        if (getXmlRequestSaveWorkflowStep().getExpectedduration() != null) {
            return MFHelper.bigDecimalValueOf("expectedduration", getXmlRequestSaveWorkflowStep().getExpectedduration());
        }
        return getWorkflowStep().getExpectedDuration();
    }

    public Set<Sample> getSamples() throws InvalidDataException {
        if (getXmlRequestSaveWorkflowStep().getSampleid() != null) {
            Set<Sample> samples = new HashSet<>();
            for (String sampleId : getXmlRequestSaveWorkflowStep().getSampleid()) {
                if (StringHelper.isNotEmpty(sampleId)) {
                    samples.add((Sample) fetch(Sample.class, MFHelper.positiveLongValueOf("sampleid", sampleId)));
                }
            }
            return samples;
        }
        return getWorkflowStep().getSamples();
    }

    public LocalDateTime getStartDateTime() throws InvalidDataException {
        if (getXmlRequestSaveWorkflowStep().getStartdatetime() != null) {
            return MFHelper.dateTimeValueOf("startdatetime", getXmlRequestSaveWorkflowStep().getStartdatetime());
        }
        return getWorkflowStep().getStartDateTime();
    }

    private User getSupervisor() throws InvalidDataException {
        if (getXmlRequestSaveWorkflowStep().getSupervisorid() != null) {
            MFHelper.checkNotNull("supervisorid", getXmlRequestSaveWorkflowStep().getSupervisorid());
            return (User) fetch(User.class, MFHelper.positiveLongValueOf("supervisorid", getXmlRequestSaveWorkflowStep().getSupervisorid()));
        }
        return getWorkflowStep().getSupervisor();
    }

    public Workflow getWorkflow() throws InvalidDataException {
        if (getXmlRequestSaveWorkflowStep().getWorkflowid() != null) {
            return (Workflow) fetch(Workflow.class, MFHelper.positiveLongValueOf("workflowid", getXmlRequestSaveWorkflowStep().getWorkflowid()));
        }
        return getWorkflowStep().getWorkflow();
    }

    public WorkflowStep getWorkflowStep() {
        return workflowStep;
    }

    public WorkflowTemplateStep getWorkflowTemplateStep() throws InvalidDataException {
        if (getXmlRequestSaveWorkflowStep().getWorkflowtemplatestepid() != null) {
            return (WorkflowTemplateStep) fetch(WorkflowTemplateStep.class, MFHelper.positiveLongValueOf("workflowtemplatestepid", getXmlRequestSaveWorkflowStep().getWorkflowtemplatestepid()));
        }
        return getWorkflowStep().getWorkflowTemplateStep();
    }

    public Set<Workunit> getWorkunits() throws InvalidDataException {
        if (getXmlRequestSaveWorkflowStep().getWorkunitid() != null) {
            Set<Workunit> workunits = new HashSet<>();
            for (String workunitId : getXmlRequestSaveWorkflowStep().getWorkunitid()) {
                if (StringHelper.isNotEmpty(workunitId)) {
                    workunits.add((Workunit) fetch(Workunit.class, MFHelper.positiveLongValueOf("workunitid", workunitId)));
                }
            }
            return workunits;
        }
        return getWorkflowStep().getWorkunits();
    }

    public XMLRequestParameterSaveWorkflowStep getXmlRequestSaveWorkflowStep() {
        return xmlRequestSaveWorkflowStep;
    }
}
