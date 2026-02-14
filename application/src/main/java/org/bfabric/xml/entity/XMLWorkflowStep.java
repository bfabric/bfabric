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

import org.bfabric.entity.Dataset;
import org.bfabric.entity.Sample;
import org.bfabric.entity.WorkflowStep;
import org.bfabric.entity.Workunit;
import org.bfabric.util.DateUtils;

@XmlRootElement(name = "workflowstep")
public class XMLWorkflowStep extends XMLAbstractBaseEntity {

    @XmlElement
    private List<XMLDataset> dataset = new ArrayList<>();

    @XmlElement
    private String enddatetime;

    @XmlElement
    private String expectedduration;

    @XmlElement
    private List<XMLSample> sample = new ArrayList<>();

    @XmlElement
    private String startdatetime;

    @XmlElement
    private String status;

    @XmlElement
    private XMLWorkflow workflow;

    @XmlElement
    private XMLWorkflowTemplateStep workflowtemplatestep;

    @XmlElement
    private List<XMLWorkunit> workunit = new ArrayList<>();

    public XMLWorkflowStep() {
    }

    public XMLWorkflowStep(WorkflowStep entity, boolean reference) {
        super(entity, reference);
    }

    public XMLWorkflowStep(WorkflowStep workflowStep) {
        super(workflowStep);
        if (workflowStep != null) {
            if (workflowStep.getWorkflow() != null) {
                setWorkflow(new XMLWorkflow(workflowStep.getWorkflow(), true));
            }
            if (workflowStep.getWorkflowTemplateStep() != null) {
                setWorkflowtemplatestep(new XMLWorkflowTemplateStep(workflowStep.getWorkflowTemplateStep(), true));
            }
            if (workflowStep.getStatus() != null) {
                setStatus(workflowStep.getStatus().name());
            }
            if (workflowStep.getStartDateTime() != null) {
                setStartdatetime(DateUtils.getDateAsFormattedString(workflowStep.getStartDateTime()));
            }
            if (workflowStep.getEndDateTime() != null) {
                setEnddatetime(DateUtils.getDateAsFormattedString(workflowStep.getEndDateTime()));
            }
            if (workflowStep.getExpectedDuration() != null) {
                setEnddatetime(String.valueOf(workflowStep.getExpectedDuration()));
            }
            if (workflowStep.getExpectedDuration() != null) {
                setExpectedduration(String.valueOf(workflowStep.getExpectedDuration()));
            }
            if (workflowStep.getDatasets() != null) {
                for (Dataset aDataset : workflowStep.getDatasets()) {
                    getDataset().add(new XMLDataset(aDataset, true));
                }
            }
            if (workflowStep.getSamples() != null) {
                for (Sample aSample : workflowStep.getSamples()) {
                    getSample().add(new XMLSample(aSample, true));
                }
            }
            if (workflowStep.getWorkunits() != null) {
                for (Workunit aWorkunit : workflowStep.getWorkunits()) {
                    getWorkunit().add(new XMLWorkunit(aWorkunit, true));
                }
            }
        }
    }

    public List<XMLDataset> getDataset() {
        return dataset;
    }

    public String getEnddatetime() {
        return enddatetime;
    }

    public String getExpectedduration() {
        return expectedduration;
    }

    public List<XMLSample> getSample() {
        return sample;
    }

    public String getStartdatetime() {
        return startdatetime;
    }

    public String getStatus() {
        return status;
    }

    public XMLWorkflow getWorkflow() {
        return workflow;
    }

    public XMLWorkflowTemplateStep getWorkflowtemplatestep() {
        return workflowtemplatestep;
    }

    public List<XMLWorkunit> getWorkunit() {
        return workunit;
    }

    public void setDataset(List<XMLDataset> dataset) {
        this.dataset = dataset;
    }

    public void setEnddatetime(String enddatetime) {
        this.enddatetime = enddatetime;
    }

    public void setExpectedduration(String expectedduration) {
        this.expectedduration = expectedduration;
    }

    public void setSample(List<XMLSample> sample) {
        this.sample = sample;
    }

    public void setStartdatetime(String startdatetime) {
        this.startdatetime = startdatetime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setWorkflow(XMLWorkflow workflow) {
        this.workflow = workflow;
    }

    public void setWorkflowtemplatestep(XMLWorkflowTemplateStep workflowtemplatestep) {
        this.workflowtemplatestep = workflowtemplatestep;
    }

    public void setWorkunit(List<XMLWorkunit> workunit) {
        this.workunit = workunit;
    }
}
